package com.example.feature.home.data.provider

import com.example.core.subscriptions.SubscribedLibrariesProvider
import com.example.core.subscriptions.SubscribedLibrary
import com.example.feature.home.domain.model.Subscription
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.repository.HomeRepository

class HomeSubscribedLibrariesProvider(
    private val homeRepository: HomeRepository
) : SubscribedLibrariesProvider {

    override suspend fun getSubscribedLibraries(userId: String?): List<SubscribedLibrary> {
        val cached = homeRepository.getCachedSubscriptions(userId)
            .mapNotNull { it.toSubscribedLibrary() }
            .distinctBy { it.libraryId }

        return when (val result = homeRepository.getSubscriptions(userId)) {
            is HomeResult.Success -> result.data
                .mapNotNull { it.toSubscribedLibrary() }
                .distinctBy { it.libraryId }
            is HomeResult.Error -> cached
            is HomeResult.Loading -> cached
        }
    }
}

private fun Subscription.toSubscribedLibrary(): SubscribedLibrary? {
    val id = libraryId?.takeIf { it.isNotBlank() } ?: return null
    val name = libraryName?.takeIf { it.isNotBlank() } ?: "Library"
    return SubscribedLibrary(
        libraryId = id,
        libraryName = name,
        libraryLogoUrl = libraryLogoUrl
    )
}
