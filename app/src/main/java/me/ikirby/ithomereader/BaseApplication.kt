package me.ikirby.ithomereader

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager

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
}
