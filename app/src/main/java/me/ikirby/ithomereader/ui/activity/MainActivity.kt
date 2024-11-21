package me.ikirby.ithomereader.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.databinding.ActivityViewpagerBinding
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.fragment.ArticleListFragment
import me.ikirby.ithomereader.ui.fragment.TrendingListFragment
import me.ikirby.ithomereader.ui.task.checkForUpdate
import me.ikirby.ithomereader.ui.task.cleanUp
import me.ikirby.ithomereader.ui.task.clearCache
import me.ikirby.ithomereader.ui.util.ToastUtil

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityViewpagerBinding

    private val startSettings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewpagerBinding.inflate(layoutInflater)
        // make sure binding is inflated before using in [initView()]
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

        binding.viewPager.adapter = adapter

        if (savedInstanceState == null) {
            if (BaseApplication.preferences.getBoolean(SETTINGS_KEY_CHECK_UPDATE_ON_LAUNCH, true)) {
                checkForUpdate(this)
            }
            if (!BaseApplication.preferences.contains(SETTINGS_KEY_VERSION) ||
                BuildConfig.VERSION_CODE > BaseApplication.preferences.getInt(
                        SETTINGS_KEY_VERSION,
                        BuildConfig.VERSION_CODE
                    )
            ) {
                cleanUp(this)
            }

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(false)
            cookieManager.removeAllCookies(null)
        }

        onBackPressedDispatcher.addCallback(this, true) {
            remove()
            BaseApplication.hasSetNightModeManually = false
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun initView() {
        setContentView(binding.root)
        if (BaseApplication.preferences.getBoolean(SETTINGS_KEY_USE_BOTTOM_NAV, false)) {
            binding.tabs.visibility = View.GONE
            if (isNightMode()) {
                binding.bottomNav.setBackgroundColor(getColor(R.color.background_dark))
            } else {
                binding.bottomNav.setBackgroundColor(getColor(R.color.background_light))
            }
            binding.bottomNav.visibility = View.VISIBLE
            binding.viewPager.setSwipeDisabled(true)
            binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    binding.bottomNav.selectedItemId =
                        if (position == 1) R.id.bottom_nav_hot else R.id.bottom_nav_news
                }
            })
            binding.bottomNav.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.bottom_nav_news -> binding.viewPager.setCurrentItem(0, false)
                    R.id.bottom_nav_hot -> binding.viewPager.setCurrentItem(1, false)
                }
                true
            }
        } else {
            supportActionBar?.elevation = 0F
            binding.tabs.setupWithViewPager(binding.viewPager)
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

    override fun swipeRight(): Boolean {
        return false
    }
}
