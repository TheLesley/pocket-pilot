package com.example.pocketpilot

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.pocketpilot.core.designsystem.component.AdaptiveNavItem
import com.example.pocketpilot.core.designsystem.component.AdaptiveNavScaffold
import com.example.pocketpilot.core.designsystem.component.SyncStatusBar
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.sync.SyncWorkStatus
import com.example.pocketpilot.feature.auth.presentation.AuthNavHost
import com.example.pocketpilot.feature.dashboard.presentation.HomeNavHost
import com.example.pocketpilot.feature.dashboard.presentation.HomeTab
import com.example.pocketpilot.feature.dashboard.presentation.rememberHomeNavController
import com.example.pocketpilot.feature.finance.di.SyncContainer
import com.example.pocketpilot.feature.finance.di.SyncWorkContainer
import com.example.pocketpilot.feature.security.presentation.AppLockGate

/**
 * Hosts the root Compose graph. Extends [FragmentActivity] instead of the
 * bare `ComponentActivity` because AndroidX BiometricPrompt anchors its
 * dialog to a `FragmentActivity`; the composable that shows the biometric
 * prompt resolves this activity from `LocalContext`.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kick off connectivity-driven sync for the lifetime of the Activity so
        // an in-app "back online" transition can flush pending mutations
        // without waiting for the next periodic WorkManager window.
        SyncContainer.manager.startAutoSync(lifecycleScope)
        enableEdgeToEdge()
        setContent {
            PocketPilotTheme {
                AppLockGate {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            val scheduler = remember { SyncWorkContainer.workScheduler }
                            val status: SyncWorkStatus by scheduler.status
                                .collectAsStateWithLifecycle(initialValue = SyncWorkStatus.Idle)
                            SyncStatusBar(
                                status = status,
                                onSyncNowClick = { scheduler.enqueueOneTimeSync() },
                            )
                            RootAppContent()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun RootAppContent() {
    var authenticated by remember { mutableStateOf(false) }
    AnimatedContent(
        targetState = authenticated,
        label = "AuthGate",
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
        },
    ) { isAuthenticated ->
        if (!isAuthenticated) {
            AuthNavHost(onAuthenticated = { authenticated = true })
        } else {
            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = remember(context) { context.findActivity() }
            val windowSizeClass = calculateWindowSizeClass(activity)
            val homeNavController = rememberHomeNavController()
            val currentTab = homeNavController.destination.tab
            val navItems = listOf(
                AdaptiveNavItem(
                    label = "Dashboard",
                    icon = Icons.Filled.Home,
                    selected = currentTab == HomeTab.Dashboard,
                    onClick = { homeNavController.selectTab(HomeTab.Dashboard) },
                ),
                AdaptiveNavItem(
                    label = "Transactions",
                    icon = Icons.Filled.Receipt,
                    selected = currentTab == HomeTab.Transactions,
                    onClick = { homeNavController.selectTab(HomeTab.Transactions) },
                ),
            )
            AdaptiveNavScaffold(windowSizeClass = windowSizeClass, items = navItems) {
                HomeNavHost(controller = homeNavController)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("Expected an Activity context but got ${this::class.java.name}")
}
