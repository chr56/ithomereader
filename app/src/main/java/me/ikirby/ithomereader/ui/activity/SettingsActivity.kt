package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.fragment.SettingsFragment

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.settings))
        enableBackBtn()

        supportFragmentManager.beginTransaction().add(android.R.id.content, SettingsFragment()).commit()

        onBackPressedDispatcher.addCallback(this, true) {
            remove()
            BaseApplication.instance.loadPreferences()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return false
    }

}
