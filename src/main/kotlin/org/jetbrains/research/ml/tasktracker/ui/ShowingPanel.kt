package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.CefApp
import org.jetbrains.research.ml.tasktracker.ui.util.CustomSchemeHandlerFactory

class ShowingPanel(project: Project) : SimpleToolWindowPanel(true, true) {

    private val jbCefBrowser: JBCefBrowser = JBCefBrowser()

    init {
        registerAppSchemeHandler()
        Disposer.register(project, jbCefBrowser)
        setContent(jbCefBrowser.component)
        jbCefBrowser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 10)
    }

    private fun registerAppSchemeHandler() {
        CefApp
            .getInstance()
            .registerSchemeHandlerFactory(
                "http",
                "tasktracker",
                CustomSchemeHandlerFactory()
            )
    }


    fun executeJavascript(
        codeBeforeInject: String = "",
        codeAfterInject: String = "",
        queryResult: String = "",
        action: (String) -> JBCefJSQuery.Response?
    ) {
        val jsQueryGetSelectedProjects = JBCefJSQuery.create(jbCefBrowser)
        jsQueryGetSelectedProjects.addHandler(action)
        val cefBrowser = jbCefBrowser.cefBrowser
        //TODO javascript faster than html loading
        Thread.sleep(1000)
        jbCefBrowser.cefBrowser.executeJavaScript(
            (codeBeforeInject + """
                        ${jsQueryGetSelectedProjects.inject(queryResult)};
                    """ + codeAfterInject).trimIndent(), cefBrowser.url, 0
        )
    }

    fun updatePanel(url: String) {
        jbCefBrowser.loadURL(url)
    }
}
