package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class SurveyPaneText(
    val age: String,
    val gender: String,
    val experience: String,
    val country: String,
    val years: String,
    val months: String,
    val startSession: String,
    val programmingLanguage: String
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class FinalPaneText(
    val praise: String,
    val backToSurvey: String,
    val finalMessage: String,
    val backToTasks: String
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class TaskChoosingPaneText(
    val chooseTask: String,
    val finishSession: String,
    val startSolving: String,
    val description: String
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class TaskSolvingPaneText(
    val inputData: String,
    val outputData: String,
    val submit: String,
    val backToTasks: String,
    val hint: String
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class SuccessPaneText(
    val backToTasks: String,
    val successMessage: String
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class PaneText(
    val surveyPane: Map<PaneLanguage, SurveyPaneText>,
    val taskChoosingPane: Map<PaneLanguage, TaskChoosingPaneText>,
    val taskSolvingPane: Map<PaneLanguage, TaskSolvingPaneText>,
    val finalPane: Map<PaneLanguage, FinalPaneText>,
    val successPane: Map<PaneLanguage, SuccessPaneText>
)