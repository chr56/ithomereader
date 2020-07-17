package me.ikirby.ithomereader.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_viewpager.*
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.task.CleanUpTask
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.fragment.ArticleListFragment
import me.ikirby.ithomereader.ui.fragment.TrendingListFragment
import me.ikirby.ithomereader.ui.task.checkForUpdate
import me.ikirby.ithomereader.ui.task.clearCache
import me.ikirby.ithomereader.ui.util.ToastUtil

class MainActivity : BaseActivity() {

    private val startSettings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.app_name))
        isGestureEnabled = false

        val fragments = listOf(
            ArticleListFragment(),
            TrendingListFragment()
        )

        val adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount(): Int {
                return fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return if (position == 1) {
                    getString(R.string.trending)
                } else {
                    getString(R.string.news)
                }
            }
        }

        viewPager!!.adapter = adapter

        if (savedInstanceState == null) {
            if (BaseApplication.preferences.getBoolean(SETTINGS_KEY_CHECK_UPDATE_ON_LAUNCH, true)) {
                checkForUpdate(this)
            }
            if (!BaseApplication.preferences.contains(SETTINGS_KEY_VERSION)
                || BuildConfig.VERSION_CODE > BaseApplication.preferences.getInt(
                    SETTINGS_KEY_VERSION,
                    BuildConfig.VERSION_CODE
                )
            ) {
                CleanUpTask().execute()
            }

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(false)
            cookieManager.removeAllCookies(null)
        }
    }

    override fun initView() {
        setContentView(R.layout.activity_viewpager)
        if (BaseApplication.preferences.getBoolean(SETTINGS_KEY_USE_BOTTOM_NAV, false)) {
            tabs.visibility = View.GONE
            if (isNightMode()) {
                bottom_nav.setBackgroundColor(getColor(R.color.background_dark))
            } else {
                bottom_nav.setBackgroundColor(getColor(R.color.background_light))
            }
            bottom_nav.visibility = View.VISIBLE
            viewPager.setSwipeDisabled(true)
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == 1) {
                        bottom_nav.selectedItemId = R.id.bottom_nav_hot
                    } else {
                        bottom_nav.selectedItemId = R.id.bottom_nav_news
                    }
                }

            })
            bottom_nav.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.bottom_nav_news -> viewPager.setCurrentItem(0, false)
                    R.id.bottom_nav_hot -> viewPager.setCurrentItem(1, false)
                }
                true
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.navigationBarColor = Color.TRANSPARENT
                container.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
            supportActionBar?.elevation = 0F
            tabs.setupWithViewPager(viewPager)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_action, menu)

        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.queryHint = getString(R.string.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                if (s != "") {
                    val intent = Intent(this@MainActivity, SearchActivity::class.java)
                    intent.putExtra(KEY_KEYWORD, s)
                    startActivity(intent)
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

        menu.findItem(R.id.action_night_mode)
            .setTitle(if (isNightMode()) R.string.day_mode else R.string.pref_night_mode)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> return false
            R.id.action_clearcache -> {
                ToastUtil.showToast(R.string.cache_clearing)
                clearCache(this)
            }
            R.id.action_settings -> startSettings.launch(Intent(this, SettingsActivity::class.java))
            R.id.action_night_mode -> {
                BaseApplication.hasSetNightModeManually = true
                val defaultNightMode = if (isNightMode()) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
                AppCompatDelegate.setDefaultNightMode(defaultNightMode)
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        BaseApplication.hasSetNightModeManually = false
    }

    override fun swipeRight(): Boolean {
        return false
    }
}
