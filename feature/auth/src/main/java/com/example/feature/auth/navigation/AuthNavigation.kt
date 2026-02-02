package com.example.feature.auth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feature.auth.presentation.home.HomeScreen
import com.example.feature.auth.presentation.signin.SignInScreen
import com.example.feature.auth.presentation.signin.SignInUiState
import com.example.feature.auth.presentation.signin.SignInViewModel
import com.example.feature.auth.presentation.signup.SignUpScreen
import com.example.feature.auth.presentation.signup.SignUpUiState
import com.example.feature.auth.presentation.signup.SignUpViewModel
import com.example.feature.auth.presentation.splash.SplashScreen
import com.example.feature.auth.presentation.splash.SplashViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object SignIn : Screen("signin")
    data object SignUp : Screen("signup")
    data object Home : Screen("home")
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
            
            LaunchedEffect(uiState) {
                if (uiState is SignInUiState.Success) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            }
            
            SignInScreen(
                onSignInClick = { email, password ->
                    viewModel.signIn(email, password)
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
            
            LaunchedEffect(uiState) {
                if (uiState is SignUpUiState.Success) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            }
            
            SignUpScreen(
                onSignUpClick = { name, email, password ->
                    viewModel.signUp(name, email, password)
                },
                onGoogleSignUpClick = {
                    // TODO: Implement Google Sign-Up
                },
                onSignInClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSignOutClick = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
