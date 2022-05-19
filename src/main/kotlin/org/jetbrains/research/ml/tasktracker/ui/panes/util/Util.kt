package org.jetbrains.research.ml.tasktracker.ui.panes.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import java.util.function.Consumer

fun <T : Any, C : Consumer<T>> subscribe(topic: Topic<C>, notifier: C) {
    ApplicationManager.getApplication().messageBus.connect().subscribe(topic, notifier)
}
