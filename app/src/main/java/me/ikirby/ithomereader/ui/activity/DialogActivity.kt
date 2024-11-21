package me.ikirby.ithomereader.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.KEY_UPDATE_INFO
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.SETTINGS_KEY_IGNORE_VERSION_CODE
import me.ikirby.ithomereader.databinding.ActivityUpdateDialogBinding
import me.ikirby.ithomereader.entity.UpdateInfo
import me.ikirby.ithomereader.ui.util.CompatibilityUtil.parcelableExtra
import me.ikirby.ithomereader.util.openLink

class DialogActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUpdateDialogBinding
    private var updateInfo: UpdateInfo? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDialogBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_update_dialog)

        updateInfo = intent.parcelableExtra(KEY_UPDATE_INFO)

        if (updateInfo != null) {
            binding.updateInfoText.text = updateInfo!!.version + "\n" + updateInfo!!.log

            binding.btnUpdate.setOnClickListener(this)
            binding.btnCancel.setOnClickListener(this)
            binding.btnIgnore.setOnClickListener(this)
        } else {
            finish()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_update -> {
                openLink(this, updateInfo!!.url)
            }
            R.id.btn_cancel -> finish()
            R.id.btn_ignore -> BaseApplication.preferences.edit().putInt(
                SETTINGS_KEY_IGNORE_VERSION_CODE,
                updateInfo!!.versionCode
            ).apply()
        }
        finish()
    }
}
