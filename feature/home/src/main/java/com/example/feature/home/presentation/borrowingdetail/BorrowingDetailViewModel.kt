package com.example.feature.home.presentation.borrowingdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.home.domain.model.BookDetail
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.usecase.GetCachedBookDetailUseCase
import com.example.feature.home.domain.usecase.GetBookDetailUseCase
import com.example.feature.home.domain.usecase.GetBorrowingByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BorrowingDetailViewModel(
    private val getBorrowingByIdUseCase: GetBorrowingByIdUseCase,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val getCachedBookDetailUseCase: GetCachedBookDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BorrowingDetailUiState>(BorrowingDetailUiState.Loading)
    val uiState: StateFlow<BorrowingDetailUiState> = _uiState.asStateFlow()

    fun loadBorrowing(borrowingId: String) {
        if (borrowingId.isBlank()) {
            _uiState.value = BorrowingDetailUiState.Error("Invalid borrowing id")
            return
        }

        viewModelScope.launch {
            _uiState.value = BorrowingDetailUiState.Loading
            when (val result = getBorrowingByIdUseCase(borrowingId)) {
                is HomeResult.Success -> {
                    val borrowing = result.data
                    _uiState.value = BorrowingDetailUiState.Content(
                        borrowing = borrowing,
                        bookDetail = null,
                        isBookLoading = true
                    )
                    loadBookDetail(borrowing)
                }
                is HomeResult.Error -> {
                    Log.e(TAG, "Failed to load borrowing detail: ${result.message}", result.cause)
                    _uiState.value = BorrowingDetailUiState.Error(result.message)
                }
                is HomeResult.Loading -> {
                    _uiState.value = BorrowingDetailUiState.Loading
                }
            }
        }
    }

    private fun loadBookDetail(borrowing: Borrowing) {
        val bookId = borrowing.bookId.ifBlank { borrowing.book.id }
        if (bookId.isBlank()) {
            _uiState.value = BorrowingDetailUiState.Content(
                borrowing = borrowing,
                bookDetail = null,
                isBookLoading = false
            )
            return
        }

        viewModelScope.launch {
            val cached = getCachedBookDetailUseCase(bookId)
            if (cached != null) {
                _uiState.value = BorrowingDetailUiState.Content(
                    borrowing = borrowing,
                    bookDetail = cached,
                    isBookLoading = true
                )
            }

            when (val result = getBookDetailUseCase(bookId)) {
                is HomeResult.Success -> {
                    _uiState.value = BorrowingDetailUiState.Content(
                        borrowing = borrowing,
                        bookDetail = result.data,
                        isBookLoading = false
                    )
                }
                is HomeResult.Error -> {
                    Log.w(TAG, "Failed to load book detail: ${result.message}", result.cause)
                    _uiState.value = BorrowingDetailUiState.Content(
                        borrowing = borrowing,
                        bookDetail = null,
                        isBookLoading = false
                    )
                }
                is HomeResult.Loading -> Unit
            }
        }
    }

    companion object {
        private const val TAG = "BorrowingDetailVM"
    }
}

sealed class BorrowingDetailUiState {
    data object Loading : BorrowingDetailUiState()
    data class Content(
        val borrowing: Borrowing,
        val bookDetail: BookDetail?,
        val isBookLoading: Boolean
    ) : BorrowingDetailUiState()
    data class Error(val message: String) : BorrowingDetailUiState()
}
