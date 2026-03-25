package com.example.feature.home.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class WarmUpHomeUseCase(
    private val getBorrowingsUseCase: GetBorrowingsUseCase,
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase
) {
    suspend operator fun invoke(userId: String? = null) = withContext(Dispatchers.IO) {
        if (userId.isNullOrBlank()) return@withContext
        coroutineScope {
            val borrowingsJob = async {
                runCatching { getBorrowingsUseCase(userId) }
            }
            val subscriptionsJob = async {
                runCatching { getSubscriptionsUseCase(userId) }
            }

            borrowingsJob.await()
            subscriptionsJob.await()
        }
    }
}
