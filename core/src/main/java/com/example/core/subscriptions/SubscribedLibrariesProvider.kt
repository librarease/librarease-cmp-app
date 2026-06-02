package com.example.core.subscriptions

data class SubscribedLibrary(
    val libraryId: String,
    val libraryName: String,
    val libraryLogoUrl: String?
)

interface SubscribedLibrariesProvider {
    suspend fun getSubscribedLibraries(userId: String? = null): List<SubscribedLibrary>
}
