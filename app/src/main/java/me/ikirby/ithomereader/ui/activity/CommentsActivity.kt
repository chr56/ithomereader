package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_viewpager.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.COMMENT_POSTED_REQUEST_CODE
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.CommentApiImpl
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.dialog.LoginDialog
import me.ikirby.ithomereader.ui.fragment.CommentListFragment
import me.ikirby.ithomereader.ui.util.ToastUtil

class CommentsActivity : BaseActivity(), ViewPager.OnPageChangeListener {
    private lateinit var id: String
    private lateinit var title: String
    private lateinit var url: String
    //    private lateinit var lapinId: String
    private lateinit var commentHash: String

    private lateinit var fragments: List<CommentListFragment>
    private var cookie: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.comments))
        enableBackBtn()

        id = intent.getStringExtra("id")
        title = intent.getStringExtra("title")
        url = intent.getStringExtra("url")
//        lapinId = intent.getStringExtra("lapinId")

        cookie = preferences.getString("user_hash", null)

        if (savedInstanceState != null) {
            commentHash = savedInstanceState.getString("comment_hash", "")
            loadPages(id, commentHash, cookie, url, null)
        } else {
            loadCommentHash()
        }
    }

    override fun initView() {
        setContentView(R.layout.activity_viewpager)
        tabs.setupWithViewPager(viewPager)
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
                    putExtra("id", id)
                    putExtra("title", title)
                }
                startActivityForResult(intent, COMMENT_POSTED_REQUEST_CODE)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == COMMENT_POSTED_REQUEST_CODE) {
            goToAllComments()
            val refreshBtn = findViewById<View>(R.id.action_refresh)
            refreshBtn?.callOnClick()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::fragments.isInitialized) {
            loadCookie()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("comment_hash", commentHash)
    }

    private fun loadCommentHash() {
        load_tip.setOnClickListener(null)
        tabs.visibility = View.GONE
        viewPager.visibility = View.INVISIBLE
        load_tip.visibility = View.VISIBLE
        load_progress.visibility = View.VISIBLE
        load_text.visibility = View.GONE
        launch {
            val hash = withContext(Dispatchers.IO) { CommentApiImpl.getCommentHash(id) }
            if (hash != null) {
                commentHash = hash
                loadPages(id, hash, cookie, url, null)
            } else {
                ToastUtil.showToast(R.string.timeout_no_internet)
                load_text.visibility = View.VISIBLE
                load_tip.setOnClickListener { loadCommentHash() }
            }
            load_progress.visibility = View.GONE
        }
    }

    private fun loadPages(id: String, hash: String, cookie: String?, url: String, lapinId: String?) {
        supportActionBar?.elevation = 0F
        load_tip.visibility = View.GONE
        load_progress.visibility = View.GONE
        tabs.visibility = View.VISIBLE
        viewPager.visibility = View.VISIBLE

        fragments = listOf(
                CommentListFragment.newInstance(id, hash, cookie, url, lapinId, true),
                CommentListFragment.newInstance(id, hash, cookie, url, lapinId)
        )

        val adapter = object : FragmentPagerAdapter(supportFragmentManager) {
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

        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(this)
    }

    private fun goToAllComments() {
        viewPager.currentItem = 1
    }

    fun showLoginDialog() {
        val dialog = LoginDialog.newInstance(cookie)
        dialog.show(supportFragmentManager, "loginDialog")
    }

    fun loadCookie() {
        cookie = BaseApplication.preferences.getString("user_hash", null)
        for (fragment in fragments) {
            if (fragment.view != null) {
                fragment.setCookie(cookie)
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        isGestureEnabled = position != 1
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
}
