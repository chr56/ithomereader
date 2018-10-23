package me.ikirby.ithomereader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_layout.view.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.api.impl.TrendingApiImpl
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.ui.adapter.ArticleListAdapter
import me.ikirby.ithomereader.ui.adapter.SlideBannerAdapter
import me.ikirby.ithomereader.ui.base.BaseFragment
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.ui.widget.OnBottomReachedListener
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.list_layout, container, false)
        layoutManager = LinearLayoutManager(activity)
        view.list_view.layoutManager = layoutManager

        view.swipe_refresh.setColorSchemeResources(UiUtil.getAccentColorRes())
        view.swipe_refresh.setProgressBackgroundColorSchemeResource(UiUtil.getWindowBackgroundColorRes())
        view.swipe_refresh.setOnRefreshListener { reloadList() }

        view.error_placeholder.setOnClickListener { reloadList() }

        if (savedInstanceState != null) {
            articleList = savedInstanceState.getParcelableArrayList("articleList") ?: ArrayList()
            focuses = savedInstanceState.getParcelableArrayList("focuses") ?: ArrayList()
        }

        if (savedInstanceState == null || showBanner || articleList.isEmpty()) {
            showThumb = shouldShowThumb()
            showBanner = BaseApplication.preferences.getBoolean("show_banner", true)
            articleList = ArrayList()
            focuses = ArrayList()
            if (showBanner) {
                focusSlideAdapter = SlideBannerAdapter(focuses, context!!)
            }
            adapter = ArticleListAdapter(articleList, focusSlideAdapter, context!!, showThumb)
            view.list_view.adapter = adapter
        } else {
            showThumb = savedInstanceState.getBoolean("show_thumb", true)
            showBanner = savedInstanceState.getBoolean("show_banner", true)
            page = savedInstanceState.getInt("page")
            if (showBanner) {
                focusSlideAdapter = SlideBannerAdapter(focuses, context!!)
            }
            adapter = ArticleListAdapter(articleList, focusSlideAdapter, context!!, showThumb)
            view.list_view.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("list_state"))
            if (showBanner) {
                adapter.bannerLayoutManager.onRestoreInstanceState(
                        savedInstanceState.getParcelable("focus_state"))
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
        outState.putInt("page", page)
        outState.putParcelableArrayList("articleList", articleList)
        outState.putParcelable("list_state", layoutManager.onSaveInstanceState())
        outState.putBoolean("show_thumb", showThumb)
        outState.putBoolean("show_banner", showBanner)
        if (showBanner) {
            outState.putParcelableArrayList("focuses", focuses)
            outState.putParcelable("focus_state", adapter.bannerLayoutManager.onSaveInstanceState())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
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
            showBanner = BaseApplication.preferences.getBoolean("show_banner", true)
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
            GlobalScope.launch(Dispatchers.Main + parentJob) {
                val filterLapin = BaseApplication.preferences.getBoolean("filter_lapin", false)
                val keywords = BaseApplication.preferences.getString("custom_filter", "")!!
                        .split(", ").dropLastWhile { it.isEmpty() }.toTypedArray()
                val customFilter = keywords.isNotEmpty()
                val articles = if (isRefresh) {
                    ArticleApiImpl.getArticleList(page, filterLapin, customFilter, keywords, null).await()
                } else {
                    ArticleApiImpl.getArticleList(page, filterLapin, customFilter, keywords, articleList).await()
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
        GlobalScope.launch(Dispatchers.Main + parentJob) {
            val articles = TrendingApiImpl.getFocusBannerArticles().await()
            if (articles != null && articles.isNotEmpty()) {
                focuses.clear()
                focuses.addAll(articles)
                focusSlideAdapter.notifyDataSetChanged()
            }
        }
    }
}
