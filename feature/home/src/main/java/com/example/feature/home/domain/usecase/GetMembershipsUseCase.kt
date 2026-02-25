package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetMembershipsUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke() = homeRepository.getMemberships()
}
