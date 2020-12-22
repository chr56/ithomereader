package me.ikirby.ithomereader.ui.databinding.adapter

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:html")
fun bindTextViewHtml(textView: TextView, content: String) {
    textView.text = if (Build.VERSION.SDK_INT > 23) {
        Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(content)
    }
}
