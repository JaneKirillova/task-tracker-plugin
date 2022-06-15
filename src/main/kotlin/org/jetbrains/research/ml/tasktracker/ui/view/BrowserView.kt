package org.jetbrains.research.ml.tasktracker.ui.view

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
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.controllers.TaskSolvingController
import org.jetbrains.research.ml.tasktracker.ui.controllers.ViewState
import org.jetbrains.research.ml.tasktracker.ui.util.CustomSchemeHandlerFactory

/**
 * Stores jbCefBrowser and project in which it was created
 */
class BrowserView(val project: Project) : SimpleToolWindowPanel(true, true) {
    var state = ViewState.GREETING
    val taskController by lazy {
        TaskSolvingController(PluginServer.tasks, this)
    }
    private var currentCefLoadHandler: CefLoadHandler? = null
    private val jbCefBrowser: JBCefBrowser = object : JBCefBrowser() {
        override fun dispose() {
            MainController.browserViews.remove(project)
            //Returns to the user properties if there are no other windows in the TASK_SOLVING state
            if(MainController.browserViews.filter { it.value.state == ViewState.TASK_SOLVING }.isEmpty()){
                taskController.returnToUserProperties()
            }
            super.dispose()
        }
    }

    init {
        jbCefBrowser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 100)
        registerAppSchemeHandler()
        Disposer.register(project, jbCefBrowser)
        setContent(jbCefBrowser.component)
    }


    private fun registerAppSchemeHandler() {
        CefApp.getInstance().registerSchemeHandlerFactory(
            "http", "tasktracker", CustomSchemeHandlerFactory()
        )
    }

    /**
     * Executes javascript at page after it is loaded
     */
    fun executeJavascript(
        codeBeforeInject: String = "",
        codeAfterInject: String = "",
        queryResult: String = "",
        action: (String) -> JBCefJSQuery.Response?
    ) {
        val jbCefJSQuery = JBCefJSQuery.create(jbCefBrowser as JBCefBrowserBase)
        jbCefJSQuery.addHandler(action)
        currentCefLoadHandler = object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                jbCefBrowser.cefBrowser.executeJavaScript(
                    (codeBeforeInject + """
                        ${jbCefJSQuery.inject(queryResult)};
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
