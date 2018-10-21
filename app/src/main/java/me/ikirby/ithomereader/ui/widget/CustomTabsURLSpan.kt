package me.ikirby.ithomereader.ui.widget

import android.text.style.URLSpan
import android.view.View
import me.ikirby.ithomereader.util.openLink

class CustomTabsURLSpan(url: String) : URLSpan(url) {

    override fun onClick(widget: View) {
        openLink(widget.context, url)
    }
}
