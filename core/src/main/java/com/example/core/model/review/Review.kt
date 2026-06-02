package com.example.core.model.review

data class Review(
    val id: String,
    val borrowingId: String?,
    val rating: Double?,
    val comment: String?,
    val createdAt: String?,
    val reviewerName: String?
)
