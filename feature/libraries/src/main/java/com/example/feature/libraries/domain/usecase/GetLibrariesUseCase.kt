package com.example.feature.libraries.domain.usecase

import com.example.feature.libraries.domain.repository.LibrariesRepository

class GetLibrariesUseCase(private val librariesRepository: LibrariesRepository) {
    suspend operator fun invoke(limit: Int? = null) = librariesRepository.getLibraries(limit)
}
