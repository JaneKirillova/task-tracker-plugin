package org.jetbrains.research.ml.tasktracker.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery

class ShowingPanel : SimpleToolWindowPanel(true, true),
    Disposable {

    private val jbCefBrowser: JBCefBrowser = JBCefBrowser()

    init {
        setContent(jbCefBrowser.component)
        jbCefBrowser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 10)
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
        Thread.sleep(1000)
        jbCefBrowser.cefBrowser.executeJavaScript(
            (codeBeforeInject + """
                        ${jsQueryGetSelectedProjects.inject(queryResult)};
                    """+codeAfterInject).trimIndent(), cefBrowser.url, 0
        )
    }

    fun updatePanel(html: String) {
        jbCefBrowser.loadHTML(html)
    }

    override fun dispose() {
        jbCefBrowser.dispose()
    }
}
