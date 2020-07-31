package me.ikirby.ithomereader.ui.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.post_list_item.view.*
import kotlinx.android.synthetic.main.slide_recycler.view.*
import me.ikirby.ithomereader.*
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
        if (viewType == 0) {
            val view = LayoutInflater.from(context).inflate(R.layout.slide_recycler, parent, false)
            return SlideBannerViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.post_list_item, parent, false)
            val articleListViewHolder = ArticleListViewHolder(view)
            view.setOnClickListener {
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
            view.setOnLongClickListener {
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
        var pos = position
        if (focusSlideAdapter == null || pos > 0) {
            val articleListViewHolder = holder as ArticleListViewHolder

            if (focusSlideAdapter != null) {
                pos -= 1
            }
            val (title, date, _, thumb) = list[pos]

            articleListViewHolder.titleText.text = title

            if (date!!.contains("今")) {
                articleListViewHolder.dateTextToday.text = date
                articleListViewHolder.dateText.text = ""
            } else {
                articleListViewHolder.dateText.text = date
                articleListViewHolder.dateTextToday.text = ""
            }

            if (showThumb) {
                articleListViewHolder.titleText.maxLines = 3
                articleListViewHolder.thumbImage.visibility = View.VISIBLE
                Glide.with(context).load(thumb).into(articleListViewHolder.thumbImage)
            } else {
                articleListViewHolder.titleText.maxLines = 2
                articleListViewHolder.thumbImage.visibility = View.GONE
            }
        } else if (pos == 0) {
            (holder as SlideBannerViewHolder).recyclerView.layoutManager = bannerLayoutManager
        }
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (focusSlideAdapter != null && position == 0) {
            0
        } else {
            1
        }
    }

    internal inner class ArticleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbImage: ImageView = itemView.post_thumb
        val titleText: TextView = itemView.post_title
        val dateText: TextView = itemView.post_date
        val dateTextToday: TextView = itemView.post_date_today
        val postInfoWrapper: View = itemView.post_info_wrapper

        init {
            thumbImage.clipToOutline = true
        }
    }

    internal inner class SlideBannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.recycler_view
        val handler = Handler(Looper.getMainLooper())
        private val autoScroll = object : Runnable {
            override fun run() {
                if (bannerLayoutManager.findFirstVisibleItemPosition() == bannerLayoutManager.itemCount - 1) {
                    recyclerView.smoothScrollToPosition(0)
                } else {
                    recyclerView.smoothScrollToPosition(bannerLayoutManager.findFirstVisibleItemPosition() + 1)
                }
                handler.postDelayed(this, SLIDE_SCROLL_INTERVAL)
            }
        }

        init {
            PagerSnapHelper().attachToRecyclerView(recyclerView)
            recyclerView.layoutManager = bannerLayoutManager
            recyclerView.adapter = focusSlideAdapter
            recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
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
    }

    fun setShowThumb(showThumb: Boolean) {
        this.showThumb = showThumb
    }

    fun setFocusSlideAdapter(focusSlideAdapter: SlideBannerAdapter?) {
        this.focusSlideAdapter = focusSlideAdapter
    }

    private fun showPopupMenu(post: Article) {
        UiUtil.showBottomSheetMenu(context, object : BottomSheetMenu.BottomSheetMenuListener {
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
        })
    }
}
