package me.ikirby.ithomereader.task

import android.os.AsyncTask
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.BuildConfig
import me.ikirby.ithomereader.SETTINGS_KEY_CUSTOM_FILTER
import me.ikirby.ithomereader.SETTINGS_KEY_VERSION
import me.ikirby.ithomereader.util.clearCache

class CleanUpTask : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg voids: Void): Void? {
        if (BaseApplication.preferences.getInt(SETTINGS_KEY_VERSION, BuildConfig.VERSION_CODE) <= 146) {
            val customFilter = BaseApplication.preferences
                .getString(SETTINGS_KEY_CUSTOM_FILTER, "")!!
                .split(", ").toList()
            BaseApplication.preferences.edit()
                .putString(SETTINGS_KEY_CUSTOM_FILTER, customFilter.joinToString(","))
                .apply()
        }
        clearCache()
        return null
    }
}
