package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetSubscriptionsUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(userId: String? = null) = homeRepository.getSubscriptions(userId)
}
