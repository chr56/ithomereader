package me.ikirby.ithomereader.task

import android.os.AsyncTask
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.util.clearCache

class CleanUpTask : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg voids: Void): Void? {
        BaseApplication.preferences.edit()
            .remove("use_square_launcher_icon")
            .remove("show_thumb")
            .apply()
        clearCache()
        return null
    }
}
