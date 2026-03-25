package com.example.feature.books.data.model

import com.example.core.model.book.BookColorsDto
import com.example.core.model.book.BookDetailDto
import com.example.core.model.book.BookDto
import com.example.core.model.book.BookStatsDto
import com.example.core.model.book.BookSummary
import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookPalette
import com.example.core.model.book.BookStats
import com.example.core.model.book.HslColor
import com.example.core.model.book.HslColorDto
import com.example.core.model.book.LibraryDto
import com.example.core.model.book.LibraryInfo

fun BookDto.toDomain(): BookSummary {
    return BookSummary(
        id = id,
        title = title,
        author = author.orEmpty(),
        year = year,
        code = code,
        coverUrl = coverUrl,
        colors = colors?.toDomain(),
        libraryId = libraryId,
        available = available,
        rating = rating
    )
}

fun BookDetailDto.toDomain(): BookDetail {
    return BookDetail(
        id = id,
        title = title,
        author = author.orEmpty(),
        year = year,
        code = code,
        coverUrl = coverUrl,
        colors = colors?.toDomain(),
        libraryId = libraryId,
        available = available,
        description = description,
        stats = stats?.toDomain(),
        library = library?.toDomain()
    )
}

fun BookSummary.toDto(): BookDto {
    return BookDto(
        id = id,
        title = title,
        author = author,
        year = year,
        code = code,
        coverUrl = coverUrl,
        colors = colors?.toDto(),
        libraryId = libraryId,
        available = available,
        rating = rating
    )
}

private fun BookColorsDto.toDomain(): BookPalette {
    return BookPalette(
        muted = muted?.toDomain(),
        vibrant = vibrant?.toDomain(),
        darkMuted = darkMuted?.toDomain(),
        lightMuted = lightMuted?.toDomain(),
        darkVibrant = darkVibrant?.toDomain(),
        lightVibrant = lightVibrant?.toDomain()
    )
}

private fun BookPalette.toDto(): BookColorsDto {
    return BookColorsDto(
        muted = muted?.toDto(),
        vibrant = vibrant?.toDto(),
        darkMuted = darkMuted?.toDto(),
        lightMuted = lightMuted?.toDto(),
        darkVibrant = darkVibrant?.toDto(),
        lightVibrant = lightVibrant?.toDto()
    )
}

private fun BookStatsDto.toDomain(): BookStats {
    return BookStats(
        borrowCount = borrowCount ?: 0,
        reviewCount = reviewCount ?: 0,
        rating = rating ?: 0.0
    )
}

private fun LibraryDto.toDomain(): LibraryInfo {
    return LibraryInfo(
        id = id,
        name = name,
        logo = logo,
        address = null
    )
}

private fun HslColorDto.toDomain(): HslColor? {
    val hue = h ?: return null
    val saturation = s ?: return null
    val lightness = l ?: return null
    return HslColor(
        h = hue,
        s = saturation,
        l = lightness
    )
}

private fun HslColor.toDto(): HslColorDto {
    return HslColorDto(
        h = h,
        s = s,
        l = l,
        space = "hsl"
    )
}
