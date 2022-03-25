package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
class TaskSolvingErrorText(
    val header: String,
    val description: String
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class TaskSolvingErrorDialogText(
    val translation: Map<PaneLanguage, TaskSolvingErrorText>
)

