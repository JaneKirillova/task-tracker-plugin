package org.jetbrains.research.ml.tasktracker.tracking

import com.intellij.openapi.diagnostic.Logger
import krangl.DataFrame
import krangl.map
import krangl.readCSV
import krangl.writeCSV
import org.apache.commons.csv.CSVFormat
import org.jetbrains.research.ml.tasktracker.Plugin.PLUGIN_NAME
import org.jetbrains.research.ml.tasktracker.models.Extension
import org.jetbrains.research.ml.tasktracker.models.Language
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileInitContentProvider.PLUGIN_FOLDER
import java.io.File
import java.io.FileNotFoundException

enum class ActivityTrackerColumn {
    TIMESTAMP, USERNAME, EVENT_TYPE, EVENT_DATA, PROJECT_NAME, FOCUSED_COMPONENT,
    CURRENT_FILE, PSI_PATH, EDITOR_LINE, EDITOR_COLUMN
}

object ActivityTrackerFileHandler {

    private const val ACTIVITY_TRACKER_FILE_NAME = "ide-events"
    private const val DEFAULT_PATH_SYMBOL = "*"
    private val logger: Logger = Logger.getInstance(javaClass)

    fun filterActivityTrackerData(filePath: String): String? {
        return try {
            val df = DataFrame.readCSV(
                filePath,
                format = CSVFormat.DEFAULT.withHeader(ActivityTrackerColumn::class.java)
            )
            val filteredDf = filterDataFrame(df)
            val resultPath = filePath.replace(ACTIVITY_TRACKER_FILE_NAME, "${ACTIVITY_TRACKER_FILE_NAME}_filtered")
            filteredDf.writeCSV(File(resultPath), format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true))
            resultPath
        } catch (e: FileNotFoundException) {
            logger.info("${PLUGIN_NAME}: The activity tracker file $filePath does not exist")
            null
        }
    }

    private fun filterDataFrame(df: DataFrame): DataFrame {
        // Remove columns, which can contain private information
        val anonymousDf = df.remove(
            ActivityTrackerColumn.USERNAME.name, ActivityTrackerColumn.PROJECT_NAME.name,
            ActivityTrackerColumn.PSI_PATH.name
        )
        return clearFilesPaths(anonymousDf)
    }

    // Return the default symbol, if the file is not from the plugin files and only filename from the path otherwise
    private fun replaceAbsoluteFilePath(path: String, pluginFilesRegex: Regex): String {
        // Try to find plugin's tasks files
        val found = pluginFilesRegex.find(path)
        return if (found != null) {
            // Get name from path
            path.split("/").last()
        } else {
            DEFAULT_PATH_SYMBOL
        }
    }

    private fun clearFilesPaths(df: DataFrame): DataFrame {
        val tasks = PluginServer.tasks.joinToString(separator = "|") { it.key.toString() }
        val languages = Language.values().joinToString(separator = "|") { it.name.toLowerCase() }
        val extensions = Extension.values().joinToString(separator = "|") { it.ext.toLowerCase() }
        val tasksMatchCondition = ".*/$PLUGIN_FOLDER/($languages)/($tasks)($extensions)".toRegex(
            RegexOption.IGNORE_CASE
        )
        return df.addColumn(ActivityTrackerColumn.CURRENT_FILE.name) { filePath ->
            filePath[ActivityTrackerColumn.CURRENT_FILE.name].map<String> {
                replaceAbsoluteFilePath(it, tasksMatchCondition)
            }
        }
    }
}