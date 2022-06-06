package me.ikirby.ithomereader.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.SETTINGS_KEY_USERNAME
import me.ikirby.ithomereader.SETTINGS_KEY_USER_HASH
import me.ikirby.ithomereader.api.impl.UserApiImpl
import me.ikirby.ithomereader.databinding.LoginDialogBinding
import me.ikirby.ithomereader.ui.util.ToastUtil

fun showLoginDialog(context: Context, loginSuccessCallback: ((username: String) -> Unit)?) {
    val coroutineContext = Dispatchers.Main + SupervisorJob()
    val coroutineScope = CoroutineScope(coroutineContext)

    val binding: LoginDialogBinding = LoginDialogBinding.inflate(LayoutInflater.from(context))

    val dialog = MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .setNegativeButton(R.string.cancel, null)
        .create()

    dialog.setOnShowListener {

        binding.btnLogin.setOnClickListener {

            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            if (username == "" || password == "") {
                ToastUtil.showToast(R.string.user_pass_empty)
            } else {
                binding.btnLogin.isEnabled = false
                binding.loadProgress.visibility = View.VISIBLE

                coroutineScope.launch {
                    val userHash = withContext(Dispatchers.IO) { UserApiImpl.login(username, password) }
                    if (userHash != null) {
                        val cookieStr = "user=$userHash"
                        BaseApplication.preferences.edit()
                            .putString(SETTINGS_KEY_USER_HASH, cookieStr)
                            .putString(SETTINGS_KEY_USERNAME, username)
                            .apply()
                        ToastUtil.showToast(R.string.login_success)
                        loginSuccessCallback?.invoke(username)
                        dialog.dismiss()
                    } else {
                        ToastUtil.showToast(R.string.login_failed)
                        binding.loadProgress.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                }
            }
        }
    }

    dialog.setOnDismissListener {
        coroutineContext.cancelChildren()
    }

    dialog.show()
}

fun showLogoutDialog(context: Context, username: String, preference: Preference) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.logout_title)
        .setMessage(context.getString(R.string.logout_message, username))
        .setPositiveButton(R.string.ok) { _, _ ->
            BaseApplication.preferences.edit()
                .remove(SETTINGS_KEY_USER_HASH)
                .remove(SETTINGS_KEY_USERNAME)
                .apply()
            preference.title = context.getString(R.string.login_title)
            ToastUtil.showToast(R.string.login_info_cleared)
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
