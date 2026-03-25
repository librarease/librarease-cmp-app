package com.example.feature.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.core.R

@Composable
@Suppress("UnusedParameter")
fun HomeScreen(
    uiState: HomeUiState,
    onRefresh: (String?) -> Unit = {},
    onBorrowingClick: (String) -> Unit = {},
    onSubscriptionClick: (String) -> Unit = {},
    onSignOutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val contentState = uiState as? HomeUiState.Content
    val overdueBorrowings = contentState?.borrowings.orEmpty()
        .filter { it.isOverdueBorrowing() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
            .padding(top = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 24.dp)
        ) {
            GreetingHeader()

            if (uiState is HomeUiState.Loading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp)),
                    color = colorResource(id = R.color.accent),
                    trackColor = colorResource(id = R.color.surface_light)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (overdueBorrowings.isNotEmpty()) {
                OverdueBorrowingsSection(
                    overdueBorrowings = overdueBorrowings,
                    subscriptions = contentState?.subscriptions.orEmpty(),
                    onBorrowingClick = onBorrowingClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onClear = { searchQuery = "" }
            )

            Spacer(modifier = Modifier.height(24.dp))
            BorrowingsSection(
                uiState = uiState,
                searchQuery = searchQuery,
                onRetry = { onRefresh(null) },
                onBorrowingClick = onBorrowingClick
            )

            Spacer(modifier = Modifier.height(24.dp))
            SubscriptionsSection(
                uiState = uiState,
                onSubscriptionClick = onSubscriptionClick
            )
        }
    }
}
