package com.example.feature.libraries.presentation.libraries

import com.example.core.model.book.LibraryInfo

data class LibrariesUiState(
    val allLibraries: List<LibraryInfo> = emptyList(),
    val myLibraries: List<LibraryInfo> = emptyList(),
    val subscribedLibraryIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: LibraryCategory = LibraryCategory.MY
)
