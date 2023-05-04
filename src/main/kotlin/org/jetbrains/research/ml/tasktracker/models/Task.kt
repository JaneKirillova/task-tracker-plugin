package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Settings(val actionsToToggle: List<String>, val parameters: Map<String, String>)











@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Task(
    val key: Int,
    val id: Int = -1,
    val isExperimental: Boolean,
    val description: String,
    val ideSettings: Settings? = null
)
