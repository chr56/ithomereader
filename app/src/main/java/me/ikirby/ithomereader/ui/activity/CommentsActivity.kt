package me.ikirby.ithomereader.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.databinding.ActivityViewpagerBinding
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.databinding.viewmodel.CommentsActivityViewModel
import me.ikirby.ithomereader.ui.fragment.AllCommentFragment
import me.ikirby.ithomereader.ui.fragment.HotCommentFragment
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.encryptNewsId

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
            viewModel.newsId.value = newsId
            viewModel.newsIdEncrypted.value = encryptNewsId(newsId)
            Logger.d("CommentsActivity", "encryptedNewsId = ${viewModel.newsIdEncrypted.value}")
            viewModel.newsTitle.value = intent.getStringExtra(KEY_TITLE)
            viewModel.newsUrl.value = intent.getStringExtra(KEY_URL)

            viewModel.loadComment(hot = true, all = true, refresh = true)
        } else {
            finish()
        }
    }

    override fun initView() {
        binding = ActivityViewpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.navigationBarColor = Color.TRANSPARENT
            binding.container.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }

        val fragments = listOf(
            HotCommentFragment(),
            AllCommentFragment()
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
            R.id.action_post_comment -> {
                val intent = Intent(this, CommentPostActivity::class.java).apply {
                    putExtra(KEY_NEWS_ID, viewModel.newsId.value!!)
                    putExtra(KEY_TITLE, viewModel.newsTitle.value!!)
                }
                postComment.launch(intent)
            }
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
