package me.ikirby.ithomereader.task

import android.os.AsyncTask
import me.ikirby.ithomereader.util.clearCache

class ClearCacheTask : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg voids: Void): Void? {
        clearCache()
        return null
    }
}
