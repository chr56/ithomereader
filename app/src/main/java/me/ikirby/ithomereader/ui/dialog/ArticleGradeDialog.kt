package me.ikirby.ithomereader.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.article_grade_dialog.view.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.ArticleApiImpl
import me.ikirby.ithomereader.ui.util.ToastUtil

class ArticleGradeDialog : BottomSheetDialogFragment(), View.OnClickListener {

    private var newsId: String? = null
    private var cookie: String? = null

    private val parentJob = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            this.newsId = args.getString("NEWS_ID")
            this.cookie = BaseApplication.preferences.getString("user_hash", null)
        }
        when {
            BaseApplication.isNightMode -> setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
            BaseApplication.isOStyleLight -> setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog_OStyle)
            else -> setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog_Light)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.article_grade_dialog, container, false)
        view.trash.setOnClickListener(this)
        view.soso.setOnClickListener(this)
        view.great.setOnClickListener(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        load()
    }

    private fun load() {
        view!!.load_progress.visibility = View.VISIBLE
        view!!.article_grade.visibility = View.GONE
        view!!.article_grade_detail.visibility = View.GONE
        GlobalScope.launch(Dispatchers.Main + parentJob) {
            val articleGrade = ArticleApiImpl.getArticleGrade(newsId!!, cookie).await()
            if (articleGrade != null) {
                view!!.article_grade.text = articleGrade.score
                view!!.trash.text = articleGrade.trashCount
                view!!.soso.text = articleGrade.sosoCount
                view!!.great.text = articleGrade.greatCount
            } else {
                view!!.article_grade.setText(R.string.timeout_no_internet)
                view!!.article_grade.visibility = View.VISIBLE
                view!!.load_progress.visibility = View.GONE
            }
            view!!.article_grade.visibility = View.VISIBLE
            view!!.article_grade_detail.visibility = View.VISIBLE
            view!!.load_progress.visibility = View.GONE
        }
    }

    private fun vote(grade: Int) {
        if (cookie == null && activity != null) {
            ToastUtil.showToast(R.string.please_login_first)
            LoginDialog.newInstance(null)
                    .show(activity!!.supportFragmentManager, "loginDialog")
            dismiss()
            return
        }
        GlobalScope.launch(Dispatchers.Main + parentJob) {
            val voteResult = ArticleApiImpl.articleVote(newsId!!, grade, cookie!!).await()
            if (voteResult != null) {
                try {
                    if (voteResult.contains("打分成功")) {
                        load()
                    } else {
                        ToastUtil.showToast(R.string.already_voted)
                    }
                } catch (ignored: Throwable) {
                }
            } else {
                ToastUtil.showToast(R.string.timeout_no_internet)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.trash -> vote(0)
            R.id.soso -> vote(1)
            R.id.great -> vote(2)
        }
    }

    override fun onDestroyView() {
        parentJob.cancel()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(newsId: String): ArticleGradeDialog {
            val args = Bundle()
            args.putString("NEWS_ID", newsId)
            val fragment = ArticleGradeDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
