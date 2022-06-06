package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import me.ikirby.ithomereader.KEY_STACK_TRACE
import me.ikirby.ithomereader.R

class UnknownExceptionHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unknown_exception_message)

        val stackTrace = intent.getStringExtra(KEY_STACK_TRACE)
        if (stackTrace != null) {
            findViewById<TextView>(R.id.exception_message).text = stackTrace
        }
    }
}
