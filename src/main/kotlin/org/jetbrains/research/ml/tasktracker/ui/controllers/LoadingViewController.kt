package org.jetbrains.research.ml.tasktracker.ui.controllers

import org.jetbrains.research.ml.tasktracker.ui.BrowserView

class LoadingViewController : ViewControllerInterface {
    override fun updateViewContent(view: BrowserView) {
        view.updateViewByUrl("http://tasktracker/LoadingPage.html")
    }
}