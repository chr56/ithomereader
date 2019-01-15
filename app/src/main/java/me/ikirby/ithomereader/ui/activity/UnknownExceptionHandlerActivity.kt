package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.unknown_exception_message.*
import me.ikirby.ithomereader.KEY_STACK_TRACE
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.task.UpdateCheckNotifyTask

class UnknownExceptionHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unknown_exception_message)

        btn_check_for_update.setOnClickListener {
            UpdateCheckNotifyTask(true).execute()
        }

        val stackTrace = intent.getStringExtra(KEY_STACK_TRACE)
        if (stackTrace != null) {
            exception_message.text = stackTrace
        }
    }
}
