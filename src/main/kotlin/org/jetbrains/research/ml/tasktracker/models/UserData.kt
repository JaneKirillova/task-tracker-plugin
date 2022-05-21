package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class UserData(
    var name: String? = null,
    var email: String? = null,
    var listOfAnswers: List<Int> = emptyList(),
    var feedback: String? = null
)
