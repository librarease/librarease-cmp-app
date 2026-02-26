package com.example.feature.auth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.feature.home.presentation.home.HomeScreen
import com.example.feature.home.presentation.home.HomeViewModel
import com.example.feature.home.presentation.borrowingdetail.BorrowingDetailScreen
import com.example.feature.home.presentation.borrowingdetail.BorrowingDetailViewModel
import com.example.feature.home.presentation.subscriptiondetail.SubscriptionDetailScreen
import com.example.feature.home.presentation.subscriptiondetail.SubscriptionDetailViewModel
import com.example.feature.auth.presentation.signin.SignInScreen
import com.example.feature.auth.presentation.signin.SignInViewModel
import com.example.feature.auth.presentation.signup.SignUpScreen
import com.example.feature.auth.presentation.signup.SignUpViewModel
import com.example.feature.auth.presentation.splash.SplashScreen
import com.example.feature.auth.presentation.splash.SplashViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object SignIn : Screen("signin")
    data object SignUp : Screen("signup")
    data object Home : Screen("home")
    data object BorrowingDetail : Screen("borrowing/{borrowingId}") {
        fun routeForId(borrowingId: String): String = "borrowing/$borrowingId"
    }
    data object SubscriptionDetail : Screen("subscription/{subscriptionId}") {
        fun routeForId(subscriptionId: String): String = "subscription/$subscriptionId"
    }
}

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            val viewModel: SplashViewModel = koinViewModel()
            
            SplashScreen(
                viewModel = viewModel,
                onNavigateToAuth = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignIn.route) {
            val viewModel: SignInViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                    viewModel.consumeSuccess()
                }
            }
            
            SignInScreen(
                uiState = uiState,
                onSignInClick = { email, password ->
                    viewModel.signIn(email, password)
                },
                onEmailChanged = {
                    viewModel.onEmailChanged()
                },
                onPasswordChanged = {
                    viewModel.onPasswordChanged()
                },
                onGoogleSignInClick = {
                    // TODO: Implement Google Sign-In
                },
                onCreateAccountClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onForgotPasswordClick = {
                    // TODO: Implement forgot password
                }
            )
        }

        composable(Screen.SignUp.route) {
            val viewModel: SignUpViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                    viewModel.consumeSuccess()
                }
            }
            
            SignUpScreen(
                viewModel = viewModel,
                onGoogleSignUpClick = {
                    // TODO: Implement Google Sign-Up
                },
                onSignInClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = koinViewModel()
            val homeUiState by homeViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                homeViewModel.loadHome()
            }

            HomeScreen(
                uiState = homeUiState,
                onRefresh = { userId ->
                    homeViewModel.refresh(userId)
                },
                onBorrowingClick = { borrowingId ->
                    navController.navigate(Screen.BorrowingDetail.routeForId(borrowingId))
                },
                onSubscriptionClick = { subscriptionId ->
                    navController.navigate(Screen.SubscriptionDetail.routeForId(subscriptionId))
                },
                onSignOutClick = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.BorrowingDetail.route,
            arguments = listOf(
                navArgument("borrowingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val borrowingId = backStackEntry.arguments?.getString("borrowingId").orEmpty()
            val detailViewModel: BorrowingDetailViewModel = koinViewModel()
            val detailUiState by detailViewModel.uiState.collectAsState()

            LaunchedEffect(borrowingId) {
                detailViewModel.loadBorrowing(borrowingId)
            }

            BorrowingDetailScreen(
                uiState = detailUiState,
                onBackClick = { navController.popBackStack() },
                onRetryClick = { detailViewModel.loadBorrowing(borrowingId) }
            )
        }

        composable(
            route = Screen.SubscriptionDetail.route,
            arguments = listOf(
                navArgument("subscriptionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getString("subscriptionId").orEmpty()
            val detailViewModel: SubscriptionDetailViewModel = koinViewModel()
            val detailUiState by detailViewModel.uiState.collectAsState()

            LaunchedEffect(subscriptionId) {
                detailViewModel.loadSubscription(subscriptionId)
            }

            SubscriptionDetailScreen(
                uiState = detailUiState,
                onBackClick = { navController.popBackStack() },
                onRetryClick = { detailViewModel.loadSubscription(subscriptionId) }
            )
        }
    }
}
