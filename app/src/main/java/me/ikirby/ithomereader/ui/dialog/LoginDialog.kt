package me.ikirby.ithomereader.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.login_dialog.*
import kotlinx.coroutines.*
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.UserApiImpl
import me.ikirby.ithomereader.ui.activity.CommentPostActivity
import me.ikirby.ithomereader.ui.activity.CommentsActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import kotlin.coroutines.CoroutineContext


class LoginDialog : DialogFragment(), CoroutineScope {

    private val preferences: SharedPreferences = BaseApplication.preferences
    private var cookie: String? = null
    private lateinit var callbackPreference: Preference

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            cookie = args.getString("cookie")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(activity)
                .setView(R.layout.login_dialog).create()
        dialog.setOnShowListener { dialogInterface ->
            val dlg = dialogInterface as AlertDialog
            val btnLogin = dlg.btn_login
            val btnLogout = dlg.btn_logout
            val btnCancel = dlg.btn_cancel
            val textUsername = dlg.username
            val textPassword = dlg.password

            btnLogin.setOnClickListener {
                val username = textUsername.text.toString()
                val password = textPassword.text.toString()
                if (username == "" || password == "") {
                    ToastUtil.showToast(R.string.user_pass_empty)
                } else {
                    btnLogin.isEnabled = false
                    doLogin(username, password, btnLogin)
                }
            }

            if (cookie != null) {
                ToastUtil.showToast(R.string.already_has_login_info)
                btnLogout.visibility = View.VISIBLE
                btnLogout.setOnClickListener {
                    doLogout()
                    btnLogout.visibility = View.GONE
                }
            }

            btnCancel.setOnClickListener { this@LoginDialog.dismiss() }
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface?) {
        val activity = activity
        if (activity is CommentsActivity) {
            activity.loadCookie()
        } else if (activity is CommentPostActivity) {
            activity.loadCookie()
        }
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        coroutineContext.cancelChildren()
        super.onDestroyView()
    }

    private fun doLogin(username: String, password: String, btnLogin: MaterialButton) {
        val loadProgress = this.dialog.findViewById<ProgressBar>(R.id.load_progress)
        loadProgress.visibility = View.VISIBLE
        launch {
            val cookie = withContext(Dispatchers.IO) { UserApiImpl.login(username, password) }
            if (cookie != null) {
                val cookieStr = "user=$cookie"
                preferences.edit().putString("user_hash", cookieStr)
                        .putString("username", username).apply()
                this@LoginDialog.cookie = cookieStr
                if (::callbackPreference.isInitialized) {
                    callbackPreference.title = username
                }
                ToastUtil.showToast(R.string.login_success)
                this@LoginDialog.dismiss()
            } else {
                ToastUtil.showToast(R.string.login_failed)
                loadProgress.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        }
    }

    private fun doLogout() {
        preferences.edit().remove("user_hash").remove("username").apply()
        this.cookie = null
        if (::callbackPreference.isInitialized) {
            callbackPreference.setTitle(R.string.login_title)
        }
        ToastUtil.showToast(R.string.login_info_cleared)
    }

    fun setCallbackPreference(callBackPreference: Preference) {
        this.callbackPreference = callBackPreference
    }

    companion object {

        fun newInstance(cookie: String?): LoginDialog {
            val dialog = LoginDialog()
            val args = Bundle()
            args.putString("cookie", cookie)
            dialog.arguments = args
            return dialog
        }
    }
}
