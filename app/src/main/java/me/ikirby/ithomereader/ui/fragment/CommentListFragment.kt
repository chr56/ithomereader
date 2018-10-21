package me.ikirby.ithomereader.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_layout.view.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import me.ikirby.ithomereader.R
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

    private lateinit var id: String
    private lateinit var hash: String
    private var cookie: String? = null
    private lateinit var url: String
    private var lapinId: String? = null
    private var isLapin: Boolean = false
    private var isHotComment: Boolean = false

    private lateinit var layoutManager: LinearLayoutManager
    private var commentList: ArrayList<Comment>? = null
    private lateinit var adapter: CommentListAdapter
    private var page = 0
    private var isLoading = false
    private var isRefresh = false

    private val onLongClickListener = View.OnLongClickListener { v ->
        showPopupMenu(commentList!![view!!.list_view.getChildLayoutPosition(v)])
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (arguments != null) {
            id = arguments!!.getString(NEWS_ID)!!
            hash = arguments!!.getString(HASH)!!
            cookie = arguments!!.getString(COOKIE)
            url = arguments!!.getString(NEWS_URL)!!
            lapinId = arguments!!.getString(LAPIN_ID)
            isHotComment = arguments!!.getBoolean(IS_HOT_COMMENT)
        }
        if (lapinId != null) {
            id = lapinId!!
            isLapin = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_layout, container, false)

        layoutManager = LinearLayoutManager(activity)
        view.list_view.layoutManager = layoutManager

        view.swipe_refresh.setColorSchemeResources(UiUtil.getAccentColorRes())
        view.swipe_refresh.setProgressBackgroundColorSchemeResource(UiUtil.getWindowBackgroundColorRes())
        view.swipe_refresh.setOnRefreshListener { reloadList() }

        view.error_placeholder.setOnClickListener { reloadList() }

        if (savedInstanceState != null) {
            commentList = savedInstanceState.getParcelableArrayList("commentList")
        }

        if (savedInstanceState == null || commentList == null || commentList!!.size == 0) {
            commentList = ArrayList()
            adapter = CommentListAdapter(commentList!!, LayoutInflater.from(context),
                    activity as CommentsActivity, onLongClickListener, cookie)
            view.list_view.adapter = adapter
        } else {
            page = savedInstanceState.getInt("page")
            adapter = CommentListAdapter(commentList!!, LayoutInflater.from(context),
                    activity as CommentsActivity, onLongClickListener, cookie)
            view.list_view.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("list_state"))
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
        outState.putInt("page", page)
        outState.putParcelableArrayList("commentList", commentList)
        outState.putParcelable("list_state", layoutManager.onSaveInstanceState())
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
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
            view!!.list_view.setAllContentLoaded(false)
            page = 0
            isRefresh = true
            loadList()
        }
    }

    private fun loadList() {
        if (!isLoading) {
            view!!.swipe_refresh.isRefreshing = true
            isLoading = true
            page++
            GlobalScope.launch(Dispatchers.Main + parentJob) {
                val comments = if (isHotComment) {
                    if (isRefresh) {
                        CommentApiImpl.getHotCommentList(id, hash, page, null, isLapin).await()
                    } else {
                        CommentApiImpl.getHotCommentList(id, hash, page, commentList, isLapin).await()
                    }
                } else {
                    if (isRefresh) {
                        CommentApiImpl.getAllCommentsList(id, hash, page, null, isLapin).await()
                    } else {
                        CommentApiImpl.getAllCommentsList(id, hash, page, commentList, isLapin).await()
                    }
                }
                if (comments != null) {
                    if (comments.isNotEmpty()) {
                        if (isRefresh) {
                            commentList!!.clear()
                        }
                        commentList!!.addAll(comments)
                        adapter.notifyDataSetChanged()
                        if (isLapin && isHotComment) {
                            view!!.list_view.setAllContentLoaded(true)
                        }
                    } else {
                        page--
                        view!!.list_view.setAllContentLoaded(true)
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    page--
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                UiUtil.switchVisibility(view!!.list_view, view!!.error_placeholder, commentList!!.size)
                isLoading = false
                isRefresh = false
                view!!.swipe_refresh.isRefreshing = false
            }
        }
    }

    fun setCookie(cookie: String?) {
        if (arguments != null) {
            arguments!!.putString("cookie", cookie)
            adapter.setCookie(cookie)
            this.cookie = cookie
        }
    }

    private fun showPopupMenu(comment: Comment) {
        val menuRes: Int = if (isHotComment) {
            R.menu.hot_comments_context
        } else {
            R.menu.comments_context
        }
        UiUtil.showBottomSheetMenu(context!!, object : BottomSheetMenu.BottomSheetMenuListener {
            override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                inflater.inflate(menuRes, menu)
            }

            override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                assert(activity != null)
                when (item.itemId) {
                    R.id.reply_comment -> {
                        val intent = Intent(context, CommentPostActivity::class.java).apply {
                            putExtra("id", id)
                            putExtra("title", activity!!.intent.getStringExtra("title"))
                            putExtra("replyTo", comment)
                        }
                        startActivity(intent)
                    }
                    R.id.copy_content -> copyToClipboard("ITHomeComment",
                            Jsoup.clean(comment.content, "", Whitelist.none(),
                                    Document.OutputSettings().prettyPrint(false)))
                    R.id.share -> {
                        val nick = comment.nick
                        val title = activity!!.intent.getStringExtra("title")
                        val content = Jsoup.clean(comment.content, "", Whitelist.none(),
                                Document.OutputSettings().prettyPrint(false))
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
        private const val NEWS_ID = "id"
        private const val HASH = "hash"
        private const val COOKIE = "cookie"
        private const val NEWS_URL = "url"
        private const val LAPIN_ID = "lapin_id"
        private const val IS_HOT_COMMENT = "is_hot"

        fun newInstance(id: String, hash: String, cookie: String?, url: String, lapinId: String?, isHotComment: Boolean = false): CommentListFragment {
            val fragment = CommentListFragment()
            val args = Bundle()
            args.putString(NEWS_ID, id)
            args.putString(HASH, hash)
            args.putString(COOKIE, cookie)
            args.putString(NEWS_URL, url)
            args.putString(LAPIN_ID, lapinId)
            args.putBoolean(IS_HOT_COMMENT, isHotComment)
            fragment.arguments = args
            return fragment
        }
    }
}
