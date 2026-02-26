package com.example.feature.home.domain.model

import com.example.core.model.book.BookSummary

data class Borrowing(
    val id: String,
    val book: BookSummary,
    val bookId: String,
    val borrowedAt: String?,
    val createdAt: String?,
    val dueAt: String?,
    val updatedAt: String?,
    val staffId: String?,
    val subscriptionId: String?,
    val returning: Returning?
)
