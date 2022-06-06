package me.ikirby.ithomereader.ui.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.databinding.PostListItemBinding
import me.ikirby.ithomereader.databinding.SlideRecyclerBinding
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.ui.activity.ArticleActivity
import me.ikirby.ithomereader.ui.activity.ImageViewerActivity
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.openLinkInBrowser

class ArticleListAdapter(
    private val list: ArrayList<Article>,
    private var focusSlideAdapter: SlideBannerAdapter?,
    private val context: Context,
    private var showThumb: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val bannerLayoutManager: LinearLayoutManager = LinearLayoutManager(context)

    init {
        bannerLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_SLIDE_BANNER) {
            val viewBinding = SlideRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SlideBannerViewHolder(viewBinding)
        } else {
            val viewBinding = PostListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val articleListViewHolder = ArticleListViewHolder(viewBinding)
            // todo: move click listeners to onBindViewHolder
            viewBinding.root.setOnClickListener {
                var position = articleListViewHolder.bindingAdapterPosition
                if (focusSlideAdapter != null) {
                    position--
                }
                val (title, _, url) = list[position]
                val intent = Intent(context, ArticleActivity::class.java).apply {
                    putExtra(KEY_URL, url)
                    putExtra(KEY_TITLE, title)
                }
                context.startActivity(intent)
            }
            viewBinding.root.setOnLongClickListener {
                var position = articleListViewHolder.bindingAdapterPosition
                if (focusSlideAdapter != null) {
                    position--
                }
                showPopupMenu(list[position])
                true
            }
            return articleListViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_POST) {
            // post
            (holder as ArticleListViewHolder).bind(
                list[ if (focusSlideAdapter != null) position - 1 else position ],
                showThumb
            )
        } else if (holder.itemViewType == TYPE_SLIDE_BANNER) {
            // banner
            (holder as SlideBannerViewHolder).layoutManager = bannerLayoutManager
        }
    }

    override fun getItemId(position: Int): Long = 0

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int =
        if (focusSlideAdapter != null && position == 0) TYPE_SLIDE_BANNER else TYPE_POST

    internal inner class ArticleListViewHolder(private val viewBinding: PostListItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(article: Article, showPostThumb: Boolean) {
            viewBinding.postThumb.clipToOutline = true

            viewBinding.postTitle.text = article.title
            viewBinding.postDate.text = article.date

            if (showPostThumb) {
                viewBinding.postTitle.maxLines = 3
                viewBinding.postThumb.visibility = View.VISIBLE
                Glide.with(context).load(article.thumb).into(viewBinding.postThumb)
            } else {
                viewBinding.postTitle.maxLines = 2
                viewBinding.postThumb.visibility = View.GONE
            }
        }
    }

    internal inner class SlideBannerViewHolder(private val viewBinding: SlideRecyclerBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        val handler = Handler(Looper.getMainLooper())
        private val autoScroll = object : Runnable {
            override fun run() {
                if (bannerLayoutManager.findFirstVisibleItemPosition() == bannerLayoutManager.itemCount - 1) {
                    viewBinding.recyclerView.smoothScrollToPosition(0)
                } else {
                    viewBinding.recyclerView.smoothScrollToPosition(bannerLayoutManager.findFirstVisibleItemPosition() + 1)
                }
                handler.postDelayed(this, SLIDE_SCROLL_INTERVAL)
            }
        }

        init {
            PagerSnapHelper().attachToRecyclerView(viewBinding.recyclerView)
            viewBinding.recyclerView.layoutManager = bannerLayoutManager
            viewBinding.recyclerView.adapter = focusSlideAdapter
            viewBinding.recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                }

                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    if (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL) {
                        handler.postDelayed(autoScroll, SLIDE_SCROLL_INTERVAL)
                    } else {
                        handler.removeCallbacks(autoScroll)
                    }
                    return false
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                }
            })
            handler.postDelayed(autoScroll, SLIDE_SCROLL_INTERVAL)
        }

        internal var layoutManager: RecyclerView.LayoutManager?
            get() = viewBinding.recyclerView.layoutManager
            set(value) { viewBinding.recyclerView.layoutManager = value }
    }

    fun setShowThumb(showThumb: Boolean) {
        this.showThumb = showThumb
    }

    fun setFocusSlideAdapter(focusSlideAdapter: SlideBannerAdapter?) {
        this.focusSlideAdapter = focusSlideAdapter
    }

    private fun showPopupMenu(post: Article) {
        UiUtil.showBottomSheetMenu(
            context,
            object : BottomSheetMenu.BottomSheetMenuListener {
                override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                    inflater.inflate(R.menu.main_context, menu)
                    if (post.thumb == null) {
                        menu.removeItem(R.id.view_thumb)
                    }
                }

                override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                    when (item.itemId) {
                        R.id.share -> {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, post.title + "\n" + post.url)
                                type = "text/plain"
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    share,
                                    context.getString(R.string.share) + " " + post.title
                                )
                            )
                        }
                        R.id.copy_link -> copyToClipboard(CLIP_TAG_NEWS_LINK, post.url)
                        R.id.view_thumb -> {
                            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                                putExtra(KEY_URL, post.thumb)
                            }
                            context.startActivity(intent)
                        }
                        R.id.open_in_browser -> openLinkInBrowser(context, post.url)
                    }
                }
            }
        )
    }

    companion object {
        private const val TYPE_SLIDE_BANNER = 0
        private const val TYPE_POST = 1
    }
}
