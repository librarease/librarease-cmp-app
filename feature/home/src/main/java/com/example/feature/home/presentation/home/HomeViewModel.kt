package com.example.feature.home.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HomeResult
import com.example.core.model.book.LibraryInfo
import com.example.feature.home.domain.model.Subscription
import com.example.feature.home.domain.usecase.GetBorrowingsUseCase
import com.example.feature.home.domain.usecase.GetCachedBorrowingsUseCase
import com.example.feature.home.domain.usecase.GetCachedBookDetailUseCase
import com.example.feature.home.domain.usecase.GetCachedLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetBookDetailUseCase
import com.example.feature.home.domain.usecase.GetLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetSubscriptionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCachedBorrowingsUseCase: GetCachedBorrowingsUseCase,
    private val getBorrowingsUseCase: GetBorrowingsUseCase,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val getCachedBookDetailUseCase: GetCachedBookDetailUseCase,
    private val getLibraryDetailUseCase: GetLibraryDetailUseCase,
    private val getCachedLibraryDetailUseCase: GetCachedLibraryDetailUseCase,
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val bookPrefetchInFlight = mutableSetOf<String>()
    private val libraryPrefetchInFlight = mutableSetOf<String>()

    fun loadHome(userId: String? = null) {
        viewModelScope.launch {
            val subscriptions = loadSubscriptionsSafely(userId)
            val cachedBorrowings = getCachedBorrowingsUseCase(userId)

            if (cachedBorrowings.isNotEmpty()) {
                _uiState.value = HomeUiState.Content(
                    borrowings = cachedBorrowings,
                    subscriptions = subscriptions
                )
                prefetchBookDetails(cachedBorrowings)
                prefetchLibraryDetails(subscriptions)
                refresh(userId = userId, subscriptions = subscriptions)
                return@launch
            }

            _uiState.value = HomeUiState.Loading
            refresh(userId = userId, subscriptions = subscriptions)
        }
    }

    fun refresh(
        userId: String? = null,
        subscriptions: List<Subscription>? = null
    ) {
        viewModelScope.launch {
            val resolvedSubscriptions = subscriptions ?: loadSubscriptionsSafely(userId)
            val hasContentAlready = _uiState.value is HomeUiState.Content
            if (!hasContentAlready) {
                _uiState.value = HomeUiState.Loading
            }

            val borrowingsResult = getBorrowingsUseCase(userId)
            when (borrowingsResult) {
                is HomeResult.Success -> {
                    _uiState.value = HomeUiState.Content(
                        borrowings = borrowingsResult.data,
                        subscriptions = resolvedSubscriptions
                    )
                    prefetchBookDetails(borrowingsResult.data)
                    prefetchLibraryDetails(resolvedSubscriptions)
                }
                is HomeResult.Error -> {
                    Log.e(TAG, "Borrowings error: ${borrowingsResult.message}", borrowingsResult.cause)
                    if (!hasContentAlready) {
                        _uiState.value = HomeUiState.Error(borrowingsResult.message)
                    }
                }
                is HomeResult.Loading -> {
                    if (!hasContentAlready) {
                        _uiState.value = HomeUiState.Loading
                    }
                }
            }
        }
    }

    private fun prefetchBookDetails(borrowings: List<Borrowing>) {
        if (borrowings.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            borrowings.forEach { borrowing ->
                val bookId = borrowing.bookId.ifBlank { borrowing.book.id }
                if (bookId.isBlank()) return@forEach
                if (isPrefetchInFlight(bookId)) return@forEach

                try {
                    val cached = getCachedBookDetailUseCase(bookId)
                    if (cached != null) return@forEach
                    getBookDetailUseCase(bookId)
                } catch (_: Exception) {
                    // Ignore prefetch failures; detail screen will retry on demand.
                } finally {
                    markPrefetchComplete(bookId)
                }
            }
        }
    }

    private fun prefetchLibraryDetails(subscriptions: List<Subscription>) {
        if (subscriptions.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val libraryIds = subscriptions
                .mapNotNull { it.libraryId?.takeIf { id -> id.isNotBlank() } }
                .distinct()

            libraryIds.forEach { libraryId ->
                if (isLibraryPrefetchInFlight(libraryId)) return@forEach

                try {
                    val cached = getCachedLibraryDetailUseCase(libraryId)
                    if (cached != null) {
                        applyLibraryInfo(cached)
                        return@forEach
                    }

                    when (val result = getLibraryDetailUseCase(libraryId)) {
                        is HomeResult.Success -> applyLibraryInfo(result.data)
                        else -> Unit
                    }
                } catch (_: Exception) {
                    // Ignore prefetch failures; UI will show placeholder icon.
                } finally {
                    markLibraryPrefetchComplete(libraryId)
                }
            }
        }
    }

    private fun isPrefetchInFlight(bookId: String): Boolean {
        synchronized(bookPrefetchInFlight) {
            if (bookPrefetchInFlight.contains(bookId)) return true
            bookPrefetchInFlight.add(bookId)
            return false
        }
    }

    private fun markPrefetchComplete(bookId: String) {
        synchronized(bookPrefetchInFlight) {
            bookPrefetchInFlight.remove(bookId)
        }
    }

    private fun isLibraryPrefetchInFlight(libraryId: String): Boolean {
        synchronized(libraryPrefetchInFlight) {
            if (libraryPrefetchInFlight.contains(libraryId)) return true
            libraryPrefetchInFlight.add(libraryId)
            return false
        }
    }

    private fun markLibraryPrefetchComplete(libraryId: String) {
        synchronized(libraryPrefetchInFlight) {
            libraryPrefetchInFlight.remove(libraryId)
        }
    }

    private fun applyLibraryInfo(info: LibraryInfo) {
        viewModelScope.launch {
            val current = _uiState.value as? HomeUiState.Content ?: return@launch
            val updated = current.subscriptions.map { subscription ->
                if (subscription.libraryId == info.id) {
                    subscription.copy(
                        libraryLogoUrl = info.logo ?: subscription.libraryLogoUrl,
                        libraryName = if (subscription.libraryName.isNullOrBlank()) info.name else subscription.libraryName
                    )
                } else {
                    subscription
                }
            }
            if (updated != current.subscriptions) {
                _uiState.value = current.copy(subscriptions = updated)
            }
        }
    }

    private suspend fun loadSubscriptionsSafely(userId: String?): List<Subscription> {
        return when (val subscriptionsResult = getSubscriptionsUseCase(userId)) {
            is HomeResult.Success -> enrichSubscriptions(subscriptionsResult.data)
            is HomeResult.Error -> {
                Log.e(TAG, "Subscriptions error: ${subscriptionsResult.message}", subscriptionsResult.cause)
                emptyList()
            }
            is HomeResult.Loading -> emptyList()
        }
    }

    private suspend fun enrichSubscriptions(subscriptions: List<Subscription>): List<Subscription> {
        if (subscriptions.isEmpty()) return subscriptions

        val libraryIds = subscriptions
            .mapNotNull { it.libraryId?.takeIf { id -> id.isNotBlank() } }
            .distinct()
        if (libraryIds.isEmpty()) return subscriptions

        val cachedLibraries = mutableMapOf<String, LibraryInfo>()
        libraryIds.forEach { id ->
            val cached = getCachedLibraryDetailUseCase(id)
            if (cached != null) {
                cachedLibraries[id] = cached
            }
        }

        if (cachedLibraries.isEmpty()) return subscriptions

        return subscriptions.map { subscription ->
            val libraryId = subscription.libraryId
            val cached = if (!libraryId.isNullOrBlank()) cachedLibraries[libraryId] else null
            if (cached != null) {
                subscription.copy(
                    libraryLogoUrl = subscription.libraryLogoUrl ?: cached.logo,
                    libraryName = subscription.libraryName ?: cached.name
                )
            } else {
                subscription
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}

sealed class HomeUiState {
    data object Idle : HomeUiState()
    data object Loading : HomeUiState()
    data class Content(
        val borrowings: List<Borrowing>,
        val subscriptions: List<Subscription>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
