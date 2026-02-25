package com.example.feature.home.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.model.Membership
import com.example.feature.home.domain.usecase.GetBorrowingsUseCase
import com.example.feature.home.domain.usecase.GetCachedBorrowingsUseCase
import com.example.feature.home.domain.usecase.GetMembershipsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCachedBorrowingsUseCase: GetCachedBorrowingsUseCase,
    private val getBorrowingsUseCase: GetBorrowingsUseCase,
    private val getMembershipsUseCase: GetMembershipsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHome(userId: String? = null) {
        viewModelScope.launch {
            val memberships = loadMembershipsSafely()
            val cachedBorrowings = getCachedBorrowingsUseCase(userId)

            if (cachedBorrowings.isNotEmpty()) {
                _uiState.value = HomeUiState.Content(
                    borrowings = cachedBorrowings,
                    memberships = memberships
                )
                refresh(userId = userId, memberships = memberships)
                return@launch
            }

            _uiState.value = HomeUiState.Loading
            refresh(userId = userId, memberships = memberships)
        }
    }

    fun refresh(
        userId: String? = null,
        memberships: List<Membership>? = null
    ) {
        viewModelScope.launch {
            val resolvedMemberships = memberships ?: loadMembershipsSafely()
            val hasContentAlready = _uiState.value is HomeUiState.Content
            if (!hasContentAlready) {
                _uiState.value = HomeUiState.Loading
            }

            val borrowingsResult = getBorrowingsUseCase(userId)
            when (borrowingsResult) {
                is HomeResult.Success -> {
                    _uiState.value = HomeUiState.Content(
                        borrowings = borrowingsResult.data,
                        memberships = resolvedMemberships
                    )
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

    private suspend fun loadMembershipsSafely(): List<Membership> {
        return when (val membershipsResult = getMembershipsUseCase()) {
            is HomeResult.Success -> membershipsResult.data
            is HomeResult.Error -> {
                Log.e(TAG, "Memberships error: ${membershipsResult.message}", membershipsResult.cause)
                emptyList()
            }
            is HomeResult.Loading -> emptyList()
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
        val memberships: List<Membership>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
