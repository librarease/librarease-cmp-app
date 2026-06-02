package com.example.feature.home.domain.repository

import com.example.feature.home.domain.model.Borrowing
import com.example.core.model.book.BookDetail
import com.example.core.model.book.LibraryInfo
import com.example.core.model.review.Review
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.model.Subscription

interface HomeRepository {
    suspend fun getCachedBorrowings(userId: String? = null): List<Borrowing>
    suspend fun getCachedBookDetail(bookId: String): BookDetail?
    suspend fun getCachedLibraryDetail(libraryId: String): LibraryInfo?
    suspend fun getCachedSubscriptions(userId: String? = null): List<Subscription>
    suspend fun getBorrowings(userId: String? = null): HomeResult<List<Borrowing>>
    suspend fun getBorrowingById(borrowingId: String, userId: String? = null): HomeResult<Borrowing>
    suspend fun getBookDetail(bookId: String): HomeResult<BookDetail>
    suspend fun getLibraryDetail(libraryId: String): HomeResult<LibraryInfo>
    suspend fun getSubscriptionById(subscriptionId: String, userId: String? = null): HomeResult<Subscription>
    suspend fun getSubscriptions(userId: String? = null): HomeResult<List<Subscription>>
    suspend fun submitReview(borrowingId: String, rating: Int, comment: String): HomeResult<Review>
    suspend fun getReviewsForBook(bookId: String): HomeResult<List<Review>>
}
