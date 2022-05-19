package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class SurveyPaneText(val questions: List<String>)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class PaneText(
    val surveyPane: Map<PaneLanguage, SurveyPaneText>,
)