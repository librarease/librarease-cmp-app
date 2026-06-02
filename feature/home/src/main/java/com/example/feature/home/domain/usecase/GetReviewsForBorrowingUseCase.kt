package com.example.feature.home.domain.usecase

import com.example.core.model.review.Review
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.repository.HomeRepository

class GetReviewsForBorrowingUseCase(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(bookId: String): HomeResult<List<Review>> {
        if (bookId.isBlank()) {
            return HomeResult.Error("Book ID cannot be empty")
        }
        
        return homeRepository.getReviewsForBook(bookId)
    }
}
