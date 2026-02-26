@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.core.model.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BookDetailResponse(
    @JsonNames("data")
    val data: BookDetailDto? = null
)

@Serializable
data class BookDetailDto(
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
    val libraryId: String? = null,
    val description: String? = null,
    @SerialName("created_at")
    @JsonNames("createdAt")
    val createdAt: String? = null,
    @SerialName("updated_at")
    @JsonNames("updatedAt")
    val updatedAt: String? = null,
    val library: LibraryDto? = null,
    val stats: BookStatsDto? = null
)

@Serializable
data class LibraryDto(
    val id: String = "",
    val name: String = "",
    val logo: String? = null,
    @SerialName("created_at")
    @JsonNames("createdAt")
    val createdAt: String? = null,
    @SerialName("updated_at")
    @JsonNames("updatedAt")
    val updatedAt: String? = null
)

@Serializable
data class BookStatsDto(
    @SerialName("borrow_count")
    @JsonNames("borrowCount")
    val borrowCount: Int? = null,
    @SerialName("review_count")
    @JsonNames("reviewCount")
    val reviewCount: Int? = null,
    val rating: Double? = null
)
