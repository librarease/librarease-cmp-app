package com.example.feature.books.domain.usecase

import com.example.feature.books.domain.repository.BooksRepository

class GetBooksUseCase(private val booksRepository: BooksRepository) {
    suspend operator fun invoke(limit: Int, skip: Int) = booksRepository.getBooks(limit, skip)
}
