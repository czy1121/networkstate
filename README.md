# NetworkState

判断网络是否可用，判断网络(wifi/mobile/vpn)是否连接，获取网络类型，监听网络可用事件


判断网络是否可用并不保证100%准确

这里网络连接判断只支持 wifi/mobile/vpn，实际上可能还有其它类型的网络

网络已连接也并不等于网络可用，比如当前连接到了Wifi，但这个wifi却上不了网

vpn 连接必须依赖 wifi 或 mobile 才能上网

## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:network-state:0.7.0"
}
```

## API

```kotlin
// 网络是否可用
val Context.isNetworkAvailable: Boolean

// 是否是Vpn连接
val Context.isVpnConnected: Boolean

// 是否是WiFi连接
val Context.isWifiConnected: Boolean

// 是否是移动数据连接
val Context.isMobileConnected: Boolean


object NetworkState {
    // 注册网络状态回调
    fun init(context: Context)

    // 监听网络可用事件
    fun onNetworkAvailable(owner: LifecycleOwner, block: () -> Unit)
}
```

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).