package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.dialog.ArticleGradeDialog
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.util.*

class ArticleActivity : BaseActivity() {

    private var title: String? = null
    private var url: String? = null
    private var newsId: String? = null
    private var isLiveInfo = false
    private val actionBarElevation = convertDpToPixel(4F)
    //private var lapinId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom("")
        enableBackBtn()

        url = intent.getStringExtra("url")
        title = intent.getStringExtra("title")

        if (url == null) {
            ToastUtil.showToast("null")
            finish()
            return
        }

        if (url!!.contains("live.ithome.com")) {
            if (intent.getStringExtra("live_info") == null) {
                val i = Intent(this, LiveActivity::class.java)
                i.putExtra("url", url)
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

            override fun onPageFinished(view: WebView?, url: String?) {
                supportActionBar?.elevation = 0F
                post_content.settings.loadsImagesAutomatically = true
                load_progress.visibility = View.GONE
                load_tip.visibility = View.GONE
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

        if (savedInstanceState == null) {
            load_tip.visibility = View.VISIBLE
            loadContent()
        } else {
            title = savedInstanceState.getString("title")
            newsId = savedInstanceState.getString("news_id")
            post_content.restoreState(savedInstanceState)
        }
    }

    override fun initView() {
        setContentView(R.layout.activity_article)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        post_content.saveState(outState)
        outState.putString("title", title)
        outState.putString("news_id", newsId)
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
            R.id.copy_link -> copyToClipboard("ITHomeNewsLink", url!!)
            R.id.open_in_browser -> openLinkInBrowser(this, url!!)
            android.R.id.home -> finish()
        }
        return true
    }

    private fun loadContent() {
        load_tip.setOnClickListener(null)
        load_tip.visibility = View.VISIBLE
        load_progress.visibility = View.VISIBLE
        load_text.visibility = View.GONE
        GlobalScope.launch(Dispatchers.Main + parentJob) {
            val loadImageAutomatically = shouldLoadImageAutomatically()
            val fullArticle = ArticleApiImpl.getFullArticle(url!!, loadImageAutomatically, isLiveInfo).await()
            if (fullArticle != null) {
                if (fullArticle.newsId.isBlank()) {
                    openLinkInBrowser(this@ArticleActivity, url!!)
                    finish()
                    return@launch
                }
                post_content.loadDataWithBaseURL(url, getCss() + fullArticle.content + getJs(),
                        "text/html; charset=utf-8",
                        "UTF-8", null)
                newsId = fullArticle.newsId
                title = fullArticle.title
            } else {
                ToastUtil.showToast(R.string.timeout_no_internet)
                load_text.visibility = View.VISIBLE
                load_tip.setOnClickListener { loadContent() }
                load_progress.visibility = View.GONE
            }
        }
    }

    @JavascriptInterface
    fun openInViewer(url: String) {
        val intent = Intent(this, ImageViewerActivity::class.java).apply {
            putExtra("url", url)
        }
        startActivity(intent)
    }

    private fun showComments() {
        val intent = Intent(this, CommentsActivity::class.java).apply {
            putExtra("id", newsId)
            putExtra("title", title)
            putExtra("url", url)
            // putExtra("lapinId", lapinId)
        }

        startActivity(intent)
    }

    private fun showGrade() {
        if (newsId != null) {
            val dialog = ArticleGradeDialog.newInstance(newsId!!)
            dialog.show(supportFragmentManager, "gradeDialog")
        } else {
            ToastUtil.showToast(R.string.see_grade_after_article_loaded)
        }
    }

    override fun swipeLeft(): Boolean {
        showComments()
        return true
    }
}
