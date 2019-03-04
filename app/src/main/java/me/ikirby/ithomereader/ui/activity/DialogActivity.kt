package me.ikirby.ithomereader.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_update_dialog.*
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.KEY_UPDATE_INFO
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.entity.UpdateInfo
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.openLink

class DialogActivity : AppCompatActivity(), View.OnClickListener {

    private var updateInfo: UpdateInfo? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        UiUtil.setNightModeForDialogActivity(this, BaseApplication.isNightMode, BaseApplication.isOStyleLight)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_dialog)

        updateInfo = intent.getParcelableExtra(KEY_UPDATE_INFO)

        if (updateInfo != null) {
            update_info_text.text = updateInfo!!.version + "\n" + updateInfo!!.log

            btn_update.setOnClickListener(this)
            btn_cancel.setOnClickListener(this)
        } else {
            finish()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_update -> {
                openLink(this, updateInfo!!.url)
                finish()
            }
            R.id.btn_cancel -> finish()
        }
    }
}
