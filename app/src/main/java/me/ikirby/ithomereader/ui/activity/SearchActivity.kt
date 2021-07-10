package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.ui.adapter.ArticleListAdapter
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import java.util.*

class SearchActivity : BaseActivity() {

    private lateinit var articleList: ArrayList<Article>
    private lateinit var adapter: ArticleListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var keyword: String
    private var lastFirst: String? = null
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableBackBtn()

        keyword = intent.getStringExtra(KEY_KEYWORD) ?: ""

        setTitleCustom(getString(R.string.keyword_s_results, keyword))

        layoutManager = LinearLayoutManager(this)
        list_view.layoutManager = layoutManager

        swipe_refresh.isEnabled = false

        error_placeholder.setOnClickListener { loadList() }

        if (savedInstanceState != null) {
            articleList = savedInstanceState.getParcelableArrayList(KEY_ARTICLE_LIST) ?: ArrayList()
        }

        list_view.setAllContentLoaded(true)

        if (savedInstanceState == null || articleList.isEmpty()) {
            articleList = ArrayList()
            adapter = ArticleListAdapter(articleList, null, this, false)
            list_view.adapter = adapter
            loadList()
        } else {
            adapter = ArticleListAdapter(articleList, null, this, false)
            list_view.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_STATE))
        }
    }

    override fun initView() {
        setContentView(R.layout.activity_search)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_ARTICLE_LIST, articleList)
        outState.putParcelable(KEY_LIST_STATE, layoutManager.onSaveInstanceState())
        outState.putString(KEY_LAST_FIRST, lastFirst)
    }

    private fun loadList() {
        if (!isLoading) {
            isLoading = true
            swipe_refresh.isRefreshing = true
            launch {
                val articles = withContext(Dispatchers.IO) { ArticleApiImpl.getSearchResults(keyword) }
                if (articles != null) {
                    if (articles.isNotEmpty()) {
                        if (articles[0].title != lastFirst) {
                            lastFirst = articles[0].title
                            articleList.addAll(articles)
                            adapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                UiUtil.switchVisibility(list_view, error_placeholder, articleList.size)
                isLoading = false
                swipe_refresh.isRefreshing = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return false
    }
}
