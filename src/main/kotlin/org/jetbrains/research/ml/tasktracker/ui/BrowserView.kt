package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.CefApp
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState
import org.jetbrains.research.ml.tasktracker.ui.util.CustomSchemeHandlerFactory

class BrowserView(val project: Project) : SimpleToolWindowPanel(true, true) {

    var currentState = ViewState.GREETING
    private var currentCefLoadHandler: CefLoadHandler? = null
    private val jbCefBrowser: JBCefBrowser = JBCefBrowser()

    init {
        jbCefBrowser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 10)
        registerAppSchemeHandler()
        Disposer.register(project, jbCefBrowser)
        setContent(jbCefBrowser.component)
    }

    private fun registerAppSchemeHandler() {
        CefApp.getInstance().registerSchemeHandlerFactory(
            "http", "tasktracker", CustomSchemeHandlerFactory()
        )
    }

    fun executeJavascript(
        codeBeforeInject: String = "",
        codeAfterInject: String = "",
        queryResult: String = "",
        action: (String) -> JBCefJSQuery.Response?
    ) {
        val jsQueryGetSelectedProjects = JBCefJSQuery.create(jbCefBrowser as JBCefBrowserBase)
        jsQueryGetSelectedProjects.addHandler(action)

        currentCefLoadHandler = object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                jbCefBrowser.cefBrowser.executeJavaScript(
                    (codeBeforeInject + """
                        ${jsQueryGetSelectedProjects.inject(queryResult)};
                    """ + codeAfterInject).trimIndent(), jbCefBrowser.cefBrowser.url, 0
                )
                super.onLoadEnd(browser, frame, httpStatusCode)
            }
        }

        currentCefLoadHandler?.let {
            jbCefBrowser.jbCefClient.addLoadHandler(it, jbCefBrowser.cefBrowser)
        }
    }

    fun updateViewByUrl(url: String) {
        currentCefLoadHandler?.let {
            jbCefBrowser.jbCefClient.removeLoadHandler(it, jbCefBrowser.cefBrowser)
        }
        jbCefBrowser.loadURL(url)
    }

    fun updateViewByHtml(html: String, dummyUrl: String = "") {
        currentCefLoadHandler?.let {
            jbCefBrowser.jbCefClient.removeLoadHandler(it, jbCefBrowser.cefBrowser)
        }
        jbCefBrowser.loadHTML(html, dummyUrl)
    }
}
