package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState

class TaskSolvingControllerr(private val taskIterator: Iterator<Task>) {
    private val logger: Logger = Logger.getInstance(javaClass)

    private val storedIdeProperties = mutableMapOf<String, String>()
    private val appliedActions = mutableMapOf<String, Int>()
    var currentTask: Task? = null
        private set

    fun startSolvingNextTask(project: Project) {
        /*PluginServer.sendDataForTask(it, project)
        TaskFileHandler.closeTaskFiles(it)*/

        if (taskIterator.hasNext()) {
            currentTask = taskIterator.next()
            logger.info("Start solving $currentTask")

            currentTask?.let {
                ApplicationManager.getApplication().invokeLater {
                    TaskFileHandler.openTaskFiles(it)
                }

                it.ideSettings.actionsToToggle.forEach { action ->
                    logger.info("Toggle $action")
                    appliedActions[action] = appliedActions.getOrDefault(action, 0) + 1
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
            //TODO at last task
            logger.info("Solution Completed. Start uploading solutions.")

            for ((key, value) in storedIdeProperties) {
                PropertiesComponent.getInstance().setValue(key, value)
            }

            for ((action, usages) in appliedActions) {
                if (usages % 2 == 1) {
                    executeIdeAction(action)
                }
            }

            executeIdeAction("HideAllWindows")
            MainController.successViewController.currentState = ViewState.FEEDBACK
            MainController.browserViews.forEach {
                MainController.successViewController.updateViewContent(it)
            }
        }
    }

    private fun sendTasks() {

    }

    fun executeIdeAction(actionId: String) {
        ApplicationManager.getApplication().invokeLater {
            val action = ActionManager.getInstance().getAction(actionId)
            ActionManager.getInstance().tryToExecute(action, null, null, null, true)
        }
    }
}