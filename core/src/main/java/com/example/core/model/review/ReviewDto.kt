@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.core.model.review

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ReviewsResponse(
    val data: List<ReviewDto> = emptyList(),
    val reviews: List<ReviewDto> = emptyList(),
    val items: List<ReviewDto> = emptyList(),
    val results: List<ReviewDto> = emptyList()
)

@Serializable
data class ReviewDto(
    val id: String? = null,
    @SerialName("user_id")
    @JsonNames("userId")
    val userId: String? = null,
    @SerialName("book_id")
    @JsonNames("bookId")
    val bookId: String? = null,
    @SerialName("library_id")
    @JsonNames("libraryId")
    val libraryId: String? = null,
    @SerialName("borrowing_id")
    @JsonNames("borrowingId")
    val borrowingId: String? = null,
    val rating: Double? = null,
    val comment: String? = null,
    @SerialName("created_at")
    @JsonNames("createdAt")
    val createdAt: String? = null,
    @SerialName("updated_at")
    @JsonNames("updatedAt")
    val updatedAt: String? = null,
    val user: ReviewUserDto? = null,
    val reviewer: ReviewUserDto? = null
)

@Serializable
data class ReviewUserDto(
    val id: String? = null,
    val name: String? = null,
    @JsonNames("displayName", "display_name", "fullName", "full_name")
    val displayName: String? = null,
    val email: String? = null
)

@Serializable
data class CreateReviewRequest(
    @SerialName("borrowing_id")
    val borrowingId: String,
    val rating: Int,
    val comment: String
)

fun ReviewDto.toDomain(): Review {
    val reviewer = user ?: reviewer
    val reviewerName = reviewer?.displayName
        ?: reviewer?.name
        ?: reviewer?.email?.substringBefore("@")

    return Review(
        id = id.orEmpty(),
        borrowingId = borrowingId,
        rating = rating,
        comment = comment,
        createdAt = createdAt,
        reviewerName = reviewerName
    )
}
