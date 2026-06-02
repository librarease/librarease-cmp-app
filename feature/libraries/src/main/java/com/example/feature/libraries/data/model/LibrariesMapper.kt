package com.example.feature.libraries.data.model

import com.example.core.model.book.LibraryInfo

internal fun LibraryItemDto.toDomain(): LibraryInfo {
    return LibraryInfo(
        id = id,
        name = name,
        logo = logo,
        address = address
    )
}

internal fun LibraryInfo.toDto(): LibraryItemDto {
    return LibraryItemDto(
        id = id,
        name = name,
        logo = logo,
        address = address
    )
}
