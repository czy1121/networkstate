@file:Suppress("DEPRECATION")

package me.reezy.cosmo.networkstate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 网络是否可用
 */
val Context.isNetworkAvailable: Boolean
    @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
    get() = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java)?.isNetworkAvailable() ?: false


/**
 * 是否是Vpn连接
 */
val Context.isVpnConnected: Boolean
    @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
    get() {
        val manager = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.getNetworkCapabilities(manager.activeNetwork)?.hasInternetValidatedTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        } else {
            manager.activeNetworkInfo?.type == ConnectivityManager.TYPE_VPN
        }
    }

/**
 * 是否是WiFi连接
 */
val Context.isWifiConnected: Boolean
    @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
    get() {
        val manager = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java) ?: return false
        // SDK_INT < Q 且使用VPN时，hasTransport(NetworkCapabilities.TRANSPORT_WIFI) 返回值错误
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.getNetworkCapabilities(manager.activeNetwork)?.hasInternetValidatedTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        } else {
            manager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }

/**
 * 是否是移动数据连接
 */
val Context.isMobileConnected: Boolean
    @RequiresPermission(allOf = ["android.permission.ACCESS_NETWORK_STATE"])
    get() {
        val manager = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java) ?: return false
        // SDK_INT < Q 且使用VPN时，hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) 返回值错误
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.getNetworkCapabilities(manager.activeNetwork)?.hasInternetValidatedTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
        } else {
            manager.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
        }
    }

@RequiresApi(Build.VERSION_CODES.M)
private fun NetworkCapabilities.hasInternetValidatedTransport(transportType: Int): Boolean {
    return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) and hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) and hasTransport(transportType)
}



/**
 * 获取当前网络类型
 */
val Context.networkType: NetworkType
    @RequiresPermission(allOf = ["android.permission.ACCESS_NETWORK_STATE"])
    get() {
        val manager = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java) ?: return NetworkType.NO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val caps = manager.getNetworkCapabilities(manager.activeNetwork) ?: return NetworkType.NO
            val hasInternetValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) and caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            return when {
                !hasInternetValidated -> NetworkType.NO
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> getNetworkType(dataNetworkType)
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        manager.deprecatedNetworkType
                    } else {
                        NetworkType.NO
                    }
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> manager.deprecatedNetworkType
                else -> NetworkType.UNKNOWN
            }
        } else {
            return manager.deprecatedNetworkType
        }
    }


@RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
internal fun ConnectivityManager.isNetworkAvailable(network: Network? = null, canVpn: Boolean = true): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val caps = getNetworkCapabilities(network ?: activeNetwork) ?: return false
        val hasInternetValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) and caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return when {
            !hasInternetValidated -> false
            !canVpn && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    activeNetworkInfo?.isConnected ?: false
                } else {
                    false
                }
            }
            else -> true
        }
    }
    return activeNetworkInfo?.isConnected ?: false
}


private val Context.dataNetworkType: Int
    @RequiresPermission(allOf = ["android.permission.ACCESS_NETWORK_STATE", "android.permission.READ_PHONE_STATE"])
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return ContextCompat.getSystemService(applicationContext, TelephonyManager::class.java)?.dataNetworkType ?: TelephonyManager.NETWORK_TYPE_UNKNOWN
        }
        return ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java)?.activeNetworkInfo?.subtype ?: TelephonyManager.NETWORK_TYPE_UNKNOWN
    }

private val ConnectivityManager.deprecatedNetworkType: NetworkType
    @RequiresPermission(allOf = ["android.permission.ACCESS_NETWORK_STATE"])
    get() {
        val info = activeNetworkInfo ?: return NetworkType.NO
        return when {
            !info.isConnected or !info.isAvailable -> NetworkType.NO
            info.type == ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
            info.type == ConnectivityManager.TYPE_MOBILE -> getNetworkType(info.subtype)
            else -> NetworkType.UNKNOWN
        }
    }

private fun getNetworkType(dataNetworkType: Int?) = when (dataNetworkType) {
    TelephonyManager.NETWORK_TYPE_NR -> NetworkType.NET_5G

    TelephonyManager.NETWORK_TYPE_IWLAN,
    TelephonyManager.NETWORK_TYPE_LTE
    -> NetworkType.NET_4G

    TelephonyManager.NETWORK_TYPE_TD_SCDMA,
    TelephonyManager.NETWORK_TYPE_EVDO_A,
    TelephonyManager.NETWORK_TYPE_UMTS,
    TelephonyManager.NETWORK_TYPE_EVDO_0,
    TelephonyManager.NETWORK_TYPE_HSDPA,
    TelephonyManager.NETWORK_TYPE_HSUPA,
    TelephonyManager.NETWORK_TYPE_HSPA,
    TelephonyManager.NETWORK_TYPE_EVDO_B,
    TelephonyManager.NETWORK_TYPE_EHRPD,
    TelephonyManager.NETWORK_TYPE_HSPAP
    -> NetworkType.NET_3G

    TelephonyManager.NETWORK_TYPE_GSM,
    TelephonyManager.NETWORK_TYPE_GPRS,
    TelephonyManager.NETWORK_TYPE_CDMA,
    TelephonyManager.NETWORK_TYPE_EDGE,
    TelephonyManager.NETWORK_TYPE_1xRTT,
    TelephonyManager.NETWORK_TYPE_IDEN
    -> NetworkType.NET_2G


    else -> NetworkType.UNKNOWN
}
