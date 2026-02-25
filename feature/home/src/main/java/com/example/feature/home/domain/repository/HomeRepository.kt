package com.example.feature.home.domain.repository

import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.model.Membership

interface HomeRepository {
    suspend fun getCachedBorrowings(userId: String? = null): List<Borrowing>
    suspend fun getBorrowings(userId: String? = null): HomeResult<List<Borrowing>>
    suspend fun getBorrowingById(borrowingId: String, userId: String? = null): HomeResult<Borrowing>
    suspend fun getMemberships(): HomeResult<List<Membership>>
}
