package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable

interface Keyed {
    val key: String
}

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class PaneLanguage(override val key: String) : Keyed

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class ProgrammingLanguage(override val key: String) : Keyed {
    fun getLanguage() = Language.values().find { it.key == key }
}

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Gender(
    override val key: String,
    val translation: Map<PaneLanguage, String>
) : Keyed

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Country(
    override val key: String,
    val translation: Map<PaneLanguage, String>
) : Keyed