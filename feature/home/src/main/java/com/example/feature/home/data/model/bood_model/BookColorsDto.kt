@file:OptIn(ExperimentalSerializationApi::class)

package com.example.feature.home.data.model.bood_model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BookColorsDto(
    val muted: HslColorDto? = null,
    val vibrant: HslColorDto? = null,
    @SerialName("dark_muted")
    @JsonNames("darkMuted")
    val darkMuted: HslColorDto? = null,
    @SerialName("light_muted")
    @JsonNames("lightMuted")
    val lightMuted: HslColorDto? = null,
    @SerialName("dark_vibrant")
    @JsonNames("darkVibrant")
    val darkVibrant: HslColorDto? = null,
    @SerialName("light_vibrant")
    @JsonNames("lightVibrant")
    val lightVibrant: HslColorDto? = null
)

@Serializable
data class HslColorDto(
    val h: Float? = null,
    val s: Float? = null,
    val l: Float? = null,
    val space: String? = null
)
