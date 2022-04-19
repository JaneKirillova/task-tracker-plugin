package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import javafx.application.Platform
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.server.*
import org.jetbrains.research.ml.tasktracker.ui.panes.*
import org.jetbrains.research.ml.tasktracker.ui.panes.util.PaneController
import org.jetbrains.research.ml.tasktracker.ui.panes.util.PaneControllerManager
import org.jetbrains.research.ml.tasktracker.ui.panes.util.Updatable
import org.jetbrains.research.ml.tasktracker.ui.panes.util.subscribe
import javax.swing.JComponent
import javax.swing.JPanel


typealias Pane = PaneControllerManager<out PaneController>

internal object MainController {

    private val logger: Logger = Logger.getInstance(javaClass)
    private val contents: MutableList<Content> = arrayListOf()
    private val showingPane = ShowingPanel()
    private lateinit var project: Project
    private val panes: List<Pane> = arrayListOf(
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
        /* Subscribes to notifications about server connection result to update visible panes */
        subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
            override fun accept(connection: ServerConnectionResult) {
                logger.info("${Plugin.PLUGIN_NAME} MainController, server connection topic $connection, current thread is ${Thread.currentThread().name}")
                ApplicationManager.getApplication().invokeLater {
                    logger.info("${Plugin.PLUGIN_NAME} MainController, server connection topic $connection in application block, current thread is ${Thread.currentThread().name}")
                    when (connection) {
                        ServerConnectionResult.UNINITIALIZED -> {
                            showingPane.updatePanel(javaClass.getResource("/ui/LoadingPane.html").readText())
                        }
                        ServerConnectionResult.LOADING -> {
                            showingPane.updatePanel(javaClass.getResource("/ui/LoadingPane.html").readText())
                        }
                        ServerConnectionResult.FAIL -> {
                            showingPane.updatePanel(javaClass.getResource("/ui/ErrorPane.html").readText())
                            showingPane.executeJavascript(
                                """                 
                                 var myButton = document.getElementById('refresh-button');
                                 myButton.onclick = function () {
                                 """, """}"""
                            ) {
                                PluginServer.reconnect(project)
                                null
                            }
                        }
                        ServerConnectionResult.SUCCESS -> {
                            showingPane.updatePanel(javaClass.getResource("/ui/PassportPane.html").readText())
                            showingPane.executeJavascript(
                                """
                                    var elements = document.querySelectorAll('.question:checked');
                                    var selectedVariants = Array.from(elements).map(element => element.value).join(',');
                                    alert(selectedVariants)
                                    var myButton = document.getElementById('next-button');
                                    myButton.onclick = function () {
                                    """, """}""", "selectedVariants"
                            ) {
                                println(it)
                                null
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
        val panel = showingPane
        this@MainController.project = project
        PluginServer.checkItInitialized(project)
        return JBScrollPane(panel)
    }

    /**
     * Represents ui content that needs to be created. It contains [panel] to which all [panesToCreateContent] should
     * add their contents.
     */
    data class Content(
        val panel: JPanel,
        val project: Project,
        val scale: Double,
        var panesToCreateContent: List<Pane>
    ) {
        init {
            logger.info("${Plugin.PLUGIN_NAME} Content init, current thread is ${Thread.currentThread().name}")
            updatePanesToCreate()
        }

        /**
         * RUN ON EDT
         * Looks to all [panesToCreateContent] and checks if any can create content. If so, creates pane contents,
         * adds them to the [panel], and removes created panes from [panesToCreateContent]
         */
        fun updatePanesToCreate() {
            logger.info("${Plugin.PLUGIN_NAME} updatePanesToCreate, current thread is ${Thread.currentThread().name}")
            val (canCreateContentPanes, cantCreateContentPanes) = panesToCreateContent.partition { it.canCreateContent }
            if (canCreateContentPanes.isNotEmpty()) {
                canCreateContentPanes.map { it.createContent(project, scale) }.forEach { panel.add(it) }
                Platform.runLater {
                    logger.info("${Plugin.PLUGIN_NAME} updatePanesToCreate in platform block, current thread is ${Thread.currentThread().name}")
                    canCreateContentPanes.map { it.getLastAddedPaneController() }.forEach {
                        if (it is Updatable) {
                            it.update()
                        }
                    }
                }
                panesToCreateContent = cantCreateContentPanes
            }
        }
    }
}
