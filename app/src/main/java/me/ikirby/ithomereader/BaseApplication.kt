package me.ikirby.ithomereader

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class BaseApplication : Application() {

    companion object {
        lateinit var instance: BaseApplication
        lateinit var preferences: SharedPreferences
            private set
        var isGestureEnabled: Boolean = false
            private set
        var hasSetNightModeManually = false
    }

    override fun onCreate() {
        super.onCreate()
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return
//        }
//        LeakCanary.install(this)
        UnknownExceptionHandler.init(this)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        loadPreferences()

        applyNightMode()

        instance = this
    }

    fun loadPreferences() {
        isGestureEnabled = preferences.getBoolean(SETTINGS_KEY_SWIPE_GESTURE, true)
    }

    fun applyNightMode() {
        val defaultNightMode = when (preferences.getString(SETTINGS_KEY_APPCOMPAT_NIGHT_MODE, "MODE_NIGHT_FOLLOW_SYSTEM")) {
            "MODE_NIGHT_FOLLOW_SYSTEM" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "MODE_NIGHT_NO" -> AppCompatDelegate.MODE_NIGHT_NO
            "MODE_NIGHT_YES" -> AppCompatDelegate.MODE_NIGHT_YES
            "MODE_NIGHT_AUTO_BATTERY" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            else -> AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        }
        AppCompatDelegate.setDefaultNightMode(defaultNightMode)
    }
}
