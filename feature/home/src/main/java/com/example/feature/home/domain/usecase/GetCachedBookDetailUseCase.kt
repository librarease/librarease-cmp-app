package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetCachedBookDetailUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(bookId: String) = homeRepository.getCachedBookDetail(bookId)
}
