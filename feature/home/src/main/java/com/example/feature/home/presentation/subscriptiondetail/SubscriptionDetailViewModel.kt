package com.example.feature.home.presentation.subscriptiondetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.book.LibraryInfo
import com.example.feature.home.domain.model.Subscription
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.usecase.GetCachedLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetSubscriptionByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionDetailViewModel(
    private val getSubscriptionByIdUseCase: GetSubscriptionByIdUseCase,
    private val getLibraryDetailUseCase: GetLibraryDetailUseCase,
    private val getCachedLibraryDetailUseCase: GetCachedLibraryDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubscriptionDetailUiState>(SubscriptionDetailUiState.Loading)
    val uiState: StateFlow<SubscriptionDetailUiState> = _uiState.asStateFlow()

    fun loadSubscription(subscriptionId: String) {
        if (subscriptionId.isBlank()) {
            _uiState.value = SubscriptionDetailUiState.Error("Invalid subscription id")
            return
        }

        viewModelScope.launch {
            _uiState.value = SubscriptionDetailUiState.Loading
            when (val result = getSubscriptionByIdUseCase(subscriptionId)) {
                is HomeResult.Success -> {
                    val subscription = result.data
                    _uiState.value = SubscriptionDetailUiState.Content(
                        subscription = subscription,
                        library = null,
                        isLibraryLoading = true
                    )
                    loadLibrary(subscription)
                }
                is HomeResult.Error -> {
                    Log.e(TAG, "Failed to load subscription detail: ${result.message}", result.cause)
                    _uiState.value = SubscriptionDetailUiState.Error(result.message)
                }
                is HomeResult.Loading -> {
                    _uiState.value = SubscriptionDetailUiState.Loading
                }
            }
        }
    }

    private fun loadLibrary(subscription: Subscription) {
        val libraryId = subscription.libraryId
        if (libraryId.isNullOrBlank()) {
            _uiState.value = SubscriptionDetailUiState.Content(
                subscription = subscription,
                library = null,
                isLibraryLoading = false
            )
            return
        }

        viewModelScope.launch {
            val cached = getCachedLibraryDetailUseCase(libraryId)
            if (cached != null) {
                _uiState.value = SubscriptionDetailUiState.Content(
                    subscription = subscription,
                    library = cached,
                    isLibraryLoading = true
                )
            }

            when (val result = getLibraryDetailUseCase(libraryId)) {
                is HomeResult.Success -> {
                    _uiState.value = SubscriptionDetailUiState.Content(
                        subscription = subscription,
                        library = result.data,
                        isLibraryLoading = false
                    )
                }
                is HomeResult.Error -> {
                    Log.w(TAG, "Failed to load library detail: ${result.message}", result.cause)
                    _uiState.value = SubscriptionDetailUiState.Content(
                        subscription = subscription,
                        library = cached,
                        isLibraryLoading = false
                    )
                }
                is HomeResult.Loading -> Unit
            }
        }
    }

    companion object {
        private const val TAG = "SubscriptionDetailVM"
    }
}

sealed class SubscriptionDetailUiState {
    data object Loading : SubscriptionDetailUiState()
    data class Content(
        val subscription: Subscription,
        val library: LibraryInfo?,
        val isLibraryLoading: Boolean
    ) : SubscriptionDetailUiState()
    data class Error(val message: String) : SubscriptionDetailUiState()
}
