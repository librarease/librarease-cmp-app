package com.example.feature.home.domain.model

data class BookDetail(
    val id: String,
    val title: String,
    val author: String,
    val year: Int?,
    val code: String?,
    val coverUrl: String?,
    val colors: BookPalette?,
    val libraryId: String?,
    val description: String?,
    val stats: BookStats?,
    val library: LibraryInfo?
)

data class BookStats(
    val borrowCount: Int,
    val reviewCount: Int,
    val rating: Double
)

data class LibraryInfo(
    val id: String,
    val name: String,
    val logo: String?,
    val address: String?
)
