package me.ikirby.ithomereader

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import me.ikirby.ithomereader.util.shouldEnableNightMode

class BaseApplication : Application() {

    companion object {
        lateinit var instance: BaseApplication
        lateinit var preferences: SharedPreferences
            private set
        var isNightMode: Boolean = false
            private set
        var isOStyleLight: Boolean = false
            private set
        var isGestureEnabled: Boolean = false
            private set
        var hasCheckedAutoNightMode: Boolean = false
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
        instance = this
    }

    fun loadPreferences() {
        isNightMode = preferences.getBoolean("night_mode", false)
        isOStyleLight = preferences.getBoolean("o_style_light", false)
        isGestureEnabled = preferences.getBoolean("swipe_back", true)
    }

    fun setNightMode() {
        if (hasCheckedAutoNightMode) return
        if (preferences.getBoolean("auto_switch_night_mode", false)) {
            val startTime = preferences.getString("night_mode_start_time", "22:00")
            val endTime = preferences.getString("night_mode_end_time", "07:00")
            isNightMode = shouldEnableNightMode(startTime, endTime)
            preferences.edit().putBoolean("night_mode", isNightMode).apply()
        }
        hasCheckedAutoNightMode = true
    }
}
