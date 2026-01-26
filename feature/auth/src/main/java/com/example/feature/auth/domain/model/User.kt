package com.example.feature.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String
)
