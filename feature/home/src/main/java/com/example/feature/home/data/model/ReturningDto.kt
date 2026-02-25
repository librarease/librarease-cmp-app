@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ReturningDto(
    val id: String = "",
    @SerialName("borrowing_id")
    @JsonNames("borrowingId")
    val borrowingId: String? = null,
    val fine: Int? = null,
    @SerialName("returned_at")
    @JsonNames("returnedAt")
    val returnedAt: String? = null,
    @SerialName("staff_id")
    @JsonNames("staffId")
    val staffId: String? = null
)
