package com.example.kemono.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class NetworkMonitor @Inject constructor(@ApplicationContext private val context: Context) {
    val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        trySend(true)
                    }
                    override fun onLost(network: Network) {
                        trySend(false)
                    }
                }

        connectivityManager.registerDefaultNetworkCallback(callback)

        // Initial state
        trySend(connectivityManager.activeNetwork != null)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
