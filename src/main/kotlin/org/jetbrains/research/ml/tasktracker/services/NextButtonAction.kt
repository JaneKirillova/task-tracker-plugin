package org.jetbrains.research.ml.tasktracker.services

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState

class NextButtonAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        getEventProject(e)?.let {
            if (MainController.browserViews[it]?.state == ViewState.TASK_SOLVING) {
                MainController.browserViews[it]?.taskController?.startNextTask()
            }
        }
    }
}
