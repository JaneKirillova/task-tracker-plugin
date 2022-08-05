package org.jetbrains.research.ml.tasktracker.services

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.view.ViewState

class NextButtonAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        getEventProject(e)?.let { project ->
            MainController.browserViews[project]?.let {
                if (it.taskController.checkCooldown() && it.state == ViewState.TASK_SOLVING) {
                    it.taskController.startNextTask()
                }
            }
        }
    }
}
