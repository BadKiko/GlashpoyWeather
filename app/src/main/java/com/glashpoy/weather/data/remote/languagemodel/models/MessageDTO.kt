package com.glashpoy.weather.data.remote.languagemodel.models

import com.squareup.moshi.JsonClass

/**
 * A class that represents a single message object that is being transmitted between the
 * large language model API and the local HTTP client.
 */
@JsonClass(generateAdapter = true)
data class MessageDTO(
    val role: String,
    val content: String
) {
    object Roles {
        const val SYSTEM = "system"
        const val USER = "user"
    }
}