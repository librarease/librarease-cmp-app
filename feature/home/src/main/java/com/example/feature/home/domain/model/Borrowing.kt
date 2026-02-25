package com.example.feature.home.domain.model

data class Borrowing(
    val id: String,
    val book: BorrowedBook,
    val bookId: String,
    val borrowedAt: String?,
    val createdAt: String?,
    val dueAt: String?,
    val updatedAt: String?,
    val staffId: String?,
    val subscriptionId: String?,
    val returning: Returning?
)
