package com.example.pocketpilot.core.network

import com.example.pocketpilot.feature.auth.data.remote.AuthApi
import com.example.pocketpilot.feature.finance.data.remote.BudgetApi
import com.example.pocketpilot.feature.finance.data.remote.SavingsGoalApi
import com.example.pocketpilot.feature.finance.data.remote.TransactionApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

/**
 * Assembles the shared OkHttp/Retrofit client and hands out feature APIs.
 * Mirrors the service-locator style used by [FinanceContainer]/[AuthContainer]
 * so a Hilt swap-in later is mechanical.
 */
object NetworkContainer {

    @Volatile
    private var tokenProvider: AuthTokenProvider = AuthTokenProvider { null }

    @Volatile
    private var isDebug: Boolean = true

    fun init(isDebug: Boolean, tokenProvider: AuthTokenProvider = this.tokenProvider) {
        this.isDebug = isDebug
        this.tokenProvider = tokenProvider
    }

    private val loggingInterceptor by lazy {
        NetworkModule.provideLoggingInterceptor(isDebug)
    }

    private val authInterceptor by lazy {
        AuthHeaderInterceptor { tokenProvider.currentAccessToken() }
    }

    private val client: OkHttpClient by lazy {
        NetworkModule.provideOkHttpClient(
            loggingInterceptor = loggingInterceptor,
            authInterceptor = authInterceptor
        )
    }

    private val retrofit: Retrofit by lazy {
        NetworkModule.provideRetrofit(client = client)
    }

    val authApi: AuthApi by lazy { retrofit.create() }
    val transactionApi: TransactionApi by lazy { retrofit.create() }
    val budgetApi: BudgetApi by lazy { retrofit.create() }
    val savingsGoalApi: SavingsGoalApi by lazy { retrofit.create() }
}
