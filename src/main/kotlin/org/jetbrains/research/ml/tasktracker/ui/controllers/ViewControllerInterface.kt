package org.jetbrains.research.ml.tasktracker.ui.controllers

import org.jetbrains.research.ml.tasktracker.ui.BrowserView

interface ViewControllerInterface {
    fun updateViewContent(view: BrowserView)
}