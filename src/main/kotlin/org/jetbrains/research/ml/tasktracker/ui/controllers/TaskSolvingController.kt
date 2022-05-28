package org.jetbrains.research.ml.tasktracker.ui.controllers

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.view.BrowserView

class TaskSolvingController(tasks: List<Task>, private val view: BrowserView) {
    private val logger: Logger = Logger.getInstance(javaClass)

    private val storedIdeProperties = mutableMapOf<String, String>()
    private val appliedActions = mutableMapOf<String, Int>()

    private val taskIterator = tasks.iterator()
    private val tasksToSendIterator = tasks.iterator()

    private var currentSolvingTask: Task? = null
    private var currentSendingTask: Task? = null

    fun startNextTask() {
        if (taskIterator.hasNext()) {
            currentSolvingTask = taskIterator.next()
            logger.info("Start solving ${currentSolvingTask?.key}")

            currentSolvingTask?.let {
                ApplicationManager.getApplication().invokeLater {
                    TaskFileHandler.openProjectTaskFile(view.project, it)
                }

                it.ideSettings.actionsToToggle.forEach { action ->
                    logger.info("Toggle $action")
                    appliedActions[action] = appliedActions.getOrDefault(action, 0).inc()
                    executeIdeAction(action)
                }
                for ((key, value) in it.ideSettings.parameters) {
                    //Mongoose database does not allow to store keys with dots
                    val correctKey = key.replace("___", ".")

                    if (!storedIdeProperties.containsKey(correctKey)) {
                        PropertiesComponent.getInstance().getValue(correctKey)?.let { userPropertiesValue ->
                            storedIdeProperties[correctKey] = userPropertiesValue
                        }
                    }
                    PropertiesComponent.getInstance().setValue(correctKey, value)
                }
            }

        } else {
            finishSolvingState()
        }
    }

    private fun finishSolvingState() {
        logger.info("Solution Completed. Start uploading solutions.")
        executeIdeAction("HideAllWindows")
        for ((key, value) in storedIdeProperties) {
            PropertiesComponent.getInstance().setValue(key, value)
        }

        for ((action, usages) in appliedActions) {
            if (usages % 2 == 1) {
                executeIdeAction(action)
            }
        }

        isAllTasksSentOrSendNext()
        view.state = ViewState.FEEDBACK
        MainController.successStateController.updateViewContent(view)
    }

    fun resendLastTask() {
        currentSendingTask?.let { task ->
            PluginServer.sendDataForTask(task, view.project)
        }
    }

    fun isAllTasksSentOrSendNext(): Boolean {
        if (tasksToSendIterator.hasNext()) {
            currentSendingTask = tasksToSendIterator.next()
            currentSendingTask?.let { task ->
                PluginServer.sendDataForTask(task, view.project)
            }
            return false
        }
        return true
    }

    fun executeIdeAction(actionId: String) {
        ApplicationManager.getApplication().invokeLater {
            ActionManager.getInstance().getAction(actionId)?.let { action ->
                ActionManager.getInstance().tryToExecute(action, null, null, null, true)
            }
        }
    }
}
