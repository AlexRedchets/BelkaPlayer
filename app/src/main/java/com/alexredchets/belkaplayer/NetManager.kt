package com.alexredchets.belkaplayer

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetManager (private val context: Context) {

    val isConnected: Boolean
        get() {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nw = manager.activeNetwork ?: return false
            val actNw = manager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
}