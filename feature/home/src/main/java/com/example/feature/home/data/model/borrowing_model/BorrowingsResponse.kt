@file:OptIn(ExperimentalSerializationApi::class)

package com.example.feature.home.data.model.borrowing_model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BorrowingsResponse(
    @JsonNames("borrowings", "data", "items")
    val borrowings: List<BorrowingDto> = emptyList()
)
