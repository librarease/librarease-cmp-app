package com.example.feature.home.data.model

import com.example.feature.home.domain.model.BorrowedBook
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.BookPalette
import com.example.feature.home.domain.model.HslColor
import com.example.feature.home.domain.model.Membership
import com.example.feature.home.domain.model.Returning

fun BorrowingDto.toDomain(): Borrowing {
    val mappedBook = book?.toDomain() ?: BorrowedBook(
        id = bookId.orEmpty(),
        title = "",
        author = "",
        year = null,
        code = null,
        coverUrl = null,
        colors = null
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

fun BookDto.toDomain(): BorrowedBook {
    return BorrowedBook(
        id = id,
        title = title,
        author = author.orEmpty(),
        year = year,
        code = code,
        coverUrl = coverUrl,
        colors = colors?.toDomain()
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

fun MembershipDto.toDomain(): Membership {
    return Membership(
        id = id,
        name = name,
        tier = tier.orEmpty(),
        status = status.orEmpty(),
        expiresAt = expiresAt
    )
}
