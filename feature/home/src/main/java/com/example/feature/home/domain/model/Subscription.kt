package com.example.feature.home.domain.model

data class Subscription(
    val id: String,
    val userId: String?,
    val membershipId: String?,
    val libraryId: String?,
    val libraryLogoUrl: String?,
    val createdAt: String?,
    val expiresAt: String?,
    val amount: Int?,
    val finePerDay: Int?,
    val loanPeriod: Int?,
    val activeLoanLimit: Int?,
    val membershipName: String,
    val libraryName: String?
)
