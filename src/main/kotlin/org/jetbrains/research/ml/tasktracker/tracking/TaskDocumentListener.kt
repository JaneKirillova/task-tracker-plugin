package org.jetbrains.research.ml.tasktracker.tracking

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.jetbrains.research.ml.tasktracker.Plugin

fun Project.addBulkFileListener(bulkFileListener: BulkFileListener) {
    messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, bulkFileListener)
}

class TaskDocumentListener : BulkFileListener {
    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("${Plugin.PLUGIN_NAME}: init BulkFile listener")
    }

    /*override fun before(events: MutableList<out VFileEvent>) {
        logger.info("TaskDocumentListener events size: ${events.size}")
        events.forEach { event ->
            event.file?.let { virtualFile ->
                FileDocumentManager.getInstance().getDocument(virtualFile)?.let {
                    DocumentLogger.log(it) }
            }
        }
    }*/

    override fun after(events: MutableList<out VFileEvent>) {
        logger.info("TaskDocumentListener events size: ${events.size}")
        events.forEach { event ->
            event.file?.let { virtualFile ->
                FileDocumentManager.getInstance().getDocument(virtualFile)?.let {
                    //logger.info(it.text)
                    DocumentLogger.log(it)
                }
            }
        }
        super.after(events)
    }
}


/*
class TaskDocumentListener : DocumentListener {
    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("${Plugin.PLUGIN_NAME}: init documents listener")
    }
    */
/**
 * Tracking documents changes before to be consistent with activity-tracker plugin
 *//*

    override fun beforeDocumentChange(event: DocumentEvent) {
        */
/*if (isValidChange(event))*//*
 DocumentLogger.log(event.document)
    }

    */
/**
 * To avoid completion events with IntellijIdeaRulezzz sign.
 * TODO: Maybe we should remove it since we don't use EditorFactory
 *  to add listeners anymore (see https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000664264-IntelliJ-DocumentListener-gives-an-internally-inconsistent-event-stream)
 *//*

    private fun isValidChange(event: DocumentEvent): Boolean {
        return EditorFactory.getInstance().getEditors(event.document).isNotEmpty() && FileDocumentManager.getInstance()
            .getFile(event.document) != null
    }
}*/
