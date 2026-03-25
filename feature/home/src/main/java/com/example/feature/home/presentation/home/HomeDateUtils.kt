package com.example.feature.home.presentation.home

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun parseDate(value: String?): java.util.Date? {
    if (value.isNullOrBlank()) return null

    val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val dateOnlyParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return runCatching { utcParser.parse(value) }.getOrNull()
        ?: runCatching { dateOnlyParser.parse(value) }.getOrNull()
}
