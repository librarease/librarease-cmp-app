package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetSubscriptionByIdUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(subscriptionId: String, userId: String? = null) =
        homeRepository.getSubscriptionById(subscriptionId, userId)
}
