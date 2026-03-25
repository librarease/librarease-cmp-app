package com.example.librarease.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.example.feature.auth.presentation.signin.SignInScreen
import com.example.feature.auth.presentation.signin.SignInViewModel
import com.example.feature.auth.presentation.signup.SignUpScreen
import com.example.feature.auth.presentation.signup.SignUpViewModel
import com.example.feature.auth.presentation.splash.SplashScreen
import com.example.feature.home.presentation.borrowingdetail.BorrowingDetailScreen
import com.example.feature.home.presentation.borrowingdetail.BorrowingDetailViewModel
import com.example.feature.home.presentation.subscriptiondetail.SubscriptionDetailScreen
import com.example.feature.home.presentation.subscriptiondetail.SubscriptionDetailViewModel
import org.koin.androidx.compose.koinViewModel

sealed class AppScreen(val route: String) {
    data object Splash : AppScreen("splash")
    data object SignIn : AppScreen("signin")
    data object SignUp : AppScreen("signup")
    data object Home : AppScreen("home")
    data object BorrowingDetail : AppScreen("borrowing/{borrowingId}") {
        fun routeForId(borrowingId: String): String = "borrowing/$borrowingId"
    }
    data object SubscriptionDetail : AppScreen("subscription/{subscriptionId}") {
        fun routeForId(subscriptionId: String): String = "subscription/$subscriptionId"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreen.Splash.route
) {
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppScreen.Splash.route) {
            val appStartViewModel: AppStartViewModel = koinViewModel()
            val destination by appStartViewModel.destination.collectAsState()

            LaunchedEffect(Unit) {
                appStartViewModel.start()
            }

            LaunchedEffect(destination) {
                when (destination) {
                    StartDestination.AUTH -> {
                        navController.navigate(AppScreen.SignIn.route) {
                            popUpTo(AppScreen.Splash.route) { inclusive = true }
                        }
                        appStartViewModel.consumeDestination()
                    }
                    StartDestination.HOME -> {
                        navController.navigate(AppScreen.Home.route) {
                            popUpTo(AppScreen.Splash.route) { inclusive = true }
                        }
                        appStartViewModel.consumeDestination()
                    }
                    null -> Unit
                }
            }

            SplashScreen()
        }

        composable(AppScreen.SignIn.route) {
            val viewModel: SignInViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate(AppScreen.Home.route) {
                        popUpTo(AppScreen.SignIn.route) { inclusive = true }
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
                    navController.navigate(AppScreen.SignUp.route)
                },
                onForgotPasswordClick = {
                    // TODO: Implement forgot password
                }
            )
        }

        composable(AppScreen.SignUp.route) {
            val viewModel: SignUpViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate(AppScreen.Home.route) {
                        popUpTo(AppScreen.SignUp.route) { inclusive = true }
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

        composable(AppScreen.Home.route) {
            MainTabsScreen(
                onBorrowingClick = { borrowingId ->
                    navController.navigate(AppScreen.BorrowingDetail.routeForId(borrowingId))
                },
                onSubscriptionClick = { subscriptionId ->
                    navController.navigate(AppScreen.SubscriptionDetail.routeForId(subscriptionId))
                },
                onSignOutClick = {
                    navController.navigate(AppScreen.SignIn.route) {
                        popUpTo(AppScreen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppScreen.BorrowingDetail.route,
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
                onBackClick = { navController.popBackStack(AppScreen.Home.route, false) },
                onRetryClick = { detailViewModel.loadBorrowing(borrowingId) }
            )
        }

        composable(
            route = AppScreen.SubscriptionDetail.route,
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
                onBackClick = { navController.popBackStack(AppScreen.Home.route, false) },
                onRetryClick = { detailViewModel.loadSubscription(subscriptionId) }
            )
        }

    }

    BackHandler(enabled = currentRoute == AppScreen.Home.route) {
        (context as? Activity)?.finish()
    }
}
