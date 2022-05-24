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
import org.jetbrains.research.ml.tasktracker.ui.util.subscribe
import javax.swing.JComponent


internal object MainController {

    private val logger: Logger = Logger.getInstance(javaClass)
    val browserViews = mutableListOf<BrowserView>()

    val taskController by lazy {
        TaskSolvingController(PluginServer.tasks)
    }

    val loadingStateController = LoadingStateController()
    val errorStateController = ErrorStateController()
    val successStateController = SuccessStateController()

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
                                loadingStateController.updateViewContent(view)
                            }
                        }
                        ServerConnectionResult.LOADING -> {
                            browserViews.forEach { view ->
                                loadingStateController.updateViewContent(view)
                            }
                        }
                        ServerConnectionResult.FAIL -> {
                            browserViews.forEach { view ->
                                errorStateController.updateViewContent(view)
                                errorStateController.setOnRefreshAction(view) {
                                    when (successStateController.currentState) {
                                        ViewState.GREETING -> {
                                            PluginServer.reconnect(view.project)
                                        }
                                        else -> {
                                            TrackerQueryExecutor.userId?.let {
                                                PluginServer.reconnectTasks(view.project)
                                            } ?: run {
                                                PluginServer.lastSendingProject = view.project
                                                PluginServer.reconnectUserId(view.project)
                                            }
                                        }
                                    }
                                    null
                                }
                            }
                        }
                        ServerConnectionResult.SUCCESS -> {
                            //TODO: ugly way to getting tasks after Id has been received? May be one more subscriber?
                            TrackerQueryExecutor.userId?.let { id ->
                                if (PluginServer.tasks.isEmpty()) {
                                    PluginServer.lastSendingProject?.let { PluginServer.reconnectTasks(it) }
                                }
                            }
                            browserViews.forEach { view ->
                                successStateController.updateViewContent(view)
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
                                loadingStateController.updateViewContent(view)
                            }
                        }

                        DataSendingResult.FAIL -> {
                            when (successStateController.currentState) {
                                ViewState.FEEDBACK -> {
                                    browserViews.forEach { view ->
                                        errorStateController.updateViewContent(view)
                                        errorStateController.setOnRefreshAction(view) {
                                            PluginServer.sendFeedback(
                                                successStateController.userData.feedback, view.project
                                            )
                                            null
                                        }
                                    }
                                }
                                else -> {
                                    browserViews.forEach { view ->
                                        errorStateController.updateViewContent(view)
                                        errorStateController.setOnRefreshAction(view) {
                                            logger.info("${Plugin.PLUGIN_NAME} on sending tasks connection failed. Try to resend")
                                            taskController.resendLastTask()
                                            null
                                        }
                                    }
                                }
                            }
                        }

                        DataSendingResult.SUCCESS -> {
                            if (taskController.isAllTasksSentOrSendNext()) {
                                browserViews.forEach { view -> successStateController.updateViewContent(view) }
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
        successStateController.updateViewContent(createdView)
        browserViews.add(createdView)

        return JBScrollPane(createdView)
    }
}
