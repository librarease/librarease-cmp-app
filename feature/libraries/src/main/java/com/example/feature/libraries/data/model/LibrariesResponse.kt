@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.feature.libraries.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class LibrariesResponse(
    @JsonNames("data", "libraries", "items")
    val libraries: List<LibraryItemDto> = emptyList()
)

@Serializable
data class LibraryItemDto(
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
