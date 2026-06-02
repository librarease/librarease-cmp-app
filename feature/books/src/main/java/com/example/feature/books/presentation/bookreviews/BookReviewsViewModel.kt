package com.example.feature.books.presentation.bookreviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.book.BookDetail
import com.example.core.model.review.Review
import com.example.feature.books.domain.model.BooksResult
import com.example.feature.books.domain.usecase.GetBookDetailUseCase
import com.example.feature.books.domain.usecase.GetBookReviewsUseCase
import com.example.feature.books.domain.usecase.GetCachedBookDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookReviewsViewModel(
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val getCachedBookDetailUseCase: GetCachedBookDetailUseCase,
    private val getBookReviewsUseCase: GetBookReviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookReviewsUiState())
    val uiState: StateFlow<BookReviewsUiState> = _uiState.asStateFlow()

    private var currentBookId: String? = null
    private var currentQuery: ReviewsQuery? = null
    private var nextSkip = 0
    private var requestVersion = 0

    fun load(bookId: String, force: Boolean = false) {
        if (bookId.isBlank()) {
            _uiState.value = BookReviewsUiState(errorMessage = "Invalid book id")
            return
        }

        if (!force && currentBookId == bookId && currentQuery != null) return

        currentBookId = bookId
        currentQuery = null
        nextSkip = 0
        val version = ++requestVersion

        _uiState.value = BookReviewsUiState(isInitialLoading = true)

        viewModelScope.launch {
            val book = resolveBook(bookId)
            if (version != requestVersion) return@launch

            if (book == null) {
                _uiState.value = BookReviewsUiState(errorMessage = "Book is unavailable.")
                return@launch
            }

            currentQuery = ReviewsQuery(
                bookId = book.id,
                libraryId = book.libraryId ?: book.library?.id,
                borrowingId = book.stats?.borrowing?.id
            )

            _uiState.update { state ->
                state.copy(bookTitle = book.title.takeIf { it.isNotBlank() })
            }

            loadPage(reset = true, version = version)
        }
    }

    fun refresh() {
        val bookId = currentBookId ?: return
        load(bookId = bookId, force = true)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing || state.isAppending || !state.canLoadMore) return
        val version = requestVersion
        loadPage(reset = false, version = version)
    }

    private suspend fun resolveBook(bookId: String): BookDetail? {
        val fresh = getBookDetailUseCase(bookId)
        if (fresh is BooksResult.Success) return fresh.data

        return getCachedBookDetailUseCase(bookId)
    }

    private fun loadPage(reset: Boolean, version: Int) {
        val query = currentQuery ?: return
        if (_uiState.value.isAppending && !reset) return

        val skip = if (reset) 0 else nextSkip
        if (reset) {
            nextSkip = 0
            _uiState.update { state ->
                state.copy(
                    reviews = emptyList(),
                    isInitialLoading = state.reviews.isEmpty(),
                    isRefreshing = state.reviews.isNotEmpty(),
                    isAppending = false,
                    canLoadMore = true,
                    errorMessage = null,
                    emptyState = null
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(isAppending = true, errorMessage = null)
            }
        }

        viewModelScope.launch {
            when (val result = getBookReviewsUseCase(
                bookId = query.bookId,
                libraryId = query.libraryId,
                borrowingId = query.borrowingId,
                skip = skip,
                limit = PAGE_LIMIT
            )) {
                is BooksResult.Success -> {
                    if (version != requestVersion) return@launch

                    val current = if (reset) emptyList() else _uiState.value.reviews
                    val merged = (current + result.data)
                        .distinctBy { review -> review.stableKey() }

                    val shouldStop = result.data.isEmpty() ||
                        result.data.size < PAGE_LIMIT ||
                        merged.size == current.size

                    nextSkip = skip + PAGE_LIMIT
                    _uiState.update { state ->
                        state.copy(
                            reviews = merged,
                            isInitialLoading = false,
                            isRefreshing = false,
                            isAppending = false,
                            canLoadMore = !shouldStop,
                            emptyState = if (merged.isEmpty()) BookReviewsEmptyState.NoReviews else null,
                            errorMessage = null
                        )
                    }
                }
                is BooksResult.Error -> {
                    if (version != requestVersion) return@launch
                    _uiState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            isAppending = false,
                            errorMessage = result.message
                        )
                    }
                }
                is BooksResult.Loading -> Unit
            }
        }
    }

    private fun Review.stableKey(): String {
        return id.ifBlank {
            listOf(comment.orEmpty(), createdAt.orEmpty(), reviewerName.orEmpty())
                .joinToString("|")
        }
    }

    private data class ReviewsQuery(
        val bookId: String,
        val libraryId: String?,
        val borrowingId: String?
    )

    companion object {
        private const val PAGE_LIMIT = 20
    }
}

data class BookReviewsUiState(
    val bookTitle: String? = null,
    val reviews: List<Review> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isAppending: Boolean = false,
    val canLoadMore: Boolean = true,
    val errorMessage: String? = null,
    val emptyState: BookReviewsEmptyState? = null
)

enum class BookReviewsEmptyState {
    NoReviews
}
