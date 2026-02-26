@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.feature.home.data.model.subscription_model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SubscriptionListResponse(
    @JsonNames("data", "subscriptions", "items")
    val subscriptions: List<SubscriptionDto>? = null,
    val meta: MetaDto? = null
)

@Serializable
data class SubscriptionDto(
    val id: String? = null,
    @SerialName("user_id")
    @JsonNames("userId")
    val userId: String? = null,
    @SerialName("membership_id")
    @JsonNames("membershipId")
    val membershipId: String? = null,
    @SerialName("created_at")
    @JsonNames("createdAt", "createdDate")
    val createdDate: String? = null,
    val user: UserDto? = null,
    val membership: MembershipDto? = null,
    @SerialName("expires_at")
    @JsonNames("expiresAt", "expireDate")
    val expireDate: String? = null,
    val amount: Int? = null,
    @SerialName("fine_per_day")
    @JsonNames("finePerDay")
    val finePerDay: Int? = null,
    @SerialName("loan_period")
    @JsonNames("loanPeriod")
    val loanPeriod: Int? = null,
    @SerialName("active_loan_limit")
    @JsonNames("activeLoanLimit")
    val activeLoanLimit: Int? = null
)

@Serializable
data class MembershipDto(
    val id: String? = null,
    val name: String? = null,
    @SerialName("library_id")
    @JsonNames("libraryId")
    val libraryId: String? = null,
    val library: LibraryDto? = null
)

@Serializable
data class LibraryDto(
    val id: String? = null,
    val name: String? = null
)

@Serializable
data class UserDto(
    val id: String? = null,
    val name: String? = null
)

@Serializable
data class MetaDto(
    val total: Int? = null,
    val skip: Int? = null,
    val limit: Int? = null
)
