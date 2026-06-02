package com.example.feature.books.presentation.bookdetail

import android.util.Log
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
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val getCachedBookDetailUseCase: GetCachedBookDetailUseCase,
    private val getBookReviewsUseCase: GetBookReviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()
    private val _reviewsPreviewState = MutableStateFlow<BookReviewsPreviewUiState>(BookReviewsPreviewUiState.Loading)
    val reviewsPreviewState: StateFlow<BookReviewsPreviewUiState> = _reviewsPreviewState.asStateFlow()
    private var previewRequestVersion = 0

    fun loadBook(bookId: String) {
        if (bookId.isBlank()) {
            _uiState.value = BookDetailUiState.Error("Invalid book id")
            _reviewsPreviewState.value = BookReviewsPreviewUiState.Error("Invalid book id")
            return
        }

        viewModelScope.launch {
            previewRequestVersion += 1
            _uiState.value = BookDetailUiState.Loading
            _reviewsPreviewState.value = BookReviewsPreviewUiState.Loading
            val cached = getCachedBookDetailUseCase(bookId)
            if (cached != null) {
                _uiState.value = BookDetailUiState.Content(cached)
                loadReviewsPreview(cached)
            }

            when (val result = getBookDetailUseCase(bookId)) {
                is BooksResult.Success -> {
                    _uiState.value = BookDetailUiState.Content(result.data)
                    loadReviewsPreview(result.data)
                }
                is BooksResult.Error -> {
                    Log.e(TAG, "Failed to load book detail: ${result.message}", result.cause)
                    if (cached == null) {
                        _uiState.value = BookDetailUiState.Error(result.message)
                        _reviewsPreviewState.value = BookReviewsPreviewUiState.Error(result.message)
                    }
                }
                is BooksResult.Loading -> Unit
            }
        }
    }

    private fun loadReviewsPreview(book: BookDetail) {
        val requestVersion = ++previewRequestVersion
        _reviewsPreviewState.value = BookReviewsPreviewUiState.Loading

        viewModelScope.launch {
            when (val result = getBookReviewsUseCase(
                bookId = book.id,
                libraryId = book.libraryId ?: book.library?.id,
                borrowingId = book.stats?.borrowing?.id,
                skip = 0,
                limit = PREVIEW_LIMIT
            )) {
                is BooksResult.Success -> {
                    if (requestVersion != previewRequestVersion) return@launch
                    _reviewsPreviewState.value = if (result.data.isEmpty()) {
                        BookReviewsPreviewUiState.Empty
                    } else {
                        BookReviewsPreviewUiState.Content(result.data)
                    }
                }
                is BooksResult.Error -> {
                    if (requestVersion != previewRequestVersion) return@launch
                    _reviewsPreviewState.value = BookReviewsPreviewUiState.Error(result.message)
                }
                is BooksResult.Loading -> Unit
            }
        }
    }

    companion object {
        private const val TAG = "BookDetailVM"
        private const val PREVIEW_LIMIT = 2
    }
}

sealed class BookDetailUiState {
    data object Loading : BookDetailUiState()
    data class Content(val book: BookDetail) : BookDetailUiState()
    data class Error(val message: String) : BookDetailUiState()
}

sealed class BookReviewsPreviewUiState {
    data object Loading : BookReviewsPreviewUiState()
    data object Empty : BookReviewsPreviewUiState()
    data class Content(val reviews: List<Review>) : BookReviewsPreviewUiState()
    data class Error(val message: String) : BookReviewsPreviewUiState()
}
