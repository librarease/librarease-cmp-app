package com.example.feature.home.domain.model

data class BorrowedBook(
    val id: String,
    val title: String,
    val author: String,
    val year: Int?,
    val code: String?,
    val coverUrl: String?,
    val colors: BookPalette?
)
