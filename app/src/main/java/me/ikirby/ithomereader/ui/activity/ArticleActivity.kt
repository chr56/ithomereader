package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.databinding.ActivityArticleBinding
import me.ikirby.ithomereader.entity.FullArticle
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.dialog.ArticleGradeDialog
import me.ikirby.ithomereader.ui.util.CompatibilityUtil.parcelable
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.util.*
import kotlin.math.roundToInt

class ArticleActivity : BaseActivity() {

    private lateinit var binding: ActivityArticleBinding

    private lateinit var title: String
    private lateinit var url: String
    private lateinit var fullArticle: FullArticle
    private var isLiveInfo = false
    private var readProgress = 0F

    private val actionBarElevation = convertDpToPixel(4F)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityArticleBinding.inflate(layoutInflater)
        // make sure binding is inflated before using in [initView()]
        super.onCreate(savedInstanceState)
        setTitleCustom("")
        enableBackBtn()

        url = intent.getStringExtra(KEY_URL) ?: ""
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

        if (isNightMode()) {
            binding.postContent.setBackgroundColor(getColor(R.color.background_dark))
        }

        binding.postContent.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                openLink(this@ArticleActivity, request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                supportActionBar?.elevation = 0F
                binding.loadProgress.visibility = View.GONE
                binding.loadTip.visibility = View.GONE

                if (readProgress != 0F) {
                    view.postDelayed({
                        val webViewSize = binding.postContent.contentHeight - binding.postContent.top
                        val position = webViewSize * readProgress
                        val scrollY = (binding.postContent.top + position).roundToInt()
                        binding.postContent.scrollTo(0, scrollY)
                    }, 300)
                }
            }
        }
        binding.postContent.settings.javaScriptEnabled = true
        binding.postContent.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        binding.postContent.addJavascriptInterface(this, "JSInterface")
        binding.postContent.setOnScrollChangeListener { _, _, scrollY, _, _ ->
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

        if (savedInstanceState?.parcelable<FullArticle>(KEY_FULL_ARTICLE) == null) {
            binding.loadTip.visibility = View.VISIBLE
            loadContent()
        } else {
            fullArticle = savedInstanceState.parcelable(KEY_FULL_ARTICLE)!!
            readProgress = savedInstanceState.getFloat(KEY_READ_PROGRESS)
            loadArticleContent()
        }
    }

    override fun initView() {
        setContentView(binding.root)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::fullArticle.isInitialized) {
            outState.putParcelable(KEY_FULL_ARTICLE, fullArticle)
            outState.putFloat(KEY_READ_PROGRESS, getScrollProgress(binding.postContent))
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
        binding.loadTip.setOnClickListener(null)
        binding.loadTip.visibility = View.VISIBLE
        binding.loadProgress.visibility = View.VISIBLE
        binding.loadText.visibility = View.GONE
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
                binding.loadText.visibility = View.VISIBLE
                binding.loadTip.setOnClickListener { loadContent() }
                binding.loadProgress.visibility = View.GONE
            }
        }
    }

    private fun loadArticleContent() {
        binding.postContent.loadDataWithBaseURL(
            url,
            getHead(isNightMode()) + fullArticle.content + getFooter(),
            "text/html; charset=utf-8",
            "UTF-8",
            null
        )
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
        if (::fullArticle.isInitialized) {
            val intent = Intent(this, CommentsActivity::class.java).apply {
                putExtra(KEY_NEWS_ID, fullArticle.newsId)
                putExtra(KEY_TITLE, title)
                putExtra(KEY_URL, url)
            }
            startActivity(intent)
        }
    }

    private fun showGrade() {
        if (::fullArticle.isInitialized) {
            val dialog = ArticleGradeDialog.newInstance(fullArticle.newsId)
            dialog.show(supportFragmentManager, "gradeDialog")
        }
    }

    override fun swipeLeft(): Boolean {
        showComments()
        return true
    }
}
