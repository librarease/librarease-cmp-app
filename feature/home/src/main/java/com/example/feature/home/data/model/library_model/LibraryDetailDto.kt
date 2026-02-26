@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.feature.home.data.model.library_model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class LibraryDetailResponse(
    @JsonNames("data")
    val data: LibraryDetailDto? = null
)

@Serializable
data class LibraryDetailDto(
    val id: String = "",
    val name: String = "",
    val logo: String? = null,
    val address: String? = null,
    @SerialName("created_at")
    @JsonNames("createdAt")
    val createdAt: String? = null,
    @SerialName("updated_at")
    @JsonNames("updatedAt")
    val updatedAt: String? = null
)
