package me.ikirby.ithomereader.ui.widget

import android.graphics.Rect
import android.text.Spannable
import android.text.Spanned
import android.text.method.TransformationMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.View
import android.widget.TextView

class CustomLinkTransformationMethod : TransformationMethod {

    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        if (view is TextView) {
            Linkify.addLinks(view, Linkify.WEB_URLS)
            if (view.text == null || view.text !is Spannable) {
                return source
            }
            view.movementMethod = ClickableMovementMethod
            view.isClickable = false
            view.isFocusable = false
            view.isLongClickable = false
            val text = view.text as Spannable
            val urlSpans = text.getSpans(0, view.length(), URLSpan::class.java)
            for (span in urlSpans) {
                val start = text.getSpanStart(span)
                val end = text.getSpanEnd(span)
                val url = span.url
                text.removeSpan(span)
                text.setSpan(CustomTabsURLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return text
        }
        return source
    }

    override fun onFocusChanged(view: View, charSequence: CharSequence, b: Boolean, i: Int, rect: Rect) {

    }
}
