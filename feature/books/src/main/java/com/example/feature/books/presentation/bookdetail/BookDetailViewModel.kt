package com.example.feature.books.presentation.bookdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.book.BookDetail
import com.example.feature.books.domain.model.BooksResult
import com.example.feature.books.domain.usecase.GetBookDetailUseCase
import com.example.feature.books.domain.usecase.GetCachedBookDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val getCachedBookDetailUseCase: GetCachedBookDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: String) {
        if (bookId.isBlank()) {
            _uiState.value = BookDetailUiState.Error("Invalid book id")
            return
        }

        viewModelScope.launch {
            _uiState.value = BookDetailUiState.Loading
            val cached = getCachedBookDetailUseCase(bookId)
            if (cached != null) {
                _uiState.value = BookDetailUiState.Content(cached)
            }

            when (val result = getBookDetailUseCase(bookId)) {
                is BooksResult.Success -> {
                    _uiState.value = BookDetailUiState.Content(result.data)
                }
                is BooksResult.Error -> {
                    Log.e(TAG, "Failed to load book detail: ${result.message}", result.cause)
                    if (cached == null) {
                        _uiState.value = BookDetailUiState.Error(result.message)
                    }
                }
                is BooksResult.Loading -> Unit
            }
        }
    }

    companion object {
        private const val TAG = "BookDetailVM"
    }
}

sealed class BookDetailUiState {
    data object Loading : BookDetailUiState()
    data class Content(val book: BookDetail) : BookDetailUiState()
    data class Error(val message: String) : BookDetailUiState()
}
