package com.example.feature.libraries.domain.repository

import com.example.core.model.book.LibraryInfo
import com.example.feature.libraries.domain.model.LibrariesResult

interface LibrariesRepository {
    suspend fun getLibraries(limit: Int? = null): LibrariesResult<List<LibraryInfo>>
    suspend fun getCachedLibraries(): List<LibraryInfo>
}
