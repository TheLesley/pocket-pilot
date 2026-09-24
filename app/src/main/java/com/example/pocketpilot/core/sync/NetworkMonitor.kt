package com.example.pocketpilot.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Emits `true` whenever the device has a validated internet connection.
 * Wraps the platform [ConnectivityManager] callback so consumers can react
 * to connectivity changes as a cold [Flow] without touching Android APIs.
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    fun isCurrentlyOnline(): Boolean
}

class ConnectivityManagerNetworkMonitor(private val context: Context) : NetworkMonitor {

    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override val isOnline: Flow<Boolean> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            private val available = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                available += network
                trySend(true)
            }

            override fun onLost(network: Network) {
                available -= network
                trySend(available.isNotEmpty())
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val validated = capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                ) &&
                    capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    )
                if (validated) available += network else available -= network
                trySend(available.isNotEmpty())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)
        trySend(isCurrentlyOnline())

        awaitClose { runCatching { cm.unregisterNetworkCallback(callback) } }
    }.distinctUntilChanged()

    override fun isCurrentlyOnline(): Boolean {
        val cm = connectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
