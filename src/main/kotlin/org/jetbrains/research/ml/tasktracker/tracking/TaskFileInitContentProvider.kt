package org.jetbrains.research.ml.tasktracker.tracking

import org.jetbrains.research.ml.tasktracker.models.Language
import org.jetbrains.research.ml.tasktracker.models.Task

object TaskFileInitContentProvider {
    const val PLUGIN_FOLDER = "tasktracker"

    fun getLanguageFolderRelativePath(language: Language): String {
        return "${PLUGIN_FOLDER}/${language.name.toLowerCase()}"
    }

    private fun getTaskComment(task: Task, language: Language): String {
        val commonCommentPart = "Write code for the ${task.key} task in this file\n\n"
        return when (language) {
            Language.PYTHON -> "# $commonCommentPart"
            Language.CPP -> "// ${commonCommentPart}To run this file add it to MAKE or CMAKE file in your project\n\n"
            // Default case, because in most languages is used // in comments
            else -> "// $commonCommentPart"
        }
    }

    private fun getPackage(language: Language): String {
        val currentPackage = "package ${PLUGIN_FOLDER}.${language.name.toLowerCase()}"
        return when (language) {
            Language.JAVA -> "$currentPackage;\n\n"
            Language.KOTLIN -> "$currentPackage\n\n"
            else -> ""
        }
    }

    private fun getClassNameByTask(task: Task): String {
        return "${task.key.capitalize()}Class"
    }

    fun getTaskFileName(task: Task, language: Language): String {
        return when (language) {
            Language.JAVA -> "${getClassNameByTask(task)}${language.extension.ext}"
            Language.KOTLIN -> "${task.key.capitalize()}${language.extension.ext}"
            else -> "${task.key}${language.extension.ext}"
        }
    }

    fun getInitFileContent(task: Task, language: Language): String {
        val comment = getTaskComment(task, language)
        return when (language) {
            //TODO: print condition at not only jupyter
            Language.JUPYTER -> {
                //Mongoose database does not allow to store keys with dots
                task.description.toString().replace("___",".")
            }
            Language.JAVA -> comment + "public class ${getClassNameByTask(task)} {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        // Write your code here\n" +
                    "    }" +
                    "\n}"
            Language.KOTLIN -> comment +
                    "fun main() {\n" +
                    "    // Write tour code here\n" +
                    "}"
            Language.CPP -> "$comment#include <iostream>\n" +
                    "\n" +
                    "int main() \n" +
                    "{ \n" +
                    "    // Write your code here\n" +
                    "    return 0; \n" +
                    "}"
            Language.PYTHON -> "$comment# Write your code here"
        }
    }
}