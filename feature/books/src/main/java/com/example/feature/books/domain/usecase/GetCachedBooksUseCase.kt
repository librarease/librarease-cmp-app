package com.example.feature.books.domain.usecase

import com.example.feature.books.domain.repository.BooksRepository

class GetCachedBooksUseCase(private val booksRepository: BooksRepository) {
    suspend operator fun invoke() = booksRepository.getCachedBooks()
}
