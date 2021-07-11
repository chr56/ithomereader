package me.ikirby.ithomereader.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import me.ikirby.ithomereader.KEY_NEWS_ID
import me.ikirby.ithomereader.KEY_TITLE
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.ActivityViewpagerBinding
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.databinding.viewmodel.CommentsActivityViewModel
import me.ikirby.ithomereader.ui.fragment.AllCommentFragment
import me.ikirby.ithomereader.ui.fragment.HotCommentFragment
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.encryptString

class CommentsActivity : BaseActivity(), ViewPager.OnPageChangeListener {

    private val viewModel by lazy { ViewModelProvider(this).get(CommentsActivityViewModel::class.java) }
    private lateinit var binding: ActivityViewpagerBinding

    private val postComment =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                goToAllComments()
                val refreshBtn = findViewById<View>(R.id.action_refresh)
                refreshBtn?.callOnClick()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.comments))
        enableBackBtn()
        supportActionBar?.elevation = 0F

        val newsId = intent.getStringExtra(KEY_NEWS_ID)
        Logger.d("CommentsActivity", "newsId = $newsId")
        if (newsId != null) {
            if (savedInstanceState == null) {
                viewModel.newsId.value = newsId
                viewModel.newsIdEncrypted.value = encryptString(newsId)
                viewModel.newsTitle.value = intent.getStringExtra(KEY_TITLE)
                viewModel.newsUrl.value = intent.getStringExtra(KEY_URL)
                viewModel.loadComment(hot = true, all = true, refresh = true)
            }
        } else {
            finish()
        }
    }

    override fun initView() {
        binding = ActivityViewpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragments = listOf(
            HotCommentFragment(),
            AllCommentFragment()
        )

        val adapter = object :
            FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount(): Int {
                return fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return if (position == 1) {
                    getString(R.string.all_comments)
                } else {
                    getString(R.string.hot_comments)
                }
            }
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.addOnPageChangeListener(this)
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.comments_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_refresh -> return false
        }
        return true
    }

    private fun goToAllComments() {
        binding.viewPager.currentItem = 1
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        isGestureEnabled = position != 1
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
}
