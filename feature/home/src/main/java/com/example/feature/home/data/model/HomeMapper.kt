package com.example.feature.home.data.model

import com.example.core.model.book.BookColorsDto
import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookDetailDto
import com.example.core.model.book.BookDto
import com.example.core.model.book.BookPalette
import com.example.core.model.book.BookBorrowing
import com.example.core.model.book.BookBorrowingDto
import com.example.core.model.book.BookStats
import com.example.core.model.book.BookStatsDto
import com.example.core.model.book.BookSummary
import com.example.core.model.book.HslColor
import com.example.core.model.book.HslColorDto
import com.example.core.model.book.LibraryDto
import com.example.core.model.book.LibraryInfo
import com.example.feature.home.data.model.borrowing_model.BorrowingDto
import com.example.feature.home.data.model.borrowing_model.ReturningDto
import com.example.feature.home.data.model.library_model.LibraryDetailDto
import com.example.feature.home.data.model.subscription_model.SubscriptionDto
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.Returning
import com.example.feature.home.domain.model.Subscription

fun BorrowingDto.toDomain(): Borrowing {
    val mappedBook = book?.toDomain() ?: BookSummary(
        id = bookId.orEmpty(),
        title = "",
        author = "",
        year = null,
        code = null,
        coverUrl = null,
        colors = null,
        libraryId = null,
        available = null,
        rating = null
    )

    return Borrowing(
        id = id,
        book = mappedBook,
        bookId = bookId.orEmpty(),
        borrowedAt = borrowedAt,
        createdAt = createdAt,
        dueAt = dueAt,
        updatedAt = updatedAt,
        staffId = staffId,
        subscriptionId = subscriptionId,
        returning = returning?.toDomain()
    )
}

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
    val statsDomain = stats?.toDomain()
    return BookDetail(
        id = id,
        title = title,
        author = author.orEmpty(),
        year = year,
        code = code,
        coverUrl = coverUrl,
        colors = colors?.toDomain(),
        libraryId = libraryId,
        description = description,
        stats = statsDomain,
        library = library?.toDomain()
    )
}

fun BookDetail.toDto(): BookDetailDto {
    return BookDetailDto(
        id = id,
        title = title,
        author = author,
        year = year,
        code = code,
        coverUrl = coverUrl,
        colors = colors?.toDto(),
        libraryId = libraryId,
        description = description,
        createdAt = null,
        updatedAt = null,
        library = library?.toDto(),
        stats = stats?.toDto()
    )
}

private fun ReturningDto.toDomain(): Returning {
    return Returning(
        id = id,
        borrowingId = borrowingId,
        fine = fine ?: 0,
        returnedAt = returnedAt,
        staffId = staffId
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
        rating = rating ?: 0.0,
        borrowing = borrowing?.toDomain()
    )
}

private fun BookStats.toDto(): BookStatsDto {
    return BookStatsDto(
        borrowCount = borrowCount,
        reviewCount = reviewCount,
        rating = rating,
        borrowing = borrowing?.toDto()
    )
}

private fun BookBorrowingDto.toDomain(): BookBorrowing? {
    val hasContent = listOf(
        id,
        bookId,
        subscriptionId,
        staffId,
        borrowedAt,
        dueAt,
        createdAt,
        updatedAt
    ).any { !it.isNullOrBlank() }
    if (!hasContent) return null
    val resolvedId = id.orEmpty()
    return BookBorrowing(
        id = resolvedId,
        borrowedAt = borrowedAt,
        dueAt = dueAt
    )
}

private fun BookBorrowing.toDto(): BookBorrowingDto {
    return BookBorrowingDto(
        id = id,
        borrowedAt = borrowedAt,
        dueAt = dueAt
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

private fun LibraryInfo.toDto(): LibraryDto {
    return LibraryDto(
        id = id,
        name = name,
        logo = logo,
        createdAt = null,
        updatedAt = null
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

fun SubscriptionDto.toDomain(): Subscription {
    val resolvedLibraryId = membership?.libraryId ?: membership?.library?.id
    return Subscription(
        id = id.orEmpty(),
        userId = userId,
        membershipId = membershipId,
        libraryId = resolvedLibraryId,
        libraryLogoUrl = null,
        createdAt = createdDate,
        expiresAt = expireDate,
        amount = amount,
        finePerDay = finePerDay,
        loanPeriod = loanPeriod,
        activeLoanLimit = activeLoanLimit,
        membershipName = membership?.name.orEmpty(),
        libraryName = membership?.library?.name
    )
}

fun LibraryDetailDto.toDomain(): LibraryInfo {
    return LibraryInfo(
        id = id,
        name = name,
        logo = logo,
        address = address
    )
}

fun LibraryInfo.toLibraryDetailDto(): LibraryDetailDto {
    return LibraryDetailDto(
        id = id,
        name = name,
        logo = logo,
        address = address,
        createdAt = null,
        updatedAt = null
    )
}
