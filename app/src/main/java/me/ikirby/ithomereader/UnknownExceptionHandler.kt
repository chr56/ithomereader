package me.ikirby.ithomereader

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import me.ikirby.ithomereader.ui.activity.UnknownExceptionHandlerActivity
import kotlin.system.exitProcess

object UnknownExceptionHandler {

    fun init(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            handleUncaughtException(context, Log.getStackTraceString(e))
        }
    }

    private fun handleUncaughtException(context: Context, stackTrace: String) {
        context.startActivity(
            Intent(context, UnknownExceptionHandlerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(KEY_STACK_TRACE, stackTrace)
            }
        )

        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}
