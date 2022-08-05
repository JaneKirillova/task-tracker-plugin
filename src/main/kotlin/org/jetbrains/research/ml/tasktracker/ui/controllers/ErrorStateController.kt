package org.jetbrains.research.ml.tasktracker.ui.controllers

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.jcef.JBCefJSQuery
import org.jetbrains.research.ml.tasktracker.ui.view.BrowserView

class ErrorStateController {
    private val logger: Logger = Logger.getInstance(javaClass)

    fun updateViewContent(view: BrowserView) {
        view.updateViewByUrl("http://tasktracker/ErrorPage.html")
    }

    fun setOnRefreshAction(view: BrowserView, action: (String) -> JBCefJSQuery.Response?) {
        view.executeJavascript(
            """
                                 var myButton = document.getElementById('refresh-button');
                                 myButton.onclick = function () {
                                 """, """}""", action = action
        )
    }

    fun setOnRefreshActionWithFailedSend(view: BrowserView, action: (String) -> JBCefJSQuery.Response?) {
        val path = PathManager.getPluginsPath().replace("/", "//").replace("\\", "\\\\")
        logger.info("TT results: $path")
        view.executeJavascript(
            """
                                 var textOutput = document.getElementById('text-output');
                                 textOutput.textContent = "If you have already completed the research, but there is a problem, please send the files to sergey.titov@jetbrains.com from the activity-tracker and tasktracker folder that you can find at the following path"
                                 var pathOutput = document.getElementById('path-output');
                                 pathOutput.textContent = "$path"
                                 var myButton = document.getElementById('refresh-button');
                                 myButton.onclick = function () {
                                 """, """}""", action = action
        )
    }
}
