@file:OptIn(ExperimentalSerializationApi::class)

package com.example.feature.home.data.model.borrowing_model

import com.example.feature.home.data.model.borrowing_model.ReturningDto
import com.example.feature.home.data.model.bood_model.BookDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BorrowingDto(
    val id: String = "",
    val book: BookDto? = null,
    @SerialName("book_id")
    @JsonNames("bookId")
    val bookId: String? = null,
    @SerialName("borrowed_at")
    @JsonNames("borrowedAt")
    val borrowedAt: String? = null,
    @SerialName("created_at")
    @JsonNames("createdAt")
    val createdAt: String? = null,
    @SerialName("due_at")
    @JsonNames("dueAt", "due_date", "dueDate")
    val dueAt: String? = null,
    val returning: ReturningDto? = null,
    @SerialName("staff_id")
    @JsonNames("staffId")
    val staffId: String? = null,
    @SerialName("subscription_id")
    @JsonNames("subscriptionId")
    val subscriptionId: String? = null,
    @SerialName("updated_at")
    @JsonNames("updatedAt")
    val updatedAt: String? = null
)
