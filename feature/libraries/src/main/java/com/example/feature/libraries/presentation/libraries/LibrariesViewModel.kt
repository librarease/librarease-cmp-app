package com.example.feature.libraries.presentation.libraries

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.book.LibraryInfo
import com.example.core.subscriptions.SubscribedLibrariesProvider
import com.example.core.subscriptions.SubscribedLibrary
import com.example.feature.libraries.domain.model.LibrariesResult
import com.example.feature.libraries.domain.usecase.GetCachedLibrariesUseCase
import com.example.feature.libraries.domain.usecase.GetLibrariesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibrariesViewModel(
    private val getCachedLibrariesUseCase: GetCachedLibrariesUseCase,
    private val getLibrariesUseCase: GetLibrariesUseCase,
    private val subscribedLibrariesProvider: SubscribedLibrariesProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibrariesUiState(isLoading = false))
    val uiState: StateFlow<LibrariesUiState> = _uiState.asStateFlow()

    private var requestInFlight = false

    fun loadLibraries() {
        if (requestInFlight) return

        viewModelScope.launch {
            requestInFlight = true

            val cachedLibraries = getCachedLibrariesUseCase()
            val initialState = if (cachedLibraries.isNotEmpty()) {
                _uiState.value.copy(
                    allLibraries = cachedLibraries,
                    isLoading = false,
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            _uiState.value = initialState

            val subscribedLibraries = loadSubscribedLibraries()
            val subscribedIds = subscribedLibraries.map { it.libraryId }.toSet()
            val cachedMyLibraries = mergeSubscribedWithAll(subscribedLibraries, cachedLibraries)
            _uiState.value = _uiState.value.copy(
                myLibraries = cachedMyLibraries,
                subscribedLibraryIds = subscribedIds
            )

            when (val result = getLibrariesUseCase(null)) {
                is LibrariesResult.Success -> {
                    val allLibraries = result.data
                    val myLibraries = mergeSubscribedWithAll(subscribedLibraries, allLibraries)
                    _uiState.value = _uiState.value.copy(
                        allLibraries = allLibraries,
                        myLibraries = myLibraries,
                        subscribedLibraryIds = subscribedIds,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is LibrariesResult.Error -> {
                    Log.e(TAG, "Libraries fetch failed: ${result.message}", result.cause)
                    val hasContent = _uiState.value.allLibraries.isNotEmpty() ||
                        _uiState.value.myLibraries.isNotEmpty()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (hasContent) null else result.message
                    )
                }
                is LibrariesResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }

            requestInFlight = false
        }
    }

    fun retry() {
        loadLibraries()
    }

    fun selectCategory(category: LibraryCategory) {
        if (category == _uiState.value.selectedCategory) return
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    private suspend fun loadSubscribedLibraries(): List<SubscribedLibrary> {
        return try {
            subscribedLibrariesProvider.getSubscribedLibraries(null)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load subscribed libraries", e)
            emptyList()
        }
    }

    private fun mergeSubscribedWithAll(
        subscribed: List<SubscribedLibrary>,
        allLibraries: List<LibraryInfo>
    ): List<LibraryInfo> {
        if (subscribed.isEmpty()) return emptyList()

        val allById = allLibraries.associateBy { it.id }
        return subscribed.map { item ->
            val libraryName = item.libraryName.ifBlank { "Library" }
            allById[item.libraryId]
                ?: LibraryInfo(
                    id = item.libraryId,
                    name = libraryName,
                    logo = item.libraryLogoUrl,
                    address = null
                )
        }.distinctBy { it.id }
    }

    companion object {
        private const val TAG = "LibrariesViewModel"
    }
}
