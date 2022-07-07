package org.jetbrains.research.ml.tasktracker.services

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.view.ViewState

class CustomCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val currentView = MainController.browserViews[parameters.editor.project]
        if (currentView?.state == ViewState.TASK_SOLVING && currentView.taskController.isZenModded()) {
            result.stopHere()
        } else {
            super.fillCompletionVariants(parameters, result)
        }
    }
}
