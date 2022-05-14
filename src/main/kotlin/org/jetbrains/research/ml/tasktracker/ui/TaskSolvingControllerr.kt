package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState


class TaskSolvingControllerr(private val taskIterator: Iterator<Task>) {
    private val logger: Logger = Logger.getInstance(javaClass)

    var currentTask: Task? = null
        private set

    fun startSolvingNextTask(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            currentTask?.let {
                PluginServer.sendDataForTask(it, project)
                //TaskFileHandler.closeTaskFiles(it)
            } ?: run {
                val action = ActionManager.getInstance().getAction("HideAllWindows")
                ActionManager.getInstance().tryToExecute(action, null, null, null, true)
            }

            if (taskIterator.hasNext()) {
                currentTask = taskIterator.next()
                logger.info("Start solving $currentTask")
                currentTask?.let { TaskFileHandler.openTaskFiles(it) }
            } else {
                //TODO at last task
                logger.info("Solution Completed. Start uploading solutions.")
                val action = ActionManager.getInstance().getAction("HideAllWindows")
                ActionManager.getInstance().tryToExecute(action, null, null, null, true)

                MainController.successViewController.currentState = ViewState.FEEDBACK
                MainController.browserViews.forEach {
                    MainController.successViewController.updateViewContent(it)
                }
            }
        }
    }

    private fun sendTasks() {

    }

}