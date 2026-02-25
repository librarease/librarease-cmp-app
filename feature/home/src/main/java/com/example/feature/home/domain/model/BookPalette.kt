package com.example.feature.home.domain.model

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
