package org.jetbrains.research.ml.tasktracker.tracking

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.server.TrackerQueryExecutor
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.joda.time.DateTime

data class LoggedDataGetter<T, S>(val header: String, val getData: (T) -> S)

abstract class LoggedData<T, S> {
    protected abstract val loggedDataGetters: List<LoggedDataGetter<T, S>>

    val headers: List<String>
        get() = loggedDataGetters.map { it.header }

    fun getData(t: T): List<S> {
        return loggedDataGetters.map { it.getData(t) }
    }
}

enum class UiLoggedDataHeader(val header: String) {
    Name("name"),
    Email("email"),
    Answers("answers"),
}

object UiLoggedData : LoggedData<Unit, String>() {
    override val loggedDataGetters: List<LoggedDataGetter<Unit, String>> = arrayListOf(
        LoggedDataGetter(UiLoggedDataHeader.Name.header) { MainController.successViewController.userData.name.toString() },
        LoggedDataGetter(UiLoggedDataHeader.Email.header) { MainController.successViewController.userData.email.toString() },
        LoggedDataGetter(UiLoggedDataHeader.Answers.header) { MainController.successViewController.userData.listOfAnswers.toString() },
    )
}

object DocumentLoggedData : LoggedData<Document, String?>() {
    override val loggedDataGetters: List<LoggedDataGetter<Document, String?>> = arrayListOf(
        LoggedDataGetter("date") { DateTime.now().toString() },
        LoggedDataGetter("timestamp") { it.modificationStamp.toString() },
        LoggedDataGetter("fileName") { FileDocumentManager.getInstance().getFile(it)?.name },
        LoggedDataGetter("fileHashCode") { FileDocumentManager.getInstance().getFile(it)?.hashCode().toString() },
        LoggedDataGetter("documentHashCode") { it.hashCode().toString() },
        LoggedDataGetter("fragment") { it.text },
        LoggedDataGetter("userId") { TrackerQueryExecutor.userId },
        LoggedDataGetter("testMode") { Plugin.testMode.toString() }
    )
}