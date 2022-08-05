package org.jetbrains.research.ml.tasktracker.ui.controllers

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.server.TrackerQueryExecutor
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.view.BrowserView
import org.jetbrains.research.ml.tasktracker.ui.view.ViewState
import org.joda.time.DateTime
import org.joda.time.Seconds

class TaskSolvingController(tasks: List<Task>, private val view: BrowserView) {
    private val logger: Logger = Logger.getInstance(javaClass)

    private val storedIdeProperties = mutableMapOf<String, String>()
    private val appliedActions = mutableMapOf<String, Int>()

    private val taskIterator = tasks.iterator()
    private val tasksToSendIterator = tasks.iterator()

    var currentSolvingTask: Task? = null
        private set
    private var currentSendingTask: Task? = null
    private var lastTaskStartTime = DateTime.now()

    fun isZenModded() = appliedActions["ToggleZenMode"]?.rem(2) == 1

    fun checkCooldown() =
        Seconds.secondsBetween(lastTaskStartTime, DateTime.now()).seconds > BUTTON_COOLDOWN

    fun startSolving() {
        hideToolWindow("TaskTracker")
        startNextTask()
    }

    fun startNextTask() {
        if (taskIterator.hasNext()) {
            lastTaskStartTime = DateTime.now()
            currentSolvingTask = taskIterator.next()
            logger.info("Start solving ${currentSolvingTask?.key}")

            currentSolvingTask?.let {
                ApplicationManager.getApplication().invokeLater {
                    TaskFileHandler.openProjectTaskFile(view.project, it)
                }

                it.ideSettings?.actionsToToggle?.forEach { action ->
                    if (appliedActions[action]?.rem(2) != 1) {
                        logger.info("Toggle $action")
                        appliedActions[action] = appliedActions.getOrDefault(action, 0).inc()
                        executeIdeAction(action)
                    }
                } ?: run {
                    returnToDefaultActions()
                    showToolWindow(ToolWindowId.PROJECT_VIEW)
                    showToolWindow("Python Console")
                }
                //TODO update logic for it
                val properties = it.ideSettings?.parameters ?: emptyMap()
                for ((key, value) in properties) {
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

    fun returnToUserProperties() {
        for ((key, value) in storedIdeProperties) {
            PropertiesComponent.getInstance().setValue(key, value)
        }
    }

    private fun returnToDefaultActions() {
        for ((action, usages) in appliedActions) {
            if (usages % 2 == 1) {
                executeIdeAction(action)
                appliedActions[action] = usages.inc()
            }
        }
    }

    private fun finishSolvingState() {
        logger.info("Solution Completed. Start uploading solutions.")

        returnToDefaultActions()
        showToolWindow("TaskTracker")
        hideToolWindow("Python Console")

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
        TrackerQueryExecutor.clearActivityTrackerFile()
        return true
    }

    private fun executeIdeAction(actionId: String) {
        ApplicationManager.getApplication().invokeLater {
            ActionManager.getInstance().getAction(actionId)?.let { action ->
                ActionManager.getInstance().tryToExecute(action, null, null, null, true)
            }
        }
    }

    private fun showToolWindow(id: String) {
        ApplicationManager.getApplication().invokeLater {
            ToolWindowManager.getInstance(view.project).getToolWindow(id)?.show()
        }
    }

    private fun hideToolWindow(id: String) {
        ApplicationManager.getApplication().invokeLater {
            ToolWindowManager.getInstance(view.project).getToolWindow(id)?.hide()
        }
    }

    companion object {
        private const val BUTTON_COOLDOWN = 5
    }
}
