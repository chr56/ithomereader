package me.ikirby.ithomereader.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.slide_image_view.view.*
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.ui.activity.ArticleActivity
import me.ikirby.ithomereader.ui.activity.ImageViewerActivity
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.openLinkInBrowser

class SlideBannerAdapter(private val list: List<Article>, private val context: Context) : RecyclerView.Adapter<SlideBannerAdapter.BannerItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.slide_image_view, parent, false)
        val holder = BannerItemViewHolder(view)
        view.setOnClickListener {
            val (title, _, url) = list[holder.adapterPosition]
            val intent = Intent(context, ArticleActivity::class.java).apply {
                putExtra("url", url)
                putExtra("title", title)
            }
            context.startActivity(intent)
        }
        view.setOnLongClickListener {
            showPopupMenu(list[holder.adapterPosition])
            true
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BannerItemViewHolder, position: Int) {
        val (title, _, _, thumb) = list[position]
        Glide.with(context).asBitmap().load(thumb).listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                Palette.from(resource).generate { palette ->
                    val bgColor: Int
                    val textColor: Int
                    when {
                        palette!!.mutedSwatch != null -> {
                            bgColor = palette.getMutedColor(Color.BLACK)
                            textColor = palette.mutedSwatch!!.titleTextColor
                        }
                        palette.darkMutedSwatch != null -> {
                            bgColor = palette.getDarkMutedColor(Color.BLACK)
                            textColor = palette.darkMutedSwatch!!.titleTextColor
                        }
                        palette.lightMutedSwatch != null -> {
                            bgColor = palette.getLightMutedColor(Color.BLACK)
                            textColor = palette.lightMutedSwatch!!.titleTextColor
                        }
                        palette.vibrantSwatch != null -> {
                            bgColor = palette.getVibrantColor(Color.BLACK)
                            textColor = palette.vibrantSwatch!!.titleTextColor
                        }
                        palette.darkVibrantSwatch != null -> {
                            bgColor = palette.getDarkVibrantColor(Color.BLACK)
                            textColor = palette.darkVibrantSwatch!!.titleTextColor
                        }
                        palette.lightVibrantSwatch != null -> {
                            bgColor = palette.getLightVibrantColor(Color.BLACK)
                            textColor = palette.lightVibrantSwatch!!.titleTextColor
                        }
                        else -> {
                            bgColor = Color.BLACK
                            textColor = Color.WHITE
                        }
                    }
                    val colors = intArrayOf(bgColor, Color.TRANSPARENT)
                    val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
                    holder.bg.setBackgroundColor(bgColor)
                    holder.gradient.background = gradient
                    holder.title.setTextColor(textColor)
                }
                return false
            }
        }).into(holder.imageView)
        holder.title.text = title
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class BannerItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.slide_image
        var title: TextView = itemView.slide_title
        var bg: View = itemView.slide_bg
        var gradient: View = itemView.slide_gradient

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
                        context.startActivity(Intent.createChooser(share,
                                context.getString(R.string.share) + " " + post.title))
                    }
                    R.id.copy_link -> copyToClipboard("ITHomeNewsLink", post.url)
                    R.id.view_thumb -> {
                        val intent = Intent(context, ImageViewerActivity::class.java).apply {
                            putExtra("url", post.thumb)
                        }
                        context.startActivity(intent)
                    }
                    R.id.open_in_browser -> openLinkInBrowser(context, post.url)
                }
            }
        })
    }
}
