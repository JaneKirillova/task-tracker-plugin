package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.server.*
import org.jetbrains.research.ml.tasktracker.ui.controllers.ErrorViewController
import org.jetbrains.research.ml.tasktracker.ui.controllers.LoadingViewController
import org.jetbrains.research.ml.tasktracker.ui.controllers.SuccessViewController
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState
import org.jetbrains.research.ml.tasktracker.ui.util.subscribe
import javax.swing.JComponent


internal object MainController {

    private val logger: Logger = Logger.getInstance(javaClass)
    val browserViews = mutableListOf<BrowserView>()

    val taskController by lazy {
        TaskSolvingController(PluginServer.tasks)
    }

    val loadingViewController = LoadingViewController()
    val errorViewController = ErrorViewController()
    val successViewController = SuccessViewController()

    init {
        /* Subscribes to notifications about server connection result to update visible view */
        subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
            override fun accept(connection: ServerConnectionResult) {
                logger.info("${Plugin.PLUGIN_NAME} MainController, server connection topic $connection, current thread is ${Thread.currentThread().name}")
                ApplicationManager.getApplication().invokeLater {
                    logger.info("${Plugin.PLUGIN_NAME} MainController, server connection topic $connection in application block, current thread is ${Thread.currentThread().name}")
                    when (connection) {
                        ServerConnectionResult.UNINITIALIZED -> {
                            browserViews.forEach { view ->
                                loadingViewController.updateViewContent(view)
                            }
                        }
                        ServerConnectionResult.LOADING -> {
                            browserViews.forEach { view ->
                                loadingViewController.updateViewContent(view)
                            }
                        }
                        ServerConnectionResult.FAIL -> {
                            browserViews.forEach { view ->
                                errorViewController.updateViewContent(view)
                                errorViewController.setOnRefreshAction(view) {
                                    when (successViewController.currentState) {
                                        ViewState.GREETING -> {
                                            PluginServer.reconnect(view.project)
                                        }
                                        else -> {
                                            PluginServer.reconnectTasks(view.project)
                                        }
                                    }
                                    null
                                }
                            }
                        }
                        ServerConnectionResult.SUCCESS -> {
                            browserViews.forEach { view ->
                                successViewController.updateViewContent(view)
                            }
                        }
                    }
                }
            }
        })

        /* Subscribes to notifications about server sending result to update visible view */
        subscribe(DataSendingNotifier.DATA_SENDING_TOPIC, object : DataSendingNotifier {
            override fun accept(result: DataSendingResult) {
                ApplicationManager.getApplication().invokeLater {
                    when (result) {
                        DataSendingResult.LOADING -> {
                            browserViews.forEach { view ->
                                loadingViewController.updateViewContent(view)
                            }
                        }

                        DataSendingResult.FAIL -> {
                            when (successViewController.currentState) {
                                ViewState.FEEDBACK -> {
                                    browserViews.forEach { view ->
                                        errorViewController.updateViewContent(view)
                                        errorViewController.setOnRefreshAction(view) {
                                            PluginServer.sendFeedback(
                                                successViewController.userData.feedback, view.project
                                            )
                                            null
                                        }
                                    }
                                }
                                else -> {
                                    browserViews.forEach { view ->
                                        errorViewController.updateViewContent(view)
                                        errorViewController.setOnRefreshAction(view) {
                                            logger.info("${Plugin.PLUGIN_NAME} on sending tasks connection failed. Try to resend")
                                            taskController.resendLastTask()
                                            null
                                        }
                                    }
                                }
                            }
                        }

                        DataSendingResult.SUCCESS -> {
                            if (!taskController.isAllTasksSentOrSendNext()) {
                                browserViews.forEach { view -> successViewController.updateViewContent(view) }
                            }
                        }
                    }
                }
            }
        })
    }

    fun createContent(project: Project): JComponent {
        logger.info("${Plugin.PLUGIN_NAME} MainController create content, current thread is ${Thread.currentThread().name}")
        PluginServer.checkItInitialized(project)

        val createdView = BrowserView(project)
        successViewController.updateViewContent(createdView)
        browserViews.add(createdView)

        return JBScrollPane(createdView)
    }
}
