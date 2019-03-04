package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.entity.FullArticle
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.dialog.ArticleGradeDialog
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.util.*

class ArticleActivity : BaseActivity() {

    private lateinit var title: String
    private lateinit var url: String
    private lateinit var newsId: String
    private lateinit var fullArticle: FullArticle
    private var isLiveInfo = false
    private var readProgress = 0F

    private val actionBarElevation = convertDpToPixel(4F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom("")
        enableBackBtn()

        url = intent.getStringExtra(KEY_URL)
        title = intent.getStringExtra(KEY_TITLE) ?: ""

        if (url.contains("live.ithome.com")) {
            if (intent.getStringExtra(KEY_LIVE_INFO) == null) {
                val i = Intent(this, LiveActivity::class.java)
                i.putExtra(KEY_URL, url)
                startActivity(i)
                finish()
                return
            }
            isLiveInfo = true
        }

        if (BaseApplication.isNightMode) {
            post_content.setBackgroundColor(getColor(R.color.background_dark))
        }
        post_content.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                openLink(this@ArticleActivity, request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                supportActionBar?.elevation = 0F
                post_content.settings.loadsImagesAutomatically = true
                load_progress.visibility = View.GONE
                load_tip.visibility = View.GONE

                if (readProgress != 0F) {
                    view.postDelayed({
                        val webViewSize = post_content.contentHeight - post_content.top
                        val position = webViewSize * readProgress
                        val scrollY = Math.round(post_content.top + position)
                        post_content.scrollTo(0, scrollY)
                    }, 300)
                }
            }
        }
        post_content.settings.loadsImagesAutomatically = false
        post_content.settings.javaScriptEnabled = true
        post_content.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        post_content.addJavascriptInterface(this, "JSInterface")
        post_content.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 0) {
                if (supportActionBar!!.elevation != actionBarElevation) {
                    supportActionBar?.elevation = actionBarElevation
                }
            } else {
                supportActionBar?.elevation = 0F
            }
            if (scrollY > 200) {
                if (getTitle().toString() != title) {
                    setTitleCustom(title)
                }
            } else if (getTitle().toString() != "") {
                setTitleCustom("")
            }
        }

        if (savedInstanceState?.getParcelable<FullArticle>(KEY_FULL_ARTICLE) == null) {
            load_tip.visibility = View.VISIBLE
            loadContent()
        } else {
            fullArticle = savedInstanceState.getParcelable(KEY_FULL_ARTICLE)!!
            readProgress = savedInstanceState.getFloat(KEY_READ_PROGRESS)
            loadArticleContent()
        }
    }

    override fun initView() {
        setContentView(R.layout.activity_article)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::fullArticle.isInitialized) {
            outState.putParcelable(KEY_FULL_ARTICLE, fullArticle)
            outState.putFloat(KEY_READ_PROGRESS, getScrollProgress(post_content))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.post_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                val share = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, title + "\n" + url)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(share, getString(R.string.share) + " " + title))
            }
            R.id.action_grade -> showGrade()
            R.id.action_comments -> showComments()
            R.id.copy_link -> copyToClipboard(CLIP_TAG_NEWS_LINK, url)
            R.id.open_in_browser -> openLinkInBrowser(this, url)
            android.R.id.home -> finish()
        }
        return true
    }

    private fun loadContent() {
        load_tip.setOnClickListener(null)
        load_tip.visibility = View.VISIBLE
        load_progress.visibility = View.VISIBLE
        load_text.visibility = View.GONE
        launch {
            val loadImageAutomatically = shouldLoadImageAutomatically()
            val fullArticle = withContext(Dispatchers.IO) {
                ArticleApiImpl.getFullArticle(url, loadImageAutomatically, isLiveInfo)
            }
            if (fullArticle != null) {
                if (fullArticle.newsId.isBlank()) {
                    openLinkInBrowser(this@ArticleActivity, url)
                    finish()
                    return@launch
                }
                this@ArticleActivity.fullArticle = fullArticle
                loadArticleContent()
            } else {
                ToastUtil.showToast(R.string.timeout_no_internet)
                load_text.visibility = View.VISIBLE
                load_tip.setOnClickListener { loadContent() }
                load_progress.visibility = View.GONE
            }
        }
    }

    private fun loadArticleContent() {
        post_content.loadDataWithBaseURL(
            url,
            getHead() + fullArticle.content + getFooter(),
            "text/html; charset=utf-8",
            "UTF-8", null
        )
        newsId = fullArticle.newsId
        title = fullArticle.title
    }

    @Suppress("Unused")
    @JavascriptInterface
    fun openInViewer(url: String) {
        val intent = Intent(this, ImageViewerActivity::class.java).apply {
            putExtra(KEY_URL, url)
        }
        startActivity(intent)
    }

    private fun showComments() {
        if (::newsId.isInitialized) {
            val intent = Intent(this, CommentsActivity::class.java).apply {
                putExtra(KEY_NEWS_ID, newsId)
                putExtra(KEY_TITLE, title)
                putExtra(KEY_URL, url)
                // putExtra("lapinId", lapinId)
            }
            startActivity(intent)
        }
    }

    private fun showGrade() {
        if (::newsId.isInitialized) {
            val dialog = ArticleGradeDialog.newInstance(newsId)
            dialog.show(supportFragmentManager, "gradeDialog")
        }
    }

    override fun swipeLeft(): Boolean {
        showComments()
        return true
    }
}
