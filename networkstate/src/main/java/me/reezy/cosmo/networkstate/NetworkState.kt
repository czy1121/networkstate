package me.reezy.cosmo.networkstate

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object NetworkState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val flowAvailable = MutableSharedFlow<Any>() 

    private lateinit var callback: ConnectivityManager.NetworkCallback

    @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
    fun init(context: Context) {
        val manager = ContextCompat.getSystemService(context.applicationContext, ConnectivityManager::class.java) ?: return
        if (!this::callback.isInitialized) {
            callback = NetworkCallback(manager) {
                scope.launch {
                    flowAvailable.emit(Any())
                }
            }
        } else {
            manager.unregisterNetworkCallback(callback)
        }
        manager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
    }

    fun onNetworkAvailable(owner: LifecycleOwner, block: () -> Unit) {
        owner.lifecycleScope.launch {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flowAvailable.collect {
                    block()
                }
            }
        }
    }
}