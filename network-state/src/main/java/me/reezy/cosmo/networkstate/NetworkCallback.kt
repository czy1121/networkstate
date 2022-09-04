package me.reezy.cosmo.networkstate

import android.net.ConnectivityManager
import android.net.Network
import androidx.annotation.RequiresPermission


internal class NetworkCallback(private val manager: ConnectivityManager, private val onChanged: (Boolean) -> Unit) : ConnectivityManager.NetworkCallback() {

    private var isAvailable: Boolean = false

    @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
    override fun onAvailable(network: Network) {
//        manager.allNetworks.forEach {
//            logE("onAvailable => ${manager.getNetworkCapabilities(it)}")
//        }
        val available = manager.isNetworkAvailable()
//        logE("onAvailable($isAvailable, $available) => ${manager.getNetworkCapabilities(network)}")
        if (!isAvailable && available) {
            onChanged(true)
        }
        isAvailable = available
    }

    @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
    override fun onLost(network: Network) {
        val available = manager.allNetworks.any {
//            logE("onLost => ${manager.getNetworkCapabilities(it)}")
            manager.isNetworkAvailable(it, false)
        }
        val isLost = isAvailable && !available
        if (isLost) {
            onChanged(false)
        }
        isAvailable = available
//        logE("onLost($available)")
    }
}