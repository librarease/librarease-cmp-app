package com.example.core.model.book

data class BookSummary(
    val id: String,
    val title: String,
    val author: String,
    val year: Int?,
    val code: String?,
    val coverUrl: String?,
    val colors: BookPalette?,
    val libraryId: String?
)

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

data class BookPalette(
    val muted: HslColor?,
    val vibrant: HslColor?,
    val darkMuted: HslColor?,
    val lightMuted: HslColor?,
    val darkVibrant: HslColor?,
    val lightVibrant: HslColor?
)

data class HslColor(
    val h: Float,
    val s: Float,
    val l: Float
)
