package com.example.librarease.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.activity.ComponentActivity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.R
import com.example.feature.books.presentation.books.BooksScreen
import com.example.feature.books.presentation.books.BooksViewModel
import com.example.feature.home.presentation.home.HomeScreen
import com.example.feature.home.presentation.home.HomeViewModel
import com.example.feature.home.presentation.home.MainBottomNavigationBar
import com.example.feature.home.presentation.home.MainTabRoutes
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainTabsScreen(
    onBorrowingClick: (String) -> Unit,
    onSubscriptionClick: (String) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: MainTabRoutes.HOME

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
    ) {
        NavHost(
            navController = tabNavController,
            startDestination = MainTabRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp)
        ) {
            composable(MainTabRoutes.HOME) {
                val owner = (LocalContext.current as? ComponentActivity)
                    ?: LocalViewModelStoreOwner.current
                    ?: error("No ViewModelStoreOwner found for Home tab")
                val homeViewModel: HomeViewModel = koinViewModel(viewModelStoreOwner = owner)
                val homeUiState by homeViewModel.uiState.collectAsState()

                LaunchedEffect(homeUiState) {
                    if (homeUiState is com.example.feature.home.presentation.home.HomeUiState.Idle) {
                        homeViewModel.loadHome()
                    }
                }
                LaunchedEffect(Unit) {
                    if (homeUiState is com.example.feature.home.presentation.home.HomeUiState.Content) {
                        homeViewModel.refresh(null)
                    }
                }

                HomeScreen(
                    uiState = homeUiState,
                    onRefresh = { userId -> homeViewModel.refresh(userId) },
                    onBorrowingClick = onBorrowingClick,
                    onSubscriptionClick = onSubscriptionClick,
                    onSignOutClick = onSignOutClick
                )
            }
            composable(MainTabRoutes.BOOKS) {
                val owner = (LocalContext.current as? ComponentActivity)
                    ?: LocalViewModelStoreOwner.current
                    ?: error("No ViewModelStoreOwner found for Books tab")
                val booksViewModel: BooksViewModel = koinViewModel(viewModelStoreOwner = owner)
                val booksUiState by booksViewModel.uiState.collectAsState()

                LaunchedEffect(booksUiState.books.size, booksUiState.isLoading) {
                    if (booksUiState.books.isEmpty() && !booksUiState.isLoading) {
                        booksViewModel.loadBooks()
                    }
                }

                BooksScreen(
                    uiState = booksUiState,
                    onLoadMore = { booksViewModel.loadMore() },
                    onRetry = { booksViewModel.retry() }
                )
            }
            composable(MainTabRoutes.LIBRARIES) {
                PlaceholderScreen(title = "Libraries")
            }
            composable(MainTabRoutes.SETTINGS) {
                PlaceholderScreen(title = "Settings")
            }
        }

        MainBottomNavigationBar(
            currentRoute = currentRoute,
            onSelected = { targetRoute ->
                tabNavController.navigate(targetRoute) {
                    popUpTo(MainTabRoutes.HOME) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title (Coming soon)",
            color = colorResource(id = R.color.secondary),
            fontSize = 14.sp
        )
    }
}
