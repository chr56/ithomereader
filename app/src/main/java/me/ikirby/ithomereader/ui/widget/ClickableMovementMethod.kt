package me.ikirby.ithomereader.ui.widget

import android.text.Layout
import android.text.Selection
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

/**
 * A movement method that traverses links in the text buffer and fires clicks. Unlike
 * {@link LinkMovementMethod}, this will not consume touch events outside {@link ClickableSpan}s.
 *
 * used to fix TextView with links not passing event to parent if not clicking on a link
 * ref: https://stackoverflow.com/a/34200772
 */
class ClickableMovementMethod : BaseMovementMethod() {
    override fun canSelectArbitrarily(): Boolean {
        return false
    }

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout: Layout = widget.layout
            val line: Int = layout.getLineForVertical(y)
            val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                } else {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]))
                }
                return true
            } else {
                Selection.removeSelection(buffer)
            }
        }
        return false
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.removeSelection(text)
    }

    companion object {
        private var sInstance: ClickableMovementMethod? = null
        val instance: ClickableMovementMethod?
            get() {
                if (sInstance == null) {
                    sInstance = ClickableMovementMethod()
                }
                return sInstance
            }
    }
}