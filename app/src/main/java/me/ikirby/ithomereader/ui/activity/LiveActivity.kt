package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.KEY_LIST_STATE
import me.ikirby.ithomereader.KEY_LIVE_INFO
import me.ikirby.ithomereader.KEY_LIVE_MESSAGES
import me.ikirby.ithomereader.KEY_NEWS_ID
import me.ikirby.ithomereader.KEY_TITLE
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.LiveApiImpl
import me.ikirby.ithomereader.entity.LiveMsg
import me.ikirby.ithomereader.ui.adapter.LivePostListAdapter
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.getMatchInt

class LiveActivity : BaseActivity() {

    private lateinit var liveMessages: ArrayList<LiveMsg>
    private lateinit var adapter: LivePostListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var url: String
    private lateinit var newsId: String
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.live))
        enableBackBtn()

        url = intent.getStringExtra(KEY_URL) ?: ""
        newsId = "" + getMatchInt(url)

        layoutManager = LinearLayoutManager(this)
        list_view.layoutManager = layoutManager

        swipe_refresh.setColorSchemeResources(UiUtil.getAccentColorRes())
        swipe_refresh.setProgressBackgroundColorSchemeResource(UiUtil.getWindowBackgroundColorRes())
        swipe_refresh.setOnRefreshListener { loadList() }

        error_placeholder.setOnClickListener { loadList() }

        if (savedInstanceState != null) {
            liveMessages = savedInstanceState.getParcelableArrayList(KEY_LIVE_MESSAGES) ?: ArrayList()
        }

        if (savedInstanceState == null || liveMessages.isEmpty()) {
            liveMessages = ArrayList()
            adapter = LivePostListAdapter(liveMessages, layoutInflater)
            list_view.adapter = adapter
            loadList()
        } else {
            adapter = LivePostListAdapter(liveMessages, layoutInflater)
            list_view.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_STATE))
        }
    }

    override fun initView() {
        setContentView(R.layout.list_layout)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_LIVE_MESSAGES, liveMessages)
        outState.putParcelable(KEY_LIST_STATE, layoutManager.onSaveInstanceState())
    }

    private fun loadList() {
        if (!isLoading) {
            isLoading = true
            swipe_refresh.isRefreshing = true
            launch {
                val liveMsgs = withContext(Dispatchers.IO) { LiveApiImpl.getLiveMessages(newsId) }
                if (liveMsgs != null) {
                    if (liveMsgs.isNotEmpty()) {
                        liveMessages.clear()
                        liveMessages.addAll(liveMsgs)
                        adapter.notifyDataSetChanged()
                    } else {
                        list_view.setAllContentLoaded(true)
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                UiUtil.switchVisibility(list_view, error_placeholder, liveMessages.size)
                isLoading = false
                swipe_refresh.isRefreshing = false
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
        if (::newsId.isInitialized) {
            val intent = Intent(this, CommentsActivity::class.java).apply {
                putExtra(KEY_NEWS_ID, newsId)
                putExtra(KEY_TITLE, getString(R.string.live))
                putExtra(KEY_URL, url)
            }
            startActivity(intent)
        }
    }
}
