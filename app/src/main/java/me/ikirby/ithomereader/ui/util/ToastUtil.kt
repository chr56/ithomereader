package me.ikirby.ithomereader.ui.util


import android.annotation.SuppressLint
import android.widget.Toast

import androidx.annotation.StringRes
import me.ikirby.ithomereader.BaseApplication

object ToastUtil {
    private var sToast: Toast? = null

    @SuppressLint("ShowToast")
    private fun createToast() {
        if (sToast == null) {
            sToast = Toast.makeText(BaseApplication.instance.applicationContext,
                    "", Toast.LENGTH_SHORT)
        }
    }

    fun showToast(@StringRes resId: Int) {
        if (sToast == null) {
            createToast()
        }
        sToast!!.setText(resId)
        sToast!!.show()
    }

    fun showToast(content: String?) {
        if (sToast == null) {
            createToast()
        }
        sToast!!.setText(content)
        sToast!!.show()
    }
}
