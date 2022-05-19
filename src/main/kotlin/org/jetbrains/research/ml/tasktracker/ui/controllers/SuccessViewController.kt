package org.jetbrains.research.ml.tasktracker.ui.controllers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.ml.tasktracker.models.PaneLanguage
import org.jetbrains.research.ml.tasktracker.models.UserData
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.BrowserView
import org.jetbrains.research.ml.tasktracker.ui.MainController
import org.jetbrains.research.ml.tasktracker.ui.util.HtmlGenerator


class SuccessViewController {
    private val logger: Logger = Logger.getInstance(javaClass)
    val userData = UserData()
    var currentState = ViewState.GREETING

    fun updateViewContent(view: BrowserView) {
        logger.info("View loaded with $currentState state")
        when (currentState) {
            ViewState.GREETING -> {
                view.updateViewByUrl("http://tasktracker/GreetingPage.html")
                setGreetingAction(view)
            }
            ViewState.QUESTIONS_FIRST -> {
                PluginServer.paneText?.let {
                    val listOfQuestions = it.surveyPane[PaneLanguage("en")]?.questions
                    view.updateViewByHtml(
                        HtmlGenerator.getSurveyPage(
                            "Survey: first part", listOfQuestions?.subList(0, listOfQuestions.size / 2) ?: emptyList()
                        ), "http://tasktracker/QuestionsFirstPage.html"
                    )
                    setQuestionsFirstAction(view)
                } ?: run { MainController.errorViewController.updateViewContent(view) }
            }
            ViewState.QUESTIONS_SECOND -> {
                PluginServer.paneText?.let {
                    val listOfQuestions = it.surveyPane[PaneLanguage("en")]?.questions
                    view.updateViewByHtml(
                        HtmlGenerator.getSurveyPage(
                            "Survey: second part",
                            listOfQuestions?.subList(listOfQuestions.size / 2, listOfQuestions.size) ?: emptyList()
                        ), "http://tasktracker/QuestionsSecondPage.html"
                    )
                    setQuestionsSecondAction(view)
                } ?: run { MainController.errorViewController.updateViewContent(view) }
            }
            ViewState.PRE_TASK_SOLVING -> {
                view.updateViewByUrl("http://tasktracker/PreSolvingPage.html")
                setPreSolvingAction(view)
            }
            ViewState.TASK_SOLVING -> {
                view.updateViewByUrl("http://tasktracker/SolvingPage.html")
            }
            ViewState.FEEDBACK -> {
                view.updateViewByUrl("http://tasktracker/FeedbackPage.html")
                setFeedbackAction(view)
            }
            ViewState.FINAL -> {
                view.updateViewByUrl("http://tasktracker/FinalPage.html")
            }
        }
    }

    private fun setGreetingAction(currentView: BrowserView) {
        currentView.executeJavascript(
            """
                            var submitButton = document.getElementById('submit-button');
                            submitButton.onclick = function () {
                            if (checkInputFields()) {
                            var nameField = document.getElementById('nameField').value;
                            var emailField = document.getElementById('emailField').value;
                            var userInfo = [nameField, emailField].join(',');
                            """, """}}""", "userInfo"
        ) {
            val listOfUserData = it.split(',')
            userData.name = listOfUserData[0]
            userData.email = listOfUserData[1]
            currentState = ViewState.QUESTIONS_FIRST
            MainController.browserViews.forEach { browserView ->
                updateViewContent(browserView)
            }
            null
        }
    }

    private fun setQuestionsFirstAction(currentView: BrowserView) {
        currentView.executeJavascript(
            """
                            var nextButton = document.getElementById('next-button');
                            nextButton.onclick = function () {
                            if (checkSurvey()) {
                            var elements = document.querySelectorAll('.question:checked');
                            var selectedVariants = Array.from(elements).map(element => element.value).join(',');
            """, """}}""", "selectedVariants"
        ) {
            val listOfUserAnswers = it.split(',').map { answer -> answer.toInt() }
            userData.listOfAnswers += listOfUserAnswers
            currentState = ViewState.QUESTIONS_SECOND
            MainController.browserViews.forEach { browserView ->
                updateViewContent(browserView)
            }
            null
        }
    }

    private fun setQuestionsSecondAction(currentView: BrowserView) {
        currentView.executeJavascript(
            """
                            var nextButton = document.getElementById('next-button');
                            nextButton.onclick = function () {
                            if (checkSurvey()) {
                            var elements = document.querySelectorAll('.question:checked');
                            var selectedVariants = Array.from(elements).map(element => element.value).join(',');
            """, """}}""", "selectedVariants"
        ) {
            val listOfUserAnswers = it.split(',').map { answer -> answer.toInt() }
            userData.listOfAnswers += listOfUserAnswers
            logger.info("Received $userData from user")
            currentState = ViewState.PRE_TASK_SOLVING
            MainController.browserViews.forEach { browserView ->
                updateViewContent(browserView)
            }
            null
        }
    }

    private fun setPreSolvingAction(currentView: BrowserView) {
        currentView.executeJavascript(
            """
                            var goButton = document.getElementById('go-button');
                            goButton.onclick = function () {
            """, """}"""
        ) {
            currentState = ViewState.TASK_SOLVING
            ApplicationManager.getApplication().invokeLater {
                TaskFileHandler.initProject(currentView.project)
            }
            MainController.taskController.executeIdeAction("HideAllWindows")
            MainController.taskController.startSolvingNextTask(currentView.project)
            MainController.browserViews.forEach { browserView ->
                updateViewContent(browserView)
            }
            null
        }
    }

    private fun setFeedbackAction(currentView: BrowserView) {
        currentView.executeJavascript(
            """
                            var submitButton = document.getElementById('submit-button');
                            submitButton.onclick = function () {
            """, """}"""
        ) {
            //TODO TO DO!!!
            currentState = ViewState.FINAL
            MainController.browserViews.forEach { browserView ->
                updateViewContent(browserView)
            }
            null
        }
    }
}
