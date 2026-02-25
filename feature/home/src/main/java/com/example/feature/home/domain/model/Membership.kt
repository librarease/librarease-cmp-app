package com.example.feature.home.domain.model

data class Membership(
    val id: String,
    val name: String,
    val tier: String,
    val status: String,
    val expiresAt: String?
)
