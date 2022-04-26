package org.jetbrains.research.ml.tasktracker.tracking.dialog

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


abstract class CustomDialogWrapper : DialogWrapper(true) {

    abstract val customPreferredSize: Dimension?

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel(createMessage())
        // Todo: should I set it?
        customPreferredSize?.let {
            label.preferredSize = it
        }
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }

    abstract fun createMessage(): String
}