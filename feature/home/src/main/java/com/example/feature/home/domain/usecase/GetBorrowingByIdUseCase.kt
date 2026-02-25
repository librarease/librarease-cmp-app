package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetBorrowingByIdUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(
        borrowingId: String,
        userId: String? = null
    ) = homeRepository.getBorrowingById(borrowingId, userId)
}
