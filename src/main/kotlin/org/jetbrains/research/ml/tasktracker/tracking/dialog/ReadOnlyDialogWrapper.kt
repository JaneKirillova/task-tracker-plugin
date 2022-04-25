package org.jetbrains.research.ml.tasktracker.tracking.dialog


import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.models.TaskSolvingErrorText
import java.awt.Dimension


class ReadOnlyDialogWrapper(private val task: Task) : CustomDialogWrapper() {
    private val defaultTaskSolvingErrorText = TaskSolvingErrorText(
        "Task solving",
        "You cannot edit this file, this file is not your current task. Follow the instructions in the TaskTracker plugin window."
    )
    override val customPreferredSize: Dimension = Dimension(300, 150)

    override fun createMessage(): String {
        val dialogText = defaultTaskSolvingErrorText.description
        return "<html>${
            java.lang.String.format(
                dialogText
            )
        }</html>"
    }

    init {
        init()
        title = defaultTaskSolvingErrorText.header
    }
}