package org.jetbrains.research.ml.tasktracker.server

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.models.*
import org.jetbrains.research.ml.tasktracker.tracking.DocumentLogger
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState
import java.util.function.Consumer

enum class ServerConnectionResult {
    UNINITIALIZED, LOADING, SUCCESS, FAIL
}

enum class DataSendingResult {
    LOADING, SUCCESS, FAIL
}

interface ServerConnectionNotifier : Consumer<ServerConnectionResult> {
    companion object {
        val SERVER_CONNECTION_TOPIC = Topic.create("server connection result", ServerConnectionNotifier::class.java)
    }
}

interface DataSendingNotifier : Consumer<DataSendingResult> {
    companion object {
        val DATA_SENDING_TOPIC = Topic.create("data sending result", DataSendingNotifier::class.java)
    }
}

/**
 * Receives all the necessary data (UI settings, tasks, etc) from server and
 * sends back all gathered data (ActivityTracker + TaskTracker files)
 */
object PluginServer {
    var paneText: PaneText? = null
        private set
    var availableLanguages: List<PaneLanguage> = emptyList()
        private set
    var tasks: List<Task> = emptyList()
        private set
    var genders: List<Gender> = emptyList()
        private set
    var countries: List<Country> = emptyList()
        private set
    var taskSolvingErrorDialogText: TaskSolvingErrorDialogText? = null
        private set
    var programmingLanguages: List<Language> = emptyList()
        private set
    private val logger: Logger = Logger.getInstance(javaClass)

    var serverConnectionResult: ServerConnectionResult = ServerConnectionResult.UNINITIALIZED
        private set
    private var dataSendingResult: DataSendingResult = DataSendingResult.SUCCESS
    var lastProject: Project? = null

    fun checkItInitialized(project: Project) {
        if (serverConnectionResult == ServerConnectionResult.UNINITIALIZED) {
            when (MainController.successViewController.currentState) {
                ViewState.GREETING -> {
                    reconnect(project)
                }
                else -> {
                    reconnectTasks(project)
                }
            }
        }
    }

    /**
     * Receives tasks in background task and sends results about receiving
     */
    fun reconnectTasks(project: Project) {
        if (serverConnectionResult != ServerConnectionResult.LOADING) {
            logger.info("${Plugin.PLUGIN_NAME} PluginServer getting reconnect tasks, current thread is ${Thread.currentThread().name}")
            ProgressManager.getInstance().run(object : Backgroundable(project, "Getting tasks from server") {
                override fun run(indicator: ProgressIndicator) {
                    safeReceive {
                        tasks = receiveTasks()
                    }
                }
            })
        }
    }

    /**
     * Receives ID in background task and sends results about receiving
     */
    fun reconnectUserId(project: Project) {
        lastProject = project
        if (serverConnectionResult != ServerConnectionResult.LOADING) {
            logger.info("${Plugin.PLUGIN_NAME} PluginServer getting reconnect id, current thread is ${Thread.currentThread().name}")
            ProgressManager.getInstance().run(object : Backgroundable(project, "Getting ID from server") {
                override fun run(indicator: ProgressIndicator) {
                    safeReceive {
                        TrackerQueryExecutor.initUserId()
                    }
                }
            })
        }
    }

    /**
     * Receives all data in background task and sends results about receiving
     */
    fun reconnect(project: Project) {
        if (serverConnectionResult != ServerConnectionResult.LOADING) {
            logger.info("${Plugin.PLUGIN_NAME} PluginServer reconnect, current thread is ${Thread.currentThread().name}")
            ProgressManager.getInstance().run(object : Backgroundable(project, "Getting data from server") {
                override fun run(indicator: ProgressIndicator) {
                    safeReceive { receiveData() }
                }
            })
        }
    }

    /**
     * Tries to call [receive] and sends the result of it to all subscribers
     */
    private fun safeReceive(receive: () -> Unit) {
        logger.info("${Plugin.PLUGIN_NAME} PluginServer safeReceive, current thread is ${Thread.currentThread().name}")
        val publisher =
            ApplicationManager.getApplication().messageBus.syncPublisher(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC)
        serverConnectionResult = ServerConnectionResult.LOADING
        publisher.accept(serverConnectionResult)

        serverConnectionResult = try {
            receive()
            ServerConnectionResult.SUCCESS
        } catch (e: java.lang.IllegalStateException) {
            ServerConnectionResult.FAIL
        }
        publisher.accept(serverConnectionResult)
    }

    private fun receiveData() {
        paneText = receivePaneText()
        availableLanguages = receiveAvailableLanguages()
        genders = receiveGenders()
        countries = receiveCountries()
        taskSolvingErrorDialogText = receiveTaskSolvingErrorDialogText()
        programmingLanguages = receiveProgrammingLanguages()
    }

    private fun receiveProgrammingLanguages(): List<Language> {
        return CollectionsQueryExecutor.getCollection("programming-language/all", ProgrammingLanguage.serializer())
            .mapNotNull { it.getLanguage() }
    }

    private fun receiveAvailableLanguages(): List<PaneLanguage> {
        return CollectionsQueryExecutor.getCollection("language/all", PaneLanguage.serializer())
    }

    private fun receiveTasks(): List<Task> {
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

    private fun receiveGenders(): List<Gender> {
        return CollectionsQueryExecutor.getCollection("gender/all", Gender.serializer())
    }

    private fun receiveCountries(): List<Country> {
        return CollectionsQueryExecutor.getCollection("country/all", Country.serializer())
    }

    private fun receivePaneText(): PaneText {
        val paneTextList = CollectionsQueryExecutor.getCollection("settings", PaneText.serializer())
        if (paneTextList.size == 1) {
            return paneTextList[0]
        }
        throw IllegalStateException("Got incorrect data from server")
    }

    private fun receiveTaskSolvingErrorDialogText(): TaskSolvingErrorDialogText {
        return CollectionsQueryExecutor.getItemFromCollection(
            "dialog-text/task_solving_error", TaskSolvingErrorDialogText.serializer()
        )
    }


    fun sendFeedback(feedback: String?, project: Project) {
        ApplicationManager.getApplication().invokeAndWait {
            ProgressManager.getInstance().run(object : Backgroundable(project, "Sending feedback", false) {
                override fun run(indicator: ProgressIndicator) {
                    sendFeedbackByJson(feedback, TrackerQueryExecutor.userId)
                }
            })
        }
    }

    private fun sendFeedbackByJson(feedback: String?, id: String?) {
        if (dataSendingResult != DataSendingResult.LOADING) {
            val publisher =
                ApplicationManager.getApplication().messageBus.syncPublisher(DataSendingNotifier.DATA_SENDING_TOPIC)
            dataSendingResult = DataSendingResult.LOADING
            publisher.accept(dataSendingResult)
            dataSendingResult = try {
                TrackerQueryExecutor.sendFeedbackData(feedback, id)
                DataSendingResult.SUCCESS
            } catch (e: java.lang.IllegalStateException) {
                DataSendingResult.FAIL
            }
            publisher.accept(dataSendingResult)
        }
    }

    fun sendDataForTask(task: Task, project: Project) {
        ApplicationManager.getApplication().invokeAndWait {
            val document = TaskFileHandler.getDocument(project, task)
            ProgressManager.getInstance()
                .run(object : Backgroundable(project, "Sending task ${task.key} solution", false) {
                    override fun run(indicator: ProgressIndicator) {
                        sendFileByDocument(document)
                    }
                })
        }
    }


    private fun sendFileByDocument(document: Document) {
        if (dataSendingResult != DataSendingResult.LOADING) {
            val publisher =
                ApplicationManager.getApplication().messageBus.syncPublisher(DataSendingNotifier.DATA_SENDING_TOPIC)
            dataSendingResult = DataSendingResult.LOADING
            publisher.accept(dataSendingResult)
            dataSendingResult = try {
                // Logs the last state (need to RUN ON EDT)
                ApplicationManager.getApplication().invokeAndWait { DocumentLogger.log(document) }
                val docPrinter = DocumentLogger.getDocumentLogPrinter(document)
                    ?: throw IllegalStateException("No printer for the document $document exists")
                TrackerQueryExecutor.sendData(docPrinter.getLogFiles())
                // Remove inactive printers only if sendData went well
                docPrinter.removeInactivePrinters()
                DataSendingResult.SUCCESS
            } catch (e: java.lang.IllegalStateException) {
                DataSendingResult.FAIL
            }
            publisher.accept(dataSendingResult)
        }
    }
}
