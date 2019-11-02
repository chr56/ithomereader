package me.ikirby.ithomereader

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import kotlin.system.exitProcess

object UnknownExceptionHandler {

    fun init(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            handleUncaughtException(context, Log.getStackTraceString(e))
        }
    }

    private fun handleUncaughtException(context: Context, stackTrace: String) {
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "me.ikirby.ithomereader.UNKNOWN_EXCEPTION"
            putExtra(KEY_STACK_TRACE, stackTrace)
        }
        context.startActivity(intent)

        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}
