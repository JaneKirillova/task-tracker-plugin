package org.jetbrains.research.ml.tasktracker.tracking.dialog

import org.jetbrains.research.ml.tasktracker.models.TaskSolvingErrorText
import java.awt.Dimension

class EndTaskSequenceDialogWrapper : CustomDialogWrapper() {

    private val defaultEndTaskSolvingText = TaskSolvingErrorText(
        "Congratulations",
        "You have finished the last task. To continue, go to the TaskTracker plugin window."
    )
    override val customPreferredSize: Dimension = Dimension(300, 150)

    override fun createMessage(): String {
        val dialogText = defaultEndTaskSolvingText.description
        return "<html>${
            java.lang.String.format(
                dialogText
            )
        }</html>"
    }

    init {
        init()
        title = defaultEndTaskSolvingText.header
    }
}