package com.example.feature.home.domain.usecase

import com.example.feature.home.domain.repository.HomeRepository

class GetLibraryDetailUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(libraryId: String) = homeRepository.getLibraryDetail(libraryId)
}
