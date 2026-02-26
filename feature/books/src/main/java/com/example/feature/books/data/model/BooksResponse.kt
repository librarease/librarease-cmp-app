@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.feature.books.data.model

import com.example.core.model.book.BookDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BooksResponse(
    @JsonNames("data", "books", "items")
    val books: List<BookDto> = emptyList()
)
