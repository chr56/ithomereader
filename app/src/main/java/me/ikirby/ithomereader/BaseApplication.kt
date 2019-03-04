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
        isNightMode = preferences.getBoolean(SETTINGS_KEY_NIGHT_MODE, false)
        isOStyleLight = preferences.getBoolean(SETTINGS_KEY_WHITE_THEME, false)
        isGestureEnabled = preferences.getBoolean(SETTINGS_KEY_SWIPE_GESTURE, true)
    }

    fun setNightMode() {
        if (hasCheckedAutoNightMode) return
        if (preferences.getBoolean(SETTINGS_KEY_AUTO_NIGHT_MODE, false)) {
            val startTime = preferences.getString(SETTINGS_KEY_NIGHT_MODE_START_TIME, "22:00")
            val endTime = preferences.getString(SETTINGS_KEY_NIGHT_MODE_END_TIME, "07:00")
            isNightMode = shouldEnableNightMode(startTime, endTime)
            preferences.edit().putBoolean(SETTINGS_KEY_NIGHT_MODE, isNightMode).apply()
        }
        hasCheckedAutoNightMode = true
    }

    fun switchNightMode() {
        isNightMode = !isNightMode
        preferences.edit().putBoolean(SETTINGS_KEY_NIGHT_MODE, isNightMode).apply()
    }
}
