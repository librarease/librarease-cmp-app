@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MembershipDto(
    val id: String = "",
    val name: String = "",
    val tier: String? = null,
    val status: String? = null,
    @SerialName("expires_at")
    @JsonNames("expiresAt")
    val expiresAt: String? = null
)
