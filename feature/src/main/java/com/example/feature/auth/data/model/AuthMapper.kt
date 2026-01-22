package com.example.feature.auth.data.model

import com.example.feature.auth.domain.model.User

fun AuthResponse.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
