package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetBookDetailUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(bookId: String) = homeRepository.getBookDetail(bookId)
}
