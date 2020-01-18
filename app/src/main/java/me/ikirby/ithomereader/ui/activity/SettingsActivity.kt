package me.ikirby.ithomereader.ui.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.fragment.SettingsFragment

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.settings))
        enableBackBtn()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val containerView = findViewById<ViewGroup>(android.R.id.content)
            containerView.clipChildren = false
            containerView.clipToPadding = false

            window.navigationBarColor = Color.TRANSPARENT
            containerView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

            ViewCompat.setOnApplyWindowInsetsListener(containerView) { v, insets ->
                v.updatePadding(top = insets.systemWindowInsets.top, bottom = insets.systemWindowInsets.bottom)
                insets
            }
        }

        supportFragmentManager.beginTransaction().add(android.R.id.content, SettingsFragment()).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return false
    }

    override fun onBackPressed() {
        BaseApplication.instance.loadPreferences()
        super.onBackPressed()
    }
}
