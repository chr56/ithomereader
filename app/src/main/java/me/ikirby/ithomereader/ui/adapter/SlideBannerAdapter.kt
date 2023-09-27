package me.ikirby.ithomereader.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import me.ikirby.ithomereader.CLIP_TAG_NEWS_LINK
import me.ikirby.ithomereader.KEY_TITLE
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.SlideImageViewBinding
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.ui.activity.ArticleActivity
import me.ikirby.ithomereader.ui.activity.ImageViewerActivity
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.openLinkInBrowser

class SlideBannerAdapter(private val list: List<Article>, private val context: Context) :
    RecyclerView.Adapter<SlideBannerAdapter.BannerItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerItemViewHolder {
        val viewBinding = SlideImageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = BannerItemViewHolder(viewBinding)
        // todo: move click listeners to onBindViewHolder
        viewBinding.root.setOnClickListener {
            val (title, _, url) = list[holder.bindingAdapterPosition]
            val intent = Intent(context, ArticleActivity::class.java).apply {
                putExtra(KEY_URL, url)
                putExtra(KEY_TITLE, title)
            }
            context.startActivity(intent)
        }
        viewBinding.root.setOnLongClickListener {
            showPopupMenu(list[holder.bindingAdapterPosition])
            true
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BannerItemViewHolder, position: Int) {
        val (_, _, _, thumb) = list[position]
        holder.bind(thumb)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class BannerItemViewHolder(private val viewBinding: SlideImageViewBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        internal fun bind(thumb: String?) {
            val loader = Coil.imageLoader(context)
            loader.enqueue(
                ImageRequest.Builder(context)
                    .data(thumb)
                    .target(viewBinding.slideImage)
                    .build()
            )
        }
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
}
