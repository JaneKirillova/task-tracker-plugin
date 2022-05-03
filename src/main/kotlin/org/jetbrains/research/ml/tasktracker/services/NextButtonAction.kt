package org.jetbrains.research.ml.tasktracker.services

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.ml.tasktracker.ui.MainController

class NextButtonAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        getEventProject(e)?.let {
            MainController.taskController.startSolvingNextTask(it)
        }
    }
}
