@file:OptIn(ExperimentalSerializationApi::class)

package com.example.feature.home.data.model.bood_model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BookDto(
    val id: String = "",
    val title: String = "",
    @JsonNames("author_name")
    val author: String? = null,
    val year: Int? = null,
    val code: String? = null,
    @SerialName("cover")
    @JsonNames(
        "cover_url",
        "coverUrl",
        "image_url",
        "imageUrl",
        "thumbnail_url",
        "thumbnailUrl"
    )
    val coverUrl: String? = null,
    val colors: BookColorsDto? = null,
    @SerialName("library_id")
    @JsonNames("libraryId")
    val libraryId: String? = null
)
