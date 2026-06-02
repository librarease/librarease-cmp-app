package com.example.feature.home.presentation.borrowingdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.book.BookDetail
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HomeResult
import com.example.core.model.review.Review
import com.example.feature.home.domain.usecase.GetCachedBookDetailUseCase
import com.example.feature.home.domain.usecase.GetBookDetailUseCase
import com.example.feature.home.domain.usecase.GetBorrowingByIdUseCase
import com.example.feature.home.domain.usecase.GetReviewsForBorrowingUseCase
import com.example.feature.home.domain.usecase.SubmitReviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BorrowingDetailViewModel(
    private val getBorrowingByIdUseCase: GetBorrowingByIdUseCase,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val getCachedBookDetailUseCase: GetCachedBookDetailUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase,
    private val getReviewsForBorrowingUseCase: GetReviewsForBorrowingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BorrowingDetailUiState>(BorrowingDetailUiState.Loading)
    val uiState: StateFlow<BorrowingDetailUiState> = _uiState.asStateFlow()

    private val _reviewSubmissionState = MutableStateFlow<ReviewSubmissionState>(ReviewSubmissionState.Idle)
    val reviewSubmissionState: StateFlow<ReviewSubmissionState> = _reviewSubmissionState.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _userHasReviewed = MutableStateFlow(false)
    val userHasReviewed: StateFlow<Boolean> = _userHasReviewed.asStateFlow()

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
                    loadReviews(borrowing.bookId, borrowing.id)
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

    private fun loadReviews(bookId: String, borrowingId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading reviews for bookId=$bookId")
            when (val result = getReviewsForBorrowingUseCase(bookId)) {
                is HomeResult.Success -> {
                    _reviews.value = result.data
                    val hasReviewedThisBorrowing = result.data.any { it.borrowingId == borrowingId }
                    _userHasReviewed.value = hasReviewedThisBorrowing
                    Log.d(TAG, "Loaded ${result.data.size} reviews, userHasReviewed=$hasReviewedThisBorrowing for borrowingId=$borrowingId")
                }
                is HomeResult.Error -> {
                    Log.w(TAG, "Failed to load reviews: ${result.message}", result.cause)
                    _reviews.value = emptyList()
                    _userHasReviewed.value = false
                }
                is HomeResult.Loading -> Unit
            }
        }
    }

    fun submitReview(borrowingId: String, bookId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _reviewSubmissionState.value = ReviewSubmissionState.Loading
            Log.d(TAG, "Submitting review: borrowingId=$borrowingId, rating=$rating")
            
            when (val result = submitReviewUseCase(borrowingId, rating, comment)) {
                is HomeResult.Success -> {
                    Log.d(TAG, "Review submitted successfully")
                    _reviewSubmissionState.value = ReviewSubmissionState.Success
                    loadReviews(bookId, borrowingId)
                }
                is HomeResult.Error -> {
                    Log.e(TAG, "Failed to submit review: ${result.message}", result.cause)
                    _reviewSubmissionState.value = ReviewSubmissionState.Error(result.message)
                }
                is HomeResult.Loading -> {
                    _reviewSubmissionState.value = ReviewSubmissionState.Loading
                }
            }
        }
    }

    fun resetReviewSubmissionState() {
        _reviewSubmissionState.value = ReviewSubmissionState.Idle
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

sealed class ReviewSubmissionState {
    data object Idle : ReviewSubmissionState()
    data object Loading : ReviewSubmissionState()
    data object Success : ReviewSubmissionState()
    data class Error(val message: String) : ReviewSubmissionState()
}
