package me.ikirby.ithomereader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.KEY_ARTICLE_LIST
import me.ikirby.ithomereader.KEY_BANNER_ITEMS
import me.ikirby.ithomereader.KEY_BANNER_STATE
import me.ikirby.ithomereader.KEY_LIST_STATE
import me.ikirby.ithomereader.KEY_PAGE
import me.ikirby.ithomereader.KEY_SHOW_BANNER
import me.ikirby.ithomereader.KEY_SHOW_THUMB
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.SETTINGS_KEY_CUSTOM_FILTER
import me.ikirby.ithomereader.SETTINGS_KEY_FILTER_ADS
import me.ikirby.ithomereader.SETTINGS_KEY_SHOW_BANNER
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.api.impl.TrendingApiImpl
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.ui.adapter.ArticleListAdapter
import me.ikirby.ithomereader.ui.adapter.SlideBannerAdapter
import me.ikirby.ithomereader.ui.base.BaseFragment
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.ui.widget.OnBottomReachedListener
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.shouldShowThumb


class ArticleListFragment : BaseFragment() {

    private lateinit var articleList: ArrayList<Article>
    private lateinit var focuses: ArrayList<Article>
    private lateinit var focusSlideAdapter: SlideBannerAdapter
    private lateinit var adapter: ArticleListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var page = 0
    private var showThumb: Boolean = false
    private var showBanner: Boolean = false
    private var isLoading = false
    private var isRefresh = false

    private val bottomReachedListener = object : OnBottomReachedListener {
        override fun onBottomReached() {
            loadList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.list_layout, container, false)
        layoutManager = LinearLayoutManager(activity)
        view.list_view.layoutManager = layoutManager

        view.swipe_refresh.setColorSchemeResources(UiUtil.getAccentColorRes())
        view.swipe_refresh.setProgressBackgroundColorSchemeResource(UiUtil.getWindowBackgroundColorRes())
        view.swipe_refresh.setOnRefreshListener { reloadList() }

        view.error_placeholder.setOnClickListener { reloadList() }

        if (savedInstanceState != null) {
            articleList = savedInstanceState.getParcelableArrayList(KEY_ARTICLE_LIST) ?: ArrayList()
            focuses = savedInstanceState.getParcelableArrayList(KEY_BANNER_ITEMS) ?: ArrayList()
        }

        if (savedInstanceState == null || showBanner || articleList.isEmpty()) {
            showThumb = shouldShowThumb()
            showBanner = BaseApplication.preferences.getBoolean(SETTINGS_KEY_SHOW_BANNER, true)
            articleList = ArrayList()
            focuses = ArrayList()
            if (showBanner) {
                focusSlideAdapter = SlideBannerAdapter(focuses, context!!)
                adapter = ArticleListAdapter(articleList, focusSlideAdapter, context!!, showThumb)
            } else {
                adapter = ArticleListAdapter(articleList, null, context!!, showThumb)
            }
            view.list_view.adapter = adapter
        } else {
            showThumb = savedInstanceState.getBoolean(KEY_SHOW_THUMB, true)
            showBanner = savedInstanceState.getBoolean(KEY_SHOW_BANNER, true)
            page = savedInstanceState.getInt(KEY_PAGE)
            if (showBanner) {
                focusSlideAdapter = SlideBannerAdapter(focuses, context!!)
                adapter = ArticleListAdapter(articleList, focusSlideAdapter, context!!, showThumb)
            } else {
                adapter = ArticleListAdapter(articleList, null, context!!, showThumb)
            }
            view.list_view.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_STATE))
            if (showBanner) {
                adapter.bannerLayoutManager.onRestoreInstanceState(
                    savedInstanceState.getParcelable(KEY_BANNER_STATE)
                )
            }
        }

        view.list_view.setOnBottomReachedListener(bottomReachedListener)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) reloadList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PAGE, page)
        outState.putParcelableArrayList(KEY_ARTICLE_LIST, articleList)
        outState.putParcelable(KEY_LIST_STATE, layoutManager.onSaveInstanceState())
        outState.putBoolean(KEY_SHOW_THUMB, showThumb)
        outState.putBoolean(KEY_SHOW_BANNER, showBanner)
        if (showBanner) {
            outState.putParcelableArrayList(KEY_BANNER_ITEMS, focuses)
            outState.putParcelable(KEY_BANNER_STATE, adapter.bannerLayoutManager.onSaveInstanceState())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                layoutManager.scrollToPosition(0)
                reloadList()
            }
        }
        return true
    }

    private fun reloadList() {
        if (!isLoading) {
            view!!.list_view.setAllContentLoaded(false)
            isRefresh = true
            page = 0
            loadList()
            showBanner = BaseApplication.preferences.getBoolean(SETTINGS_KEY_SHOW_BANNER, true)
            if (showBanner) {
                if (!::focusSlideAdapter.isInitialized) {
                    focuses = ArrayList()
                    focusSlideAdapter = SlideBannerAdapter(focuses, context!!)
                    adapter.setFocusSlideAdapter(focusSlideAdapter)
                }
                loadBanner()
            } else {
                adapter.setFocusSlideAdapter(null)
            }
        }
    }

    private fun loadList() {
        if (!isLoading) {
            isLoading = true
            view!!.swipe_refresh.isRefreshing = true
            page++
            launch {
                val filterLapin = BaseApplication.preferences.getBoolean(SETTINGS_KEY_FILTER_ADS, false)
                val keywords = BaseApplication.preferences.getString(SETTINGS_KEY_CUSTOM_FILTER, "")!!
                    .split(",").filter { it.isNotBlank() }
                Logger.d("keywords", keywords.toString())
                val articles = withContext(Dispatchers.IO) {
                    if (isRefresh) {
                        ArticleApiImpl.getArticleList(page, filterLapin, keywords, null)
                    } else {
                        ArticleApiImpl.getArticleList(page, filterLapin, keywords, articleList)
                    }
                }

                if (articles != null) {
                    if (articles.isNotEmpty()) {
                        if (isRefresh) {
                            showThumb = shouldShowThumb()
                            adapter.setShowThumb(showThumb)
                            articleList.clear()
                        }
                        articleList.addAll(articles)
                        adapter.notifyDataSetChanged()
                    } else {
                        page--
                        view!!.list_view.setAllContentLoaded(true)
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    page--
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }

                UiUtil.switchVisibility(view!!.list_view, view!!.error_placeholder, articleList.size)
                isLoading = false
                isRefresh = false
                view!!.swipe_refresh.isRefreshing = false
            }
        }
    }

    private fun loadBanner() {
        launch {
            val articles = withContext(Dispatchers.IO) { TrendingApiImpl.getFocusBannerArticles() }
            if (articles != null && articles.isNotEmpty()) {
                focuses.clear()
                focuses.addAll(articles)
                focusSlideAdapter.notifyDataSetChanged()
            }
        }
    }
}
