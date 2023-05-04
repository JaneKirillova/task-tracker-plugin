package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class UserInfo(
    val id: Int,
    @SerialName("user_group")
    val userGroup: Int, val taskOrder: List<Int>, val data: List<String>
)

