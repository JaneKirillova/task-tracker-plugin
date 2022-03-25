package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class StoredInfo(
    var loggedUIData: Map<String, String> = mapOf(),
    var userId: String? = null
)