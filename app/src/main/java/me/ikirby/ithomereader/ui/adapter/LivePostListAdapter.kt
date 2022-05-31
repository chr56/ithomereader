package me.ikirby.ithomereader.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.LIVE_MSG_TYPE_IMAGE
import me.ikirby.ithomereader.databinding.LiveMsgItemBinding
import me.ikirby.ithomereader.entity.LiveMsg
import me.ikirby.ithomereader.ui.activity.ImageViewerActivity

class LivePostListAdapter(
    private val list: ArrayList<LiveMsg>,
    private val inflater: LayoutInflater
) : RecyclerView.Adapter<LivePostListAdapter.LiveMsgViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveMsgViewHolder {
        return LiveMsgViewHolder(LiveMsgItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LiveMsgViewHolder, position: Int) {
        val (postTime, content, type) = list[position]
        holder.bind(
            LiveMsgModel(postTime, content, type)
        )
    }

    override fun getItemCount(): Int = list.size

    inner class LiveMsgViewHolder(private val viewBinding: LiveMsgItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {

        internal fun bind(data: LiveMsgModel) {
            if (data.type == LIVE_MSG_TYPE_IMAGE) {
                viewBinding.timelineDot.visibility = View.GONE
                viewBinding.postTime.visibility = View.GONE
                viewBinding.postTxt.visibility = View.GONE
                viewBinding.postImageContainer.visibility = View.VISIBLE
                Glide.with(inflater.context).load(data.content).into(viewBinding.postImageContainer)
                viewBinding.postImageContainer.setOnClickListener {
                    val intent = Intent(inflater.context, ImageViewerActivity::class.java).apply {
                        putExtra(KEY_URL, data.content)
                    }
                    inflater.context.startActivity(intent)
                }
            } else {
                viewBinding.timelineDot.visibility = View.VISIBLE
                viewBinding.postTime.visibility = View.VISIBLE
                viewBinding.postImageContainer.visibility = View.GONE
                if (data.content == "") {
                    viewBinding.postTxt.visibility = View.GONE
                } else {
                    viewBinding.postTxt.visibility = View.VISIBLE
                    viewBinding.postTxt.text = data.content
                }
                viewBinding.postTime.text = data.postTime
            }
        }
    }
    internal data class LiveMsgModel(val postTime: String?, val content: String, val type: Int)
}
