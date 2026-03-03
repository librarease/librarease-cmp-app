package com.example.feature.books.presentation.books

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.books.domain.model.BooksResult
import com.example.feature.books.domain.usecase.GetBooksUseCase
import com.example.feature.books.domain.usecase.GetCachedBooksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BooksViewModel(
    private val getCachedBooksUseCase: GetCachedBooksUseCase,
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState(isLoading = false))
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    private val pageSize = 10
    private var currentOffset = 0
    private var requestInFlight = false
    private var canLoadMore = true

    fun loadBooks() {
        if (requestInFlight) return

        viewModelScope.launch {
            val cached = getCachedBooksUseCase()
            if (cached.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    books = cached,
                    isLoading = false,
                    errorMessage = null,
                    canLoadMore = true
                )
                currentOffset = cached.size
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            fetchPage(skip = 0, append = false)
        }
    }

    fun loadMore() {
        if (requestInFlight || !canLoadMore) return
        viewModelScope.launch { fetchPage(skip = currentOffset, append = true) }
    }

    fun retry() {
        currentOffset = 0
        canLoadMore = true
        viewModelScope.launch { fetchPage(skip = 0, append = false) }
    }

    private suspend fun fetchPage(skip: Int, append: Boolean) {
        requestInFlight = true
        val existingBooks = _uiState.value.books
        if (append) {
            _uiState.value = _uiState.value.copy(isAppending = true, errorMessage = null)
        } else if (existingBooks.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        }

        when (val result = getBooksUseCase(pageSize, skip)) {
            is BooksResult.Success -> {
                val incoming = result.data
                val updated = if (append) {
                    (existingBooks + incoming).distinctBy { it.id }
                } else {
                    incoming.distinctBy { it.id }
                }
                val appendedNew = updated.size > existingBooks.size
                canLoadMore = incoming.size >= pageSize && appendedNew
                currentOffset = updated.size
                _uiState.value = _uiState.value.copy(
                    books = updated,
                    isLoading = false,
                    isAppending = false,
                    errorMessage = null,
                    canLoadMore = canLoadMore
                )
            }
            is BooksResult.Error -> {
                Log.e(TAG, "Books fetch failed: ${result.message}", result.cause)
                val hasContent = existingBooks.isNotEmpty()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAppending = false,
                    errorMessage = if (hasContent) null else result.message
                )
            }
            is BooksResult.Loading -> {
                if (existingBooks.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }

        requestInFlight = false
    }

    companion object {
        private const val TAG = "BooksViewModel"
    }
}
