package com.example.feature.books.domain.usecase

import com.example.feature.books.domain.repository.BooksRepository

class GetCachedBookDetailUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String) = booksRepository.getCachedBookDetail(bookId)
}
