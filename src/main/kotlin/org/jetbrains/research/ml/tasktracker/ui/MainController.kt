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
import org.jetbrains.research.ml.tasktracker.ui.panes.*
import org.jetbrains.research.ml.tasktracker.ui.panes.util.PaneController
import org.jetbrains.research.ml.tasktracker.ui.panes.util.PaneControllerManager
import org.jetbrains.research.ml.tasktracker.ui.panes.util.subscribe
import javax.swing.JComponent


typealias Pane = PaneControllerManager<out PaneController>

internal object MainController {

    private val logger: Logger = Logger.getInstance(javaClass)
    val browserViews = mutableListOf<BrowserView>()

    val taskController by lazy {
        TaskSolvingControllerr(PluginServer.tasks.iterator())
    }

    val loadingViewController = LoadingViewController()
    val errorViewController = ErrorViewController()
    val successViewController = SuccessViewController()
    val panes: List<Pane> = arrayListOf(
        ErrorControllerManager,
        LoadingControllerManager,
        SurveyControllerManager,
        TaskChoosingControllerManager,
        TaskSolvingControllerManager,
        FinalControllerManager,
        SuccessControllerManager
    )

    internal var visiblePane: Pane? = LoadingControllerManager
        set(value) {
            logger.info("${Plugin.PLUGIN_NAME} $value set visible, current thread is ${Thread.currentThread().name}")
            panes.forEach { it.setVisible(it == value) }
            field = value
        }

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
                                    PluginServer.reconnect(view.project)
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

        subscribe(DataSendingNotifier.DATA_SENDING_TOPIC, object : DataSendingNotifier {
            override fun accept(result: DataSendingResult) {
                ApplicationManager.getApplication().invokeLater {
                    visiblePane = when (result) {
                        DataSendingResult.LOADING -> LoadingControllerManager
                        DataSendingResult.FAIL -> {
                            val currentTask = TaskChoosingUiData.chosenTask.currentValue
//                            Todo: what pane to show if task is null? ErrorController with outdated refresh action?
                            currentTask?.let { task ->
                                ErrorControllerManager.setRefreshAction { PluginServer.sendDataForTask(task, it) }
                            }
                            ErrorControllerManager
                        }


                        DataSendingResult.SUCCESS -> SuccessControllerManager
                    }
                }
            }
        })
    }

    /*   RUN ON EDT (ToolWindowFactory takes care of it) */
    fun createContent(project: Project): JComponent {
        logger.info("${Plugin.PLUGIN_NAME} MainController create content, current thread is ${Thread.currentThread().name}")
        PluginServer.checkItInitialized(project)

        val createdView = BrowserView(project)
        successViewController.updateViewContent(createdView)
        browserViews.add(createdView)

        return JBScrollPane(createdView)
    }
}
