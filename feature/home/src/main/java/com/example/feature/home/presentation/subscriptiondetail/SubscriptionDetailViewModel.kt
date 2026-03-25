package com.example.feature.home.presentation.subscriptiondetail

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.book.LibraryInfo
import com.example.feature.home.domain.model.Subscription
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.usecase.GetCachedLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetSubscriptionByIdUseCase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    val cachedQr = qrCache.get(subscription.id)
                    _uiState.value = SubscriptionDetailUiState.Content(
                        subscription = subscription,
                        library = null,
                        isLibraryLoading = true,
                        qrBitmap = cachedQr
                    )
                    loadQrCode(subscription.id)
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
            updateContent { current ->
                current.copy(isLibraryLoading = false)
            }
            return
        }

        viewModelScope.launch {
            val cached = getCachedLibraryDetailUseCase(libraryId)
            if (cached != null) {
                updateContent { current ->
                    current.copy(library = cached, isLibraryLoading = true)
                }
            }

            when (val result = getLibraryDetailUseCase(libraryId)) {
                is HomeResult.Success -> {
                    updateContent { current ->
                        current.copy(library = result.data, isLibraryLoading = false)
                    }
                }
                is HomeResult.Error -> {
                    Log.w(TAG, "Failed to load library detail: ${result.message}", result.cause)
                    updateContent { current ->
                        current.copy(library = cached, isLibraryLoading = false)
                    }
                }
                is HomeResult.Loading -> Unit
            }
        }
    }

    private fun loadQrCode(subscriptionId: String) {
        val cached = qrCache.get(subscriptionId)
        if (cached != null) {
            updateContent { current -> current.copy(qrBitmap = cached) }
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = generateQrBitmap(subscriptionId, 420) ?: return@launch
            qrCache.put(subscriptionId, bitmap)
            withContext(Dispatchers.Main) {
                updateContent { current -> current.copy(qrBitmap = bitmap) }
            }
        }
    }

    private fun updateContent(transform: (SubscriptionDetailUiState.Content) -> SubscriptionDetailUiState.Content) {
        val current = _uiState.value as? SubscriptionDetailUiState.Content ?: return
        _uiState.value = transform(current)
    }

    private fun generateQrBitmap(data: String, size: Int): Bitmap? {
        if (data.isBlank()) return null
        return try {
            val matrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "SubscriptionDetailVM"
        private val qrCache = LruCache<String, Bitmap>(10)
    }
}

sealed class SubscriptionDetailUiState {
    data object Loading : SubscriptionDetailUiState()
    data class Content(
        val subscription: Subscription,
        val library: LibraryInfo?,
        val isLibraryLoading: Boolean,
        val qrBitmap: Bitmap?
    ) : SubscriptionDetailUiState()
    data class Error(val message: String) : SubscriptionDetailUiState()
}
