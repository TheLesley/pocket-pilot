package com.example.pocketpilot.feature.auth.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.auth.data.local.InMemoryAuthTokenStore
import com.example.pocketpilot.feature.auth.data.local.LocalAuthDataSource
import com.example.pocketpilot.feature.auth.data.local.dao.AccountDao
import com.example.pocketpilot.feature.auth.data.remote.AuthRemoteDataSource
import com.example.pocketpilot.feature.auth.data.repository.AuthRepositoryImpl
import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository
import com.example.pocketpilot.feature.auth.domain.repository.AuthTokenStore
import com.example.pocketpilot.feature.auth.domain.usecase.LoginUseCase
import com.example.pocketpilot.feature.auth.domain.usecase.LogoutUseCase
import com.example.pocketpilot.feature.auth.domain.usecase.ObserveAuthSessionUseCase
import com.example.pocketpilot.feature.auth.domain.usecase.RequestPasswordResetUseCase
import com.example.pocketpilot.feature.auth.domain.usecase.ResetPasswordUseCase
import com.example.pocketpilot.feature.auth.domain.usecase.SignUpUseCase
import com.example.pocketpilot.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.example.pocketpilot.feature.auth.presentation.login.LoginViewModel
import com.example.pocketpilot.feature.auth.presentation.resetpassword.ResetPasswordViewModel
import com.example.pocketpilot.feature.auth.presentation.signup.SignUpViewModel
import com.example.pocketpilot.feature.finance.di.FinanceContainer

/**
 * Lightweight service locator that assembles the auth graph.
 *
 * A real Hilt module will replace this in a later phase — the shape of the
 * graph (data source → repository → use cases → view models) already matches
 * what those `@Provides`/`@Binds` methods will look like, so the swap is
 * mechanical.
 */
object AuthContainer {

    @Volatile
    private var accountDao: AccountDao? = null

    /**
     * Wires the auth graph against the shared Room database. Must be called
     * before any use case or repository is touched — [FinanceContainer.init]
     * is invoked here defensively so tests and code paths that only touch the
     * auth container still boot the underlying storage.
     */
    fun init(context: Context) {
        if (accountDao != null) return
        synchronized(this) {
            if (accountDao != null) return
            FinanceContainer.init(context)
            accountDao = FinanceContainer.accountDao
        }
    }

    private val tokenStore: AuthTokenStore by lazy { InMemoryAuthTokenStore() }
    private val remoteDataSource: AuthRemoteDataSource by lazy {
        val dao = checkNotNull(accountDao) {
            "AuthContainer.init(context) must be called before accessing auth use cases."
        }
        LocalAuthDataSource(accountDao = dao)
    }
    val repository: AuthRepository by lazy {
        AuthRepositoryImpl(remote = remoteDataSource, tokenStore = tokenStore)
    }

    val loginUseCase: LoginUseCase by lazy { LoginUseCase(repository) }
    val signUpUseCase: SignUpUseCase by lazy { SignUpUseCase(repository) }
    val requestPasswordResetUseCase: RequestPasswordResetUseCase by lazy {
        RequestPasswordResetUseCase(repository)
    }
    val resetPasswordUseCase: ResetPasswordUseCase by lazy { ResetPasswordUseCase(repository) }
    val logoutUseCase: LogoutUseCase by lazy { LogoutUseCase(repository) }
    val observeAuthSessionUseCase: ObserveAuthSessionUseCase by lazy {
        ObserveAuthSessionUseCase(repository)
    }
}

/**
 * ViewModel factory that reads its collaborators from [AuthContainer]. Pass
 * extra runtime args (like `email` for the reset flow) via [AuthViewModelArgs].
 */
class AuthViewModelFactory(private val args: AuthViewModelArgs = AuthViewModelArgs()) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(AuthContainer.loginUseCase) as T
        modelClass.isAssignableFrom(SignUpViewModel::class.java) ->
            SignUpViewModel(AuthContainer.signUpUseCase) as T
        modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) ->
            ForgotPasswordViewModel(AuthContainer.requestPasswordResetUseCase) as T
        modelClass.isAssignableFrom(ResetPasswordViewModel::class.java) ->
            ResetPasswordViewModel(AuthContainer.resetPasswordUseCase, args.email.orEmpty()) as T
        else -> error("Unknown ViewModel class: ${modelClass.name}")
    }
}

data class AuthViewModelArgs(val email: String? = null)
