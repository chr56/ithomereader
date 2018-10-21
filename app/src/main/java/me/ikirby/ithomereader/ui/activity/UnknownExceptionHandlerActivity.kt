package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.unknown_exception_message.*
import me.ikirby.ithomereader.R

class UnknownExceptionHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unknown_exception_message)

        val stackTrace = intent.getStringExtra("stack_trace")

        if (stackTrace == null) {
            finish()
            return
        }

        exception_message.text = stackTrace
    }
}
