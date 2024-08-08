package me.ikirby.ithomereader.ui.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.CommentListItemBinding
import me.ikirby.ithomereader.databinding.CommentReplyItemBinding
import me.ikirby.ithomereader.entity.app.comment.Comment
import me.ikirby.ithomereader.ui.widget.CustomLinkTransformationMethod

class CommentListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_COMMENT = 1
        private const val VIEW_TYPE_REPLY = 2
    }

    var list = emptyList<Comment>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var expandClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_COMMENT) {
            val binding =
                CommentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.commentContent.transformationMethod = CustomLinkTransformationMethod()
            CommentItemViewHolder(binding)
        } else {
            val binding =
                CommentReplyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.commentContent.transformationMethod = CustomLinkTransformationMethod()
            CommentReplyItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val comment = list[position]
        if (viewHolder is CommentItemViewHolder) {
            viewHolder.bind(comment)
            viewHolder.binding.commentExpand.setOnClickListener {
                expandClickListener?.onClick(it, position)
            }
        } else if (viewHolder is CommentReplyItemViewHolder) {
            viewHolder.bind(comment)
        }
    }

    override fun getItemId(position: Int): Long {
        return list[position].cid
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].isReply) {
            VIEW_TYPE_REPLY
        } else {
            VIEW_TYPE_COMMENT
        }
    }

    class CommentItemViewHolder(val binding: CommentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            val context = binding.root.context
            with(binding) {
                commentNick.text = comment.nickname
                commentDevice.text = comment.device ?: context.getString(R.string.unknown)
                commentFloor.text = comment.floor
                commentPosandtime.text =
                    context.getString(R.string.comment_posandtime_format, comment.region, comment.time)
                commentContent.text =
                    HtmlCompat.fromHtml(comment.content,HtmlCompat.FROM_HTML_MODE_COMPACT)
                commentExpand.text = context.getString(R.string.comment_expand, comment.replyCount)
                commentExpand.visibility = if (comment.replyCount > 0) VISIBLE else GONE
                commentSupport.text = context.getString(R.string.comment_support, comment.support)
                commentAgainst.text = context.getString(R.string.comment_against, comment.against)
            }
        }
    }

    class CommentReplyItemViewHolder(private val binding: CommentReplyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            val context = binding.root.context
            with(binding) {
                commentNick.text = comment.nickname
                commentDevice.text = comment.device ?: context.getString(R.string.unknown)
                commentFloor.text = comment.floor
                commentPosandtime.text =
                    context.getString(R.string.comment_posandtime_format, comment.region, comment.time)
                commentContent.text =
                    HtmlCompat.fromHtml(comment.content,HtmlCompat.FROM_HTML_MODE_COMPACT)
                commentSupport.text = context.getString(R.string.comment_support, comment.support)
                commentAgainst.text = context.getString(R.string.comment_against, comment.against)
            }
        }
    }
}
