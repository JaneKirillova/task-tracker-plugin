package org.jetbrains.research.ml.tasktracker.ui.controllers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.ml.tasktracker.models.UserData
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.BrowserView
import org.jetbrains.research.ml.tasktracker.ui.MainController


class SuccessViewController : ViewControllerInterface {
    private val logger: Logger = Logger.getInstance(javaClass)
    private val userData = UserData()
    var currentState = ViewState.GREETING

    override fun updateViewContent(view: BrowserView) {
        logger.info("View loaded with $currentState state")
        when (currentState) {
            ViewState.GREETING -> {
                view.updateViewByUrl("http://tasktracker/GreetingPane.html")
                setGreetingAction(view)
            }
            ViewState.QUESTIONS_FIRST -> {
                view.updateViewByUrl("http://tasktracker/QuestionsFirstPane.html")
                setQuestionsFirstAction(view)
            }
            ViewState.QUESTIONS_SECOND -> {
                view.updateViewByUrl("http://tasktracker/QuestionsSecondPane.html")
                setQuestionsSecondAction(view)
            }
            ViewState.TASK_SOLVING -> {
                view.updateViewByUrl("http://tasktracker/TaskSolvingPane.html")
            }
            ViewState.FEEDBACK -> {
                view.updateViewByUrl("http://tasktracker/FeedbackPane.html")
                setFeedbackAction(view)
            }
            ViewState.FINAL -> {
                view.updateViewByUrl("http://tasktracker/FinalPane.html")
            }
        }
    }

    private fun setGreetingAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var submitButton = document.getElementById('submit-button');
                            submitButton.onclick = function () {
                            if(checkInputFields()){
                            var nameField = document.getElementById('nameField').value;
                            var emailField = document.getElementById('emailField').value;
                            var userInfo = [nameField, emailField].join(',');
                            alert(userInfo);
                            """, """}}""", "userInfo"
        ) {
            val listOfUserData = it.split(',')
            userData.name = listOfUserData[0]
            userData.email = listOfUserData[1]
            currentState = ViewState.QUESTIONS_FIRST
            MainController.browserViews.forEach { viewFromController ->
                updateViewContent(viewFromController)
            }
            null
        }
    }

    private fun setQuestionsFirstAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var nextButton = document.getElementById('next-button');
                            nextButton.onclick = function () {
                            var elements = document.querySelectorAll('.question:checked');
                            var selectedVariants = Array.from(elements).map(element => element.value).join(',');
            """, """}""", "selectedVariants"
        ) {
            val listOfUserAnswers = it.split(',').map { answer -> answer.toInt() }
            userData.listOfAnswers += listOfUserAnswers
            currentState = ViewState.QUESTIONS_SECOND
            MainController.browserViews.forEach { viewFromController ->
                updateViewContent(viewFromController)
            }
            null
        }
    }

    private fun setQuestionsSecondAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var goButton = document.getElementById('go-button');
                            goButton.onclick = function () {
                            var elements = document.querySelectorAll('.question:checked');
                            var selectedVariants = Array.from(elements).map(element => element.value).join(',');
            """, """}""", "selectedVariants"
        ) {
            val listOfUserAnswers = it.split(',').map { answer -> answer.toInt() }
            userData.listOfAnswers += listOfUserAnswers
            logger.info("Received $userData from user")
            ApplicationManager.getApplication().invokeLater {
                TaskFileHandler.initProject(view.project)
                MainController.taskController.startSolvingNextTask(view.project)
            }
            currentState = ViewState.TASK_SOLVING
            MainController.browserViews.forEach { viewFromController ->
                updateViewContent(viewFromController)
            }
            null
        }
    }

    private fun setFeedbackAction(view: BrowserView) {
        view.executeJavascript(
            """
                            var submitButton = document.getElementById('submit-button');
                            submitButton.onclick = function () {
            """, """}"""
        ) {
            //send
            currentState = ViewState.FINAL
            MainController.browserViews.forEach { viewFromController ->
                updateViewContent(viewFromController)
            }
            null
        }
    }
}