package me.ikirby.ithomereader.ui.util


import android.annotation.SuppressLint
import android.widget.Toast

import androidx.annotation.StringRes
import me.ikirby.ithomereader.BaseApplication

@SuppressLint("ShowToast")
object ToastUtil {
    private val sToast by lazy {
        Toast.makeText(BaseApplication.instance.applicationContext, "", Toast.LENGTH_SHORT)
    }

    fun showToast(@StringRes resId: Int) {
        sToast.setText(resId)
        sToast.show()
    }

    fun showToast(content: String?) {
        sToast.setText(content)
        sToast.show()
    }
}
