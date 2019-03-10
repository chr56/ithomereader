package me.ikirby.ithomereader.ui.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import me.ikirby.ithomereader.KEY_SYSTEM_UI_VISIBILITY

class ThemeSwitchTransitionActivity : AppCompatActivity() {

    companion object {
        var screenshot: Bitmap? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageView = ImageView(this)
        imageView.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(imageView)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = intent.getIntExtra(KEY_SYSTEM_UI_VISIBILITY, 0)

        if (screenshot == null || screenshot!!.isRecycled) {
            throw IllegalArgumentException("screenshot bitmap is null or recycled")
        }
        imageView.setImageBitmap(screenshot)

        Handler().postDelayed({
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 100)
    }

    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
        screenshot?.recycle()
    }
}