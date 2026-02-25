package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetBorrowingsUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(userId: String? = null) = homeRepository.getBorrowings(userId)
}
