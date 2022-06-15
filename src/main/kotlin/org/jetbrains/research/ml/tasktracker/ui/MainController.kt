package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.server.*
import org.jetbrains.research.ml.tasktracker.ui.controllers.ErrorStateController
import org.jetbrains.research.ml.tasktracker.ui.controllers.LoadingStateController
import org.jetbrains.research.ml.tasktracker.ui.controllers.SuccessStateController
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState
import org.jetbrains.research.ml.tasktracker.ui.util.SurveyData
import org.jetbrains.research.ml.tasktracker.ui.util.subscribe
import org.jetbrains.research.ml.tasktracker.ui.view.BrowserView
import javax.swing.JComponent


internal object MainController {

    private val logger: Logger = Logger.getInstance(javaClass)
    val browserViews = mutableMapOf<Project, BrowserView>()

    val loadingStateController = LoadingStateController()
    val errorStateController = ErrorStateController()
    val successStateController = SuccessStateController()

    init {
        /* Subscribes to notifications about server connection result to update all views */
        subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
            override fun accept(connection: ServerConnectionResult) {
                logger.info("${Plugin.PLUGIN_NAME} MainController, server connection topic $connection, current thread is ${Thread.currentThread().name}")
                ApplicationManager.getApplication().invokeLater {
                    logger.info("${Plugin.PLUGIN_NAME} MainController, server connection topic $connection in application block, current thread is ${Thread.currentThread().name}")
                    browserViews.values.forEach { view ->
                        when (connection) {
                            ServerConnectionResult.UNINITIALIZED -> {
                                loadingStateController.updateViewContent(view)
                            }
                            ServerConnectionResult.LOADING -> {
                                loadingStateController.updateViewContent(view)
                            }
                            ServerConnectionResult.FAIL -> {
                                errorStateController.updateViewContent(view)
                                errorStateController.setOnRefreshAction(view) {
                                    when (view.state) {
                                        ViewState.AGREEMENT -> {
                                            PluginServer.reconnect(view.project)
                                        }
                                        else -> {
                                            PluginServer.reconnectUserId(view.project)
                                        }
                                    }
                                    null
                                }
                            }
                            ServerConnectionResult.SUCCESS -> {
                                successStateController.updateViewContent(view)
                            }
                        }
                    }
                }
            }
        })

        /* Subscribes to notifications about server sending result to update all views */
        subscribe(DataSendingNotifier.DATA_SENDING_TOPIC, object : DataSendingNotifier {
            override fun accept(result: DataSendingResult) {
                ApplicationManager.getApplication().invokeLater {
                    browserViews.values.forEach { view ->
                        when (result) {
                            DataSendingResult.LOADING -> {
                                loadingStateController.updateViewContent(view)
                            }
                            DataSendingResult.FAIL -> {
                                when (view.state) {
                                    ViewState.FEEDBACK -> {
                                        errorStateController.updateViewContent(view)
                                        errorStateController.setOnRefreshAction(view) {
                                            PluginServer.sendFeedback(
                                                SurveyData.feedback, view.project
                                            )
                                            null
                                        }
                                    }
                                    else -> {
                                        errorStateController.updateViewContent(view)
                                        errorStateController.setOnRefreshAction(view) {
                                            logger.info("${Plugin.PLUGIN_NAME} on sending tasks connection failed. Try to resend")
                                            view.taskController.resendLastTask()
                                            null
                                        }
                                    }
                                }
                            }

                            DataSendingResult.SUCCESS -> {
                                if (view.taskController.isAllTasksSentOrSendNext()) {
                                    successStateController.updateViewContent(view)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    fun createContent(project: Project): JComponent {
        logger.info("${Plugin.PLUGIN_NAME} MainController create content, current thread is ${Thread.currentThread().name}")

        val createdView = BrowserView(project)
        successStateController.updateViewContent(createdView)
        browserViews[project] = createdView
        PluginServer.checkItInitialized(project)

        return JBScrollPane(createdView)
    }
}
