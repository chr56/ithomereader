package me.ikirby.ithomereader.ui.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.CommentApiImpl
import me.ikirby.ithomereader.entity.Comment
import me.ikirby.ithomereader.ui.activity.CommentsActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.widget.CustomLinkTransformationMethod
import java.util.ArrayList

class CommentListAdapter(
    private val list: ArrayList<Comment>,
    private val inflater: LayoutInflater,
    private val activity: CommentsActivity,
    private val onLongClickListener: View.OnLongClickListener,
    private var cookie: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val commentSupport: String = activity.getString(R.string.comment_support)
    private val commentAgainst: String = activity.getString(R.string.comment_against)
    private val commentIsSupport: String = activity.getString(R.string.comment_is_support)
    private val commentIsAgainst: String = activity.getString(R.string.comment_is_against)

    private lateinit var job: Job

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val holder: RecyclerView.ViewHolder
        if (viewType == 0) {
            view = inflater.inflate(R.layout.comment_list_item, parent, false)
            holder = CommentListViewHolder(view)
        } else {
            view = inflater.inflate(R.layout.comment_reply_item, parent, false)
            holder = CommentReplyViewHolder(view)
        }
        view.setOnLongClickListener(onLongClickListener)
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val comment = list[position]
        val content = comment.content

        if (viewHolder is CommentListViewHolder) {
            viewHolder.nickText.text = comment.nick
            viewHolder.floorText.text = comment.floor
            viewHolder.posAndTimeText.text = comment.posAndTime
            if (content.contains("</a>")) {
                viewHolder.contentText.text = if (Build.VERSION.SDK_INT > 23) {
                    Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(content)
                }
            } else {
                viewHolder.contentText.text = content
            }
            viewHolder.deviceText.text = comment.device
            showVoteStatus(comment, viewHolder.supportText, viewHolder.againstText)
            setVoteClickListener(comment, viewHolder.supportText, viewHolder.againstText)
        } else if (viewHolder is CommentReplyViewHolder) {

            viewHolder.nickText.text = comment.nick
            viewHolder.floorText.text = comment.floor
            viewHolder.posAndTimeText.text = comment.posAndTime
            if (content.contains("</a>")) {
                viewHolder.contentText.text = if (Build.VERSION.SDK_INT > 23) {
                    Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(content)
                }
            } else {
                viewHolder.contentText.text = content
            }
            viewHolder.deviceText.text = comment.device
            showVoteStatus(comment, viewHolder.supportText, viewHolder.againstText)
            setVoteClickListener(comment, viewHolder.supportText, viewHolder.againstText)
        }
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        val floor = list[position].floor
        return if (floor.contains("#") && !floor.contains("楼")) {
            1
        } else {
            0
        }
    }

    private fun showVoteStatus(comment: Comment, supportText: TextView, againstText: TextView) {
        if (comment.isVotedSupport) {
            supportText.text = String.format(commentIsSupport, comment.supportCount)
        } else {
            supportText.text = String.format(commentSupport, comment.supportCount)
        }

        if (comment.isVotedAgainst) {
            againstText.text = String.format(commentIsAgainst, comment.againstCount)
        } else {
            againstText.text = String.format(commentAgainst, comment.againstCount)
        }
    }

    internal inner class CommentListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nickText: TextView = itemView.findViewById(R.id.comment_nick)
        var floorText: TextView = itemView.findViewById(R.id.comment_floor)
        var posAndTimeText: TextView = itemView.findViewById(R.id.comment_posandtime)
        var contentText: TextView = itemView.findViewById(R.id.comment_content)
        var deviceText: TextView = itemView.findViewById(R.id.comment_device)
        var supportText: TextView = itemView.findViewById(R.id.comment_support)
        var againstText: TextView = itemView.findViewById(R.id.comment_against)

        init {
            contentText.transformationMethod = CustomLinkTransformationMethod()
        }
    }

    internal inner class CommentReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nickText: TextView = itemView.findViewById(R.id.comment_nick)
        var floorText: TextView = itemView.findViewById(R.id.comment_floor)
        var posAndTimeText: TextView = itemView.findViewById(R.id.comment_posandtime)
        var contentText: TextView = itemView.findViewById(R.id.comment_content)
        var deviceText: TextView = itemView.findViewById(R.id.comment_device)
        var supportText: TextView = itemView.findViewById(R.id.comment_support)
        var againstText: TextView = itemView.findViewById(R.id.comment_against)

        init {
            contentText.transformationMethod = CustomLinkTransformationMethod()
        }
    }

    fun setCookie(cookie: String?) {
        this.cookie = cookie
    }

    private fun setVoteClickListener(comment: Comment, supportText: TextView, againstText: TextView) {
        supportText.setOnClickListener { commentVote(comment, 1, comment.isVotedSupport) }
        againstText.setOnClickListener { commentVote(comment, 2, comment.isVotedAgainst) }
        supportText.setOnLongClickListener {
            ToastUtil.showToast(R.string.cancel_vote)
            commentVote(comment, 1, true)
            true
        }
        againstText.setOnLongClickListener {
            commentVote(comment, 2, true)
            ToastUtil.showToast(R.string.cancel_vote)
            true
        }
    }

    private fun commentVote(comment: Comment, typeId: Int, isVoted: Boolean) {
        if (cookie == null) {
            activity.showLoginDialog()
            return
        }
        activity.launch {
            val result = withContext(Dispatchers.IO) {
                CommentApiImpl.commentVote(comment.selfId, typeId, isVoted, cookie!!)
            }
            if (result != null) {
                if (!result.matches("\\d+".toRegex())) {
                    ToastUtil.showToast(result)
                } else {
                    if (typeId == 1) {
                        comment.isVotedSupport = !isVoted
                        comment.supportCount = Integer.parseInt(result)
                    } else {
                        comment.isVotedAgainst = !isVoted
                        comment.againstCount = Integer.parseInt(result)
                    }
                    this@CommentListAdapter.notifyDataSetChanged()
                }
            } else {
                ToastUtil.showToast(R.string.timeout_no_internet)
            }
        }
    }

    fun getJob(): Job? {
        return if (::job.isInitialized) {
            job
        } else {
            null
        }
    }
}
