package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.api.impl.LiveApiImpl
import me.ikirby.ithomereader.databinding.ActivitySimpleListBinding
import me.ikirby.ithomereader.entity.LiveMsg
import me.ikirby.ithomereader.ui.adapter.LivePostListAdapter
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.util.CompatibilityUtil.parcelable
import me.ikirby.ithomereader.ui.util.CompatibilityUtil.parcelableArrayList
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.getMatchInt

class LiveActivity : BaseActivity() {

    private lateinit var binding: ActivitySimpleListBinding

    private lateinit var liveMessages: ArrayList<LiveMsg>
    private lateinit var adapter: LivePostListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var url: String
    private lateinit var newsId: String
    private lateinit var newsIdHash: String
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySimpleListBinding.inflate(layoutInflater)
        // make sure binding is inflated before using in [initView()]
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.live))
        enableBackBtn()

        url = intent.getStringExtra(KEY_URL) ?: ""
        newsId = "" + getMatchInt(url)

        layoutManager = LinearLayoutManager(this)
        binding.layout.listView.layoutManager = layoutManager

        binding.layout.swipeRefresh.setOnRefreshListener { loadList() }
        binding.layout.errorPlaceholder.setOnClickListener { loadList() }

        if (savedInstanceState != null) {
            liveMessages = savedInstanceState.parcelableArrayList(KEY_LIVE_MESSAGES) ?: ArrayList()
        }

        if (savedInstanceState == null || liveMessages.isEmpty()) {
            liveMessages = ArrayList()
            adapter = LivePostListAdapter(liveMessages, layoutInflater)
            binding.layout.listView.adapter = adapter
            loadList()
        } else {
            adapter = LivePostListAdapter(liveMessages, layoutInflater)
            binding.layout.listView.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.parcelable(KEY_LIST_STATE))
        }
    }

    override fun initView() {
        setContentView(binding.root)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_LIVE_MESSAGES, liveMessages)
        outState.putParcelable(KEY_LIST_STATE, layoutManager.onSaveInstanceState())
    }

    private fun loadList() {
        if (!isLoading) {
            isLoading = true
            binding.layout.swipeRefresh.isRefreshing = true
            launch {
                val liveMsgs = withContext(Dispatchers.IO) { LiveApiImpl.getLiveMessages(newsId) }
                if (liveMsgs != null) {
                    if (liveMsgs.isNotEmpty()) {
                        liveMessages.clear()
                        liveMessages.addAll(liveMsgs)
                        adapter.notifyDataSetChanged()
                    } else {
                        binding.layout.listView.setAllContentLoaded(true)
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                val commentNewsIdHash = withContext(Dispatchers.IO) { LiveApiImpl.getNewsIdHash(newsId) }
                if (commentNewsIdHash != null) {
                    newsIdHash = commentNewsIdHash
                }
                UiUtil.switchVisibility(binding.layout.listView, binding.layout.errorPlaceholder, liveMessages.size)
                isLoading = false
                binding.layout.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.live_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_live_info -> {
                val intent = Intent(this, ArticleActivity::class.java)
                intent.putExtra(KEY_URL, url)
                intent.putExtra(KEY_LIVE_INFO, "")
                startActivity(intent)
            }
            R.id.action_comments -> {
                showComments()
            }
        }
        return true
    }

    override fun swipeLeft(): Boolean {
        showComments()
        return true
    }

    private fun showComments() {
        if (::newsId.isInitialized && ::newsIdHash.isInitialized) {
            val intent = Intent(this, CommentsActivity::class.java).apply {
                putExtra(KEY_NEWS_ID, newsId)
                putExtra(KEY_NEWS_ID_HASH, newsIdHash)
                putExtra(KEY_TITLE, getString(R.string.live))
                putExtra(KEY_URL, url)
            }
            startActivity(intent)
        } else {
            ToastUtil.showToast(R.string.comment_basic_load_failed)
        }
    }
}
