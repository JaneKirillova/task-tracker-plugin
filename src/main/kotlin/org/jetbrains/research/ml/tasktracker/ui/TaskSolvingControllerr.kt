package org.jetbrains.research.ml.tasktracker.ui

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
            }
            if (taskIterator.hasNext()) {
                currentTask = taskIterator.next()
                logger.info("Start solving $currentTask")
                currentTask?.let { TaskFileHandler.openTaskFiles(it) }
            } else {
                //TODO at last task
                logger.info("Solution Completed. Start uploading solutions.")
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