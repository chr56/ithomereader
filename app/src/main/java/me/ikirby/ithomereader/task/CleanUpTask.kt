package me.ikirby.ithomereader.task

import android.os.AsyncTask
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.util.clearCache

class CleanUpTask : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg voids: Void): Void? {
        if (BaseApplication.preferences.getInt(SETTINGS_KEY_VERSION, BuildConfig.VERSION_CODE) <= 146) {
            val customFilter = BaseApplication.preferences
                .getString(SETTINGS_KEY_CUSTOM_FILTER, "")!!
                .split(", ").filter { it.isNotEmpty() }
            BaseApplication.preferences.edit()
                .putString(SETTINGS_KEY_CUSTOM_FILTER, customFilter.joinToString(","))
                .apply()
        }
        clearCache()
        BaseApplication.preferences.edit()
            .putInt(SETTINGS_KEY_VERSION, BuildConfig.VERSION_CODE)
            .remove(SETTINGS_KEY_NIGHT_MODE)
            .remove(SETTINGS_KEY_AUTO_NIGHT_MODE)
            .remove(SETTINGS_KEY_WHITE_THEME)
            .apply()
        return null
    }
}
