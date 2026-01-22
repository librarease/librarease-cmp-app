package com.example.librarease

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform