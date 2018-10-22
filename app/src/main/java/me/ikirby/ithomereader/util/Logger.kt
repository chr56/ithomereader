package me.ikirby.ithomereader.util

import android.util.Log
import me.ikirby.ithomereader.BuildConfig

object Logger {
    private var isLoggingEnabled = BuildConfig.DEBUG

    fun e(tag: String, msg: String) {
        if (isLoggingEnabled) {
            Log.e(tag, msg)
        }
    }

    fun e(tag: String, msg: String, e: Throwable) {
        if (isLoggingEnabled) {
            Log.e(tag, msg, e)
        }
    }

    fun d(tag: String, msg: String) {
        if (isLoggingEnabled) {
            Log.d(tag, msg)
        }
    }

    fun d(tag: String, msg: String, e: Throwable) {
        if (isLoggingEnabled) {
            Log.d(tag, msg, e)
        }
    }
}