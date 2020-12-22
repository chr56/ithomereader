package me.ikirby.ithomereader.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ikirby.ithomereader.databinding.CommentListItemBinding
import me.ikirby.ithomereader.entity.app.comment.Comment
import me.ikirby.ithomereader.ui.widget.CustomLinkTransformationMethod

class CommentListAdapter : RecyclerView.Adapter<CommentListAdapter.CommentItemViewHolder>() {

    var list = emptyList<Comment>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentItemViewHolder {
        val binding =
            CommentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.commentContent.transformationMethod = CustomLinkTransformationMethod()
        return CommentItemViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: CommentItemViewHolder, position: Int) {
        val comment = list[position]
        viewHolder.bind(comment)
    }

    override fun getItemId(position: Int): Long {
        return list[position].cid
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class CommentItemViewHolder(private val binding: CommentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.comment = comment
            binding.executePendingBindings()
        }
    }
}
