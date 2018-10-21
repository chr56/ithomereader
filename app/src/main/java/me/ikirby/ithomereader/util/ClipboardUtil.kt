package me.ikirby.ithomereader.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.ui.util.ToastUtil


fun copyToClipboard(tag: String, content: String) {
    val clipboardManager = BaseApplication.instance
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(tag, content)
    clipboardManager.primaryClip = clip
    ToastUtil.showToast(R.string.copied_to_clipboard)
}
