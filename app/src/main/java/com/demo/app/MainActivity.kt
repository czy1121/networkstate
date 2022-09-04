package com.demo.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import me.reezy.cosmo.networkstate.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NetworkState.init(this)

        NetworkState.onNetworkAvailable(this) {
            Toast.makeText(this, "network available now", Toast.LENGTH_SHORT).show()
        }

        networkType
        isNetworkAvailable
        isVpnConnected
        isWifiConnected
        isMobileConnected

    }
}