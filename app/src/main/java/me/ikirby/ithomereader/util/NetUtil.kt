package me.ikirby.ithomereader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import me.ikirby.ithomereader.BaseApplication

private fun isWiFiConnected(): Boolean {
    val manager = BaseApplication.instance
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ?: false
    } else {
        val info = manager.activeNetworkInfo
        @Suppress("DEPRECATION")
        info != null && info.type == ConnectivityManager.TYPE_WIFI
    }
}

fun shouldLoadImageAutomatically(): Boolean {
    val preferences = BaseApplication.preferences
    when (preferences.getString("load_image_condition", "0")) {
        "0" -> return true
        "1" -> return isWiFiConnected()
        "2" -> return false
    }
    return false
}

fun shouldShowThumb(): Boolean {
    val preferences = BaseApplication.preferences
    when (preferences.getString("show_thumb_condition", "0")) {
        "0" -> return true
        "1" -> return isWiFiConnected()
        "2" -> return false
    }
    return false
}
