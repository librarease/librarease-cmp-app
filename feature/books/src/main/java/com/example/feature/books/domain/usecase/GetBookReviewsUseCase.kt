package com.example.feature.books.domain.usecase

import com.example.core.model.review.Review
import com.example.feature.books.domain.model.BooksResult
import com.example.feature.books.domain.repository.BooksRepository

class GetBookReviewsUseCase(private val booksRepository: BooksRepository) {

    suspend operator fun invoke(
        bookId: String,
        libraryId: String?,
        borrowingId: String? = null,
        skip: Int,
        limit: Int
    ): BooksResult<List<Review>> {
        return booksRepository.getBookReviews(
            bookId = bookId,
            libraryId = libraryId,
            borrowingId = borrowingId,
            skip = skip,
            limit = limit
        )
    }
}
