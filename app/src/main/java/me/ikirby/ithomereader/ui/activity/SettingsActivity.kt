package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.fragment.SettingsFragment
import me.ikirby.ithomereader.ui.util.UiUtil

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.settings))
        enableBackBtn()

        findViewById<View>(android.R.id.content)
                .setBackgroundResource(UiUtil.getWindowBackgroundColorRes())

        supportFragmentManager.beginTransaction().add(android.R.id.content,
                SettingsFragment()).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return false
    }
}
