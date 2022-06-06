package me.ikirby.ithomereader.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.ikirby.ithomereader.CLIP_TAG_NEWS_LINK
import me.ikirby.ithomereader.KEY_TITLE
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.TrendingFocusItemBinding
import me.ikirby.ithomereader.databinding.TrendingHeaderBinding
import me.ikirby.ithomereader.databinding.TrendingItemBinding
import me.ikirby.ithomereader.entity.Trending
import me.ikirby.ithomereader.ui.activity.ArticleActivity
import me.ikirby.ithomereader.ui.activity.ImageViewerActivity
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.openLinkInBrowser
import java.util.*

class TrendingListAdapter(
    private val list: ArrayList<Trending>,
    private val context: Context,
    private var showThumb: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FOCUS -> {
                val binding = TrendingFocusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TrendingFocusViewHolder(binding).also { holder ->
                    // todo: move click listeners to onBindViewHolder
                    binding.root.setOnClickListener {
                        val (_, title, url) = list[holder.bindingAdapterPosition]
                        val intent = Intent(context, ArticleActivity::class.java).apply {
                            putExtra(KEY_URL, url)
                            putExtra(KEY_TITLE, title)
                        }
                        context.startActivity(intent)
                    }
                    binding.root.setOnLongClickListener {
                        showPopupMenu(list[holder.bindingAdapterPosition])
                        true
                    }
                }
            }
            TYPE_ITEM -> {
                val binding = TrendingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TrendingListViewHolder(binding).also { holder ->
                    // todo: move click listeners to onBindViewHolder
                    binding.root.setOnClickListener {
                        val (_, title, url) = list[holder.bindingAdapterPosition]
                        val intent = Intent(context, ArticleActivity::class.java).apply {
                            putExtra(KEY_URL, url)
                            putExtra(KEY_TITLE, title)
                        }
                        context.startActivity(intent)
                    }
                    binding.root.setOnLongClickListener {
                        showPopupMenu(list[holder.bindingAdapterPosition])
                        true
                    }
                }
            }
            else -> {
                TrendingHeaderViewHolder(TrendingHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (rank, title, _, _, thumb) = list[position]

        when (holder) {
            is TrendingHeaderViewHolder -> {
                holder.bind(title)
            }
            is TrendingListViewHolder -> {
                holder.bind(rank, title)
            }
            is TrendingFocusViewHolder -> {
                holder.bind(title, showThumb, thumb)
            }
        }
    }

    override fun getItemId(position: Int): Long = 0

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return when {
            list[position].rank != null -> TYPE_ITEM
            list[position].url != null -> TYPE_FOCUS
            else -> TYPE_HEADER
        }
    }

    fun setShowThumb(showThumb: Boolean) {
        this.showThumb = showThumb
    }

    internal inner class TrendingListViewHolder(private val viewBinding: TrendingItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        internal fun bind(rank: String?, title: String) {
            viewBinding.rank.text = rank
            viewBinding.title.text = title
        }
    }

    internal inner class TrendingHeaderViewHolder(private val viewBinding: TrendingHeaderBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        internal fun bind(title: String) {
            viewBinding.title.text = title
        }
    }

    internal inner class TrendingFocusViewHolder(private val viewBinding: TrendingFocusItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        internal fun bind(title: String, showPostThumb: Boolean, thumb: String?) {
            viewBinding.postTitle.text = title
            if (showPostThumb) {
                viewBinding.postTitle.maxLines = 3
                viewBinding.postThumb.visibility = View.VISIBLE
                Glide.with(context).load(thumb).into(viewBinding.postThumb)
            } else {
                viewBinding.postTitle.maxLines = 2
                viewBinding.postThumb.visibility = View.GONE
            }
        }
        init {
            viewBinding.postThumb.clipToOutline = true
        }
    }

    private fun showPopupMenu(post: Trending) {
        if (post.url == null) {
            return
        }
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
        private const val TYPE_HEADER = 0
        private const val TYPE_FOCUS = 1
        private const val TYPE_ITEM = 2
    }
}
