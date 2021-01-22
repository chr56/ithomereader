package me.ikirby.ithomereader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

    class CommentItemViewHolder(private val binding: CommentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.comment = comment
            binding.executePendingBindings()
        }
    }

    class CommentReplyItemViewHolder(private val binding: CommentReplyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.comment = comment
            binding.executePendingBindings()
        }
    }
}
