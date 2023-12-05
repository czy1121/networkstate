package me.reezy.cosmo.networkstate


enum class NetworkType(val value: String) {
    NO("no"),
    WIFI("wifi"),
    NET_5G("5g"),
    NET_4G("4g"),
    NET_3G("3g"),
    NET_2G("2g"),
    UNKNOWN("unknown"),
}