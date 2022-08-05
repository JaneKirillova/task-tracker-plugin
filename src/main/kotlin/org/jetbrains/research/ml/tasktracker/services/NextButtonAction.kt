package org.jetbrains.research.ml.tasktracker.services

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.view.ViewState
import org.joda.time.LocalDate
import org.joda.time.Seconds

class NextButtonAction : AnAction() {
    private var lastPressedButtonTime = LocalDate.now()
    override fun actionPerformed(e: AnActionEvent) {
        getEventProject(e)?.let {
            if (checkCooldown() && MainController.browserViews[it]?.state == ViewState.TASK_SOLVING) {
                lastPressedButtonTime = LocalDate.now()
                MainController.browserViews[it]?.taskController?.startNextTask()
            }
        }
    }

    private fun checkCooldown(): Boolean =
        Seconds.secondsBetween(lastPressedButtonTime, LocalDate.now()).seconds > BUTTON_COOLDOWN

    companion object {
        private const val BUTTON_COOLDOWN = 5
    }
}
