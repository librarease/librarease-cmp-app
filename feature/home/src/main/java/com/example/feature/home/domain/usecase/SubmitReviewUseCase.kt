package com.example.feature.home.domain.usecase

import com.example.core.model.review.Review
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.repository.HomeRepository

class SubmitReviewUseCase(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(
        borrowingId: String,
        rating: Int,
        comment: String
    ): HomeResult<Review> {
        if (borrowingId.isBlank()) {
            return HomeResult.Error("Borrowing ID cannot be empty")
        }
        
        if (rating < 1 || rating > 5) {
            return HomeResult.Error("Rating must be between 1 and 5")
        }
        
        if (comment.isBlank()) {
            return HomeResult.Error("Review comment cannot be empty")
        }
        
        return homeRepository.submitReview(borrowingId, rating, comment)
    }
}
