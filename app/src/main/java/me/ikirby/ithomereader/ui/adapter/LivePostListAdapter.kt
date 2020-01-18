package me.ikirby.ithomereader.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.live_msg_item.view.*
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.LIVE_MSG_TYPE_IMAGE
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.entity.LiveMsg
import me.ikirby.ithomereader.ui.activity.ImageViewerActivity
import java.util.*


class LivePostListAdapter(
    private val list: ArrayList<LiveMsg>,
    private val inflater: LayoutInflater
) : RecyclerView.Adapter<LivePostListAdapter.LiveMsgViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveMsgViewHolder {
        val view = inflater.inflate(R.layout.live_msg_item, parent, false)
        return LiveMsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiveMsgViewHolder, position: Int) {
        val (postTime, content, type) = list[position]

        if (type == LIVE_MSG_TYPE_IMAGE) {
            holder.timelineDot.visibility = View.GONE
            holder.timeText.visibility = View.GONE
            holder.text.visibility = View.GONE
            holder.imageContainer.visibility = View.VISIBLE
            Glide.with(inflater.context).load(content).into(holder.imageContainer)
            holder.imageContainer.setOnClickListener {
                val intent = Intent(inflater.context, ImageViewerActivity::class.java).apply {
                    putExtra(KEY_URL, content)
                }
                inflater.context.startActivity(intent)
            }
        } else {
            holder.timelineDot.visibility = View.VISIBLE
            holder.timeText.visibility = View.VISIBLE
            holder.imageContainer.visibility = View.GONE
            if (content == "") {
                holder.text.visibility = View.GONE
            } else {
                holder.text.visibility = View.VISIBLE
                holder.text.text = content
            }
            holder.timeText.text = postTime
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class LiveMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timelineDot: ImageView = itemView.timeline_dot
        var timeText: TextView = itemView.post_time
        var text: TextView = itemView.post_txt
        var imageContainer: ImageView = itemView.post_image_container

    }
}
