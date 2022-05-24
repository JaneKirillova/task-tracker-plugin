package org.jetbrains.research.ml.tasktracker.services

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState

class NextButtonAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        if (MainController.successStateController.currentState == ViewState.TASK_SOLVING)
            getEventProject(e)?.let {
                MainController.taskController.startNextTask(it)
            }
    }
}
