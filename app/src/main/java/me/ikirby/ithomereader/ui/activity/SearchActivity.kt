package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.databinding.ActivitySearchBinding
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.ui.adapter.ArticleListAdapter
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.ui.widget.OnBottomReachedListener

class SearchActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchBinding

    private lateinit var articleList: ArrayList<Article>
    private lateinit var adapter: ArticleListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var keyword: String

    private var lastFirst: String? = null
    private var isLoading = false
    private var isRefresh = false
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivitySearchBinding.inflate(layoutInflater)
        // make sure binding is inflated before using in [initView()]

        super.onCreate(savedInstanceState)
        enableBackBtn()

        keyword = intent.getStringExtra(KEY_KEYWORD) ?: ""

        setTitleCustom(getString(R.string.keyword_s_results, keyword))

        layoutManager = LinearLayoutManager(this)

        binding.layout.listView.layoutManager = layoutManager
        binding.layout.swipeRefresh.isEnabled = false
        binding.layout.errorPlaceholder.setOnClickListener { loadList() }

        if (savedInstanceState != null) {
            articleList = savedInstanceState.getParcelableArrayList(KEY_ARTICLE_LIST) ?: ArrayList()
        }

        binding.layout.listView.setAllContentLoaded(false)

        if (savedInstanceState == null || articleList.isEmpty()) {
            articleList = ArrayList()
            adapter = ArticleListAdapter(articleList, null, this, false)
            binding.layout.listView.adapter = adapter
            loadList()
        } else {
            adapter = ArticleListAdapter(articleList, null, this, false)
            binding.layout.listView.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_STATE))
        }

        binding.layout.listView.setOnBottomReachedListener(bottomReachedListener)
    }

    override fun initView() {
        setContentView(binding.root)
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
            binding.layout.swipeRefresh.isRefreshing = true
            launch {
                val articles = withContext(Dispatchers.IO) { ArticleApiImpl.getSearchResults(keyword, page) }
                if (articles != null) {
                    if (articles.isNotEmpty()) {
                        if (articles[0].title != lastFirst) {
                            if (isRefresh) {
                                articleList.clear()
                            }
                            articleList.addAll(articles)
                            adapter.notifyDataSetChanged()
                            page++
                        }
                    } else {
                        binding.layout.listView.setAllContentLoaded(true)
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                UiUtil.switchVisibility(binding.layout.listView, binding.layout.errorPlaceholder, articleList.size)
                isLoading = false
                binding.layout.swipeRefresh.isRefreshing = false
            }
        }
    }

    private val bottomReachedListener = object : OnBottomReachedListener {
        override fun onBottomReached() {
            loadList()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return false
    }
}
