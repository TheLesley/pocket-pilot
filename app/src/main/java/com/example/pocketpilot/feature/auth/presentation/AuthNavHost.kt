package com.example.pocketpilot.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.auth.di.AuthViewModelArgs
import com.example.pocketpilot.feature.auth.di.AuthViewModelFactory
import com.example.pocketpilot.feature.auth.presentation.forgotpassword.ForgotPasswordRoute
import com.example.pocketpilot.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.example.pocketpilot.feature.auth.presentation.login.LoginRoute
import com.example.pocketpilot.feature.auth.presentation.login.LoginViewModel
import com.example.pocketpilot.feature.auth.presentation.resetpassword.ResetPasswordRoute
import com.example.pocketpilot.feature.auth.presentation.resetpassword.ResetPasswordViewModel
import com.example.pocketpilot.feature.auth.presentation.signup.SignUpRoute
import com.example.pocketpilot.feature.auth.presentation.signup.SignUpViewModel
import com.example.pocketpilot.feature.auth.presentation.welcome.WelcomeScreen

/**
 * Screens participating in the pre-auth graph. Kept as a plain enum so the
 * feature can render without depending on androidx.navigation — that
 * dependency will be introduced in a later phase when the full app graph
 * lands.
 */
enum class AuthDestination { Welcome, Login, SignUp, ForgotPassword, ResetPassword }

@Composable
fun AuthNavHost(onAuthenticated: () -> Unit) {
    var destination by rememberSaveable { mutableStateOf(AuthDestination.Welcome) }
    var resetEmail by rememberSaveable { mutableStateOf("") }

    when (destination) {
        AuthDestination.Welcome -> WelcomeScreen(
            onLoginClick = { destination = AuthDestination.Login },
            onSignUpClick = { destination = AuthDestination.SignUp }
        )
        AuthDestination.Login -> {
            val vm: LoginViewModel = viewModel(
                key = "auth.login",
                factory = remember { AuthViewModelFactory() }
            )
            LoginRoute(
                viewModel = vm,
                onLoggedIn = onAuthenticated,
                onNavigateToSignUp = { destination = AuthDestination.SignUp },
                onNavigateToForgotPassword = { destination = AuthDestination.ForgotPassword }
            )
        }
        AuthDestination.SignUp -> {
            val vm: SignUpViewModel = viewModel(
                key = "auth.signup",
                factory = remember { AuthViewModelFactory() }
            )
            SignUpRoute(
                viewModel = vm,
                onSignedUp = onAuthenticated,
                onNavigateToLogin = { destination = AuthDestination.Login }
            )
        }
        AuthDestination.ForgotPassword -> {
            val vm: ForgotPasswordViewModel = viewModel(
                key = "auth.forgotPassword",
                factory = remember { AuthViewModelFactory() }
            )
            ForgotPasswordRoute(
                viewModel = vm,
                onNavigateToReset = { email ->
                    resetEmail = email
                    destination = AuthDestination.ResetPassword
                },
                onNavigateBack = { destination = AuthDestination.Login }
            )
        }
        AuthDestination.ResetPassword -> {
            val emailForReset = resetEmail
            val vm: ResetPasswordViewModel = viewModel(
                key = "auth.resetPassword.$emailForReset",
                factory = remember(emailForReset) {
                    AuthViewModelFactory(AuthViewModelArgs(email = emailForReset))
                }
            )
            ResetPasswordRoute(
                viewModel = vm,
                onPasswordReset = onAuthenticated,
                onNavigateToLogin = { destination = AuthDestination.Login }
            )
        }
    }
}
