package com.example.feature.home.domain.model

data class Returning(
    val id: String,
    val borrowingId: String?,
    val fine: Int,
    val returnedAt: String?,
    val staffId: String?
)
