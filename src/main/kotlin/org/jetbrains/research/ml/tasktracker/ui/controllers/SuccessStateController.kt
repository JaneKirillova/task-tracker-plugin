package org.jetbrains.research.ml.tasktracker.ui.controllers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.ml.tasktracker.models.PaneLanguage
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.util.SurveyData
import org.jetbrains.research.ml.tasktracker.ui.util.getSurveyFactors
import org.jetbrains.research.ml.tasktracker.ui.util.getSurveyPage
import org.jetbrains.research.ml.tasktracker.ui.view.BrowserView


class SuccessStateController {
    private val logger: Logger = Logger.getInstance(javaClass)

    fun updateViewContent(view: BrowserView) {
        logger.info("View loaded with ${view.state} state")
        when (view.state) {
            ViewState.GREETING -> {
                view.updateViewByUrl("http://tasktracker/GreetingPage.html")
                setGreetingAction(view)
            }
            ViewState.QUESTIONS_FIRST -> {
                PluginServer.paneText?.let {
                    val listOfQuestions = it.surveyPane[PaneLanguage("en")]?.questions
                    view.updateViewByHtml(
                        getSurveyPage(
                            "Survey: first part", listOfQuestions?.subList(0, listOfQuestions.size / 2) ?: emptyList()
                        ), "http://tasktracker/QuestionsFirstPage.html"
                    )
                    setQuestionsFirstAction(view)
                }
            }
            ViewState.QUESTIONS_SECOND -> {
                PluginServer.paneText?.let {
                    val listOfQuestions = it.surveyPane[PaneLanguage("en")]?.questions
                    view.updateViewByHtml(
                        getSurveyPage(
                            "Survey: second part",
                            listOfQuestions?.subList(listOfQuestions.size / 2, listOfQuestions.size) ?: emptyList()
                        ), "http://tasktracker/QuestionsSecondPage.html"
                    )
                    setQuestionsSecondAction(view)
                }
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
                setFinalAction(view)
            }
        }
    }

    private fun setGreetingAction(view: BrowserView) {
        view.executeJavascript(
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
            SurveyData.name = listOfUserData[0]
            SurveyData.email = listOfUserData[1]
            view.state = ViewState.QUESTIONS_FIRST
            updateViewContent(view)
            null
        }
    }

    private fun setQuestionsFirstAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var nextButton = document.getElementById('next-button');
                            nextButton.onclick = function () {
                            if (checkSurvey()) {
                            var elements = document.querySelectorAll('.question:checked');
                            var selectedVariants = Array.from(elements).map(element => element.value).join(',');
            """, """}}""", "selectedVariants"
        ) {
            val listOfUserAnswers = it.split(',').map { answer -> answer.toInt() }
            SurveyData.form += listOfUserAnswers
            view.state = ViewState.QUESTIONS_SECOND
            updateViewContent(view)
            null
        }
    }

    private fun setQuestionsSecondAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var nextButton = document.getElementById('next-button');
                            nextButton.onclick = function () {
                            if (checkSurvey()) {
                            var elements = document.querySelectorAll('.question:checked');
                            var selectedVariants = Array.from(elements).map(element => element.value).join(',');
            """, """}}""", "selectedVariants"
        ) {
            val listOfUserAnswers = it.split(',').map { answer -> answer.toInt() }
            SurveyData.form += listOfUserAnswers
            logger.info("Received survey info: name=${SurveyData.name}, email=${SurveyData.email}, form=${SurveyData.form}")
            PluginServer.reconnectUserId(view.project)
            view.state = ViewState.PRE_TASK_SOLVING
            updateViewContent(view)
            null
        }
    }

    private fun setPreSolvingAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var goButton = document.getElementById('go-button');
                            goButton.onclick = function () {
            """, """}"""
        ) {
            ApplicationManager.getApplication().invokeLater {
                TaskFileHandler.initProject(view.project)
            }
            view.taskController.executeIdeAction("HideAllWindows")
            view.taskController.startNextTask()
            view.state = ViewState.TASK_SOLVING
            updateViewContent(view)
            null
        }
    }

    private fun setFeedbackAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var submitButton = document.getElementById('submit-button');
                            submitButton.onclick = function () {
                                var feedback = document.getElementById('tricksField');
                                var feedbackText = feedback.value;
            """, """}""", "feedbackText"
        ) {
            SurveyData.feedback = it
            PluginServer.sendFeedback(it, view.project)
            view.state = ViewState.FINAL
            updateViewContent(view)
            null
        }
    }

    private fun setFinalAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var outputField = document.getElementById('factors-output');
                            outputField.textContent = "${getSurveyFactors(SurveyData.form).joinToString(", ")}"
            """, "", ""
        ) { null }
    }
}