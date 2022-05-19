package org.jetbrains.research.ml.tasktracker.ui.controllers

import com.intellij.ui.jcef.JBCefJSQuery
import org.jetbrains.research.ml.tasktracker.ui.BrowserView

class ErrorViewController {
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
}