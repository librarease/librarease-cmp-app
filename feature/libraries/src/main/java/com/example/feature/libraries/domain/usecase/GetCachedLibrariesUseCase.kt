package com.example.feature.libraries.domain.usecase

import com.example.feature.libraries.domain.repository.LibrariesRepository

class GetCachedLibrariesUseCase(private val librariesRepository: LibrariesRepository) {
    suspend operator fun invoke() = librariesRepository.getCachedLibraries()
}
