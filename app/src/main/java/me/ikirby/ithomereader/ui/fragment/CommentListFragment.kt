package me.ikirby.ithomereader.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.api.impl.CommentApiImpl
import me.ikirby.ithomereader.entity.Comment
import me.ikirby.ithomereader.ui.activity.CommentPostActivity
import me.ikirby.ithomereader.ui.activity.CommentsActivity
import me.ikirby.ithomereader.ui.adapter.CommentListAdapter
import me.ikirby.ithomereader.ui.base.BaseFragment
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.ui.widget.OnBottomReachedListener
import me.ikirby.ithomereader.util.copyToClipboard
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist

class CommentListFragment : BaseFragment() {

    private lateinit var newsId: String
    private lateinit var hash: String
    private var cookie: String? = null
    private lateinit var url: String
    private var lapinId: String? = null
    private var isLapin: Boolean = false
    private var isHotComment: Boolean = false

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var commentList: ArrayList<Comment>
    private lateinit var adapter: CommentListAdapter
    private var page = 0
    private var isLoading = false
    private var isRefresh = false

    private val onLongClickListener = View.OnLongClickListener { v ->
        showPopupMenu(commentList[requireView().list_view.getChildLayoutPosition(v)])
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (arguments != null) {
            newsId = requireArguments().getString(KEY_NEWS_ID)!!
            hash = requireArguments().getString(KEY_COMMENT_HASH)!!
            cookie = requireArguments().getString(KEY_COOKIE)
            url = requireArguments().getString(KEY_URL)!!
            lapinId = requireArguments().getString(KEY_LAPIN_ID)
            isHotComment = requireArguments().getBoolean(KEY_HOT_COMMENT)
        }
        if (lapinId != null) {
            newsId = lapinId!!
            isLapin = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_layout, container, false)

        layoutManager = LinearLayoutManager(activity)
        view.list_view.layoutManager = layoutManager

        view.swipe_refresh.setOnRefreshListener { reloadList() }
        view.error_placeholder.setOnClickListener { reloadList() }

        if (savedInstanceState != null) {
            commentList = savedInstanceState.getParcelableArrayList(KEY_COMMENT_LIST) ?: ArrayList()
        }

        if (savedInstanceState == null || commentList.isEmpty()) {
            commentList = ArrayList()
            adapter = CommentListAdapter(
                commentList, LayoutInflater.from(context),
                activity as CommentsActivity, onLongClickListener, cookie
            )
            view.list_view.adapter = adapter
        } else {
            page = savedInstanceState.getInt(KEY_PAGE)
            adapter = CommentListAdapter(
                commentList, LayoutInflater.from(context),
                activity as CommentsActivity, onLongClickListener, cookie
            )
            view.list_view.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_STATE))
        }

        view.list_view.setOnBottomReachedListener(object : OnBottomReachedListener {
            override fun onBottomReached() {
                loadList()
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) reloadList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PAGE, page)
        outState.putParcelableArrayList(KEY_COMMENT_LIST, commentList)
        outState.putParcelable(KEY_LIST_STATE, layoutManager.onSaveInstanceState())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                layoutManager.scrollToPosition(0)
                reloadList()
            }
        }
        return true
    }

    override fun onDestroyView() {
        adapter.getJob()?.cancel()
        super.onDestroyView()
    }

    private fun reloadList() {
        if (!isLoading) {
            requireView().list_view.setAllContentLoaded(false)
            page = 0
            isRefresh = true
            loadList()
        }
    }

    private fun loadList() {
        if (!isLoading) {
            requireView().swipe_refresh.isRefreshing = true
            isLoading = true
            page++
            launch {
                val comments = withContext(Dispatchers.IO) {
                    if (isHotComment) {
                        if (isRefresh) {
                            CommentApiImpl.getHotCommentList(newsId, hash, page, null, isLapin)
                        } else {
                            CommentApiImpl.getHotCommentList(newsId, hash, page, commentList, isLapin)
                        }
                    } else {
                        if (isRefresh) {
                            CommentApiImpl.getAllCommentsList(newsId, hash, page, null, isLapin)
                        } else {
                            CommentApiImpl.getAllCommentsList(newsId, hash, page, commentList, isLapin)
                        }
                    }
                }
                if (comments != null) {
                    if (comments.isNotEmpty()) {
                        if (isRefresh) {
                            commentList.clear()
                        }
                        commentList.addAll(comments)
                        adapter.notifyDataSetChanged()
                        if (isLapin && isHotComment) {
                            requireView().list_view.setAllContentLoaded(true)
                        }
                    } else {
                        page--
                        requireView().list_view.setAllContentLoaded(true)
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    page--
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                UiUtil.switchVisibility(requireView().list_view, requireView().error_placeholder, commentList.size)
                isLoading = false
                isRefresh = false
                requireView().swipe_refresh.isRefreshing = false
            }
        }
    }

    fun setCookie(cookie: String?) {
        if (arguments != null) {
            requireArguments().putString(KEY_COOKIE, cookie)
            adapter.setCookie(cookie)
            this.cookie = cookie
        }
    }

    fun expandComment(id: String, position: Int) {
        if (!isLoading) {
            requireView().swipe_refresh.isRefreshing = true
            isLoading = true
            launch {
                val comments = withContext(Dispatchers.IO) {
                    CommentApiImpl.getSingleComment(id, newsId)
                }
                if (comments != null) {
                    if (comments.isNotEmpty()) {
                        commentList.removeAt(position)
                        commentList.addAll(position, comments)
                        adapter.notifyDataSetChanged()
                    } else {
                        ToastUtil.showToast(R.string.no_content_to_display)
                    }
                } else {
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                isLoading = false
                requireView().swipe_refresh.isRefreshing = false
            }
        }
    }

    private fun showPopupMenu(comment: Comment) {
        val menuRes: Int = if (isHotComment) {
            R.menu.hot_comments_context
        } else {
            R.menu.comments_context
        }
        UiUtil.showBottomSheetMenu(requireContext(), object : BottomSheetMenu.BottomSheetMenuListener {
            override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                inflater.inflate(menuRes, menu)
            }

            override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                when (item.itemId) {
                    R.id.reply_comment -> {
                        val intent = Intent(context, CommentPostActivity::class.java).apply {
                            putExtra(KEY_NEWS_ID, newsId)
                            putExtra(KEY_TITLE, activity!!.intent.getStringExtra(KEY_TITLE))
                            putExtra(KEY_COMMENT_REPLY_TO, comment)
                        }
                        startActivity(intent)
                    }
                    R.id.copy_content -> copyToClipboard(
                        CLIP_TAG_COMMENT,
                        Jsoup.clean(
                            comment.content, "", Whitelist.none(),
                            Document.OutputSettings().prettyPrint(false)
                        )
                    )
                    R.id.share -> {
                        val nick = comment.nick
                        val title = activity!!.intent.getStringExtra(KEY_TITLE)
                        val content = Jsoup.clean(
                            comment.content, "", Whitelist.none(),
                            Document.OutputSettings().prettyPrint(false)
                        )
                        val shareText = getString(R.string.share_comment_content, nick, title, content, url)
                        val share = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(share, getString(R.string.share_ones_comment, nick)))
                    }
                }
            }
        })
    }

    companion object {
        fun newInstance(
            id: String,
            hash: String,
            cookie: String?,
            url: String,
            lapinId: String?,
            isHotComment: Boolean = false
        ): CommentListFragment {
            val fragment = CommentListFragment()
            val args = Bundle()
            args.putString(KEY_NEWS_ID, id)
            args.putString(KEY_COMMENT_HASH, hash)
            args.putString(KEY_COOKIE, cookie)
            args.putString(KEY_URL, url)
            args.putString(KEY_LAPIN_ID, lapinId)
            args.putBoolean(KEY_HOT_COMMENT, isHotComment)
            fragment.arguments = args
            return fragment
        }
    }
}
