package me.ikirby.ithomereader.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class XViewPager : ViewPager {
    private var swipeDisabled = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (swipeDisabled) false else super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (swipeDisabled) false else super.onTouchEvent(ev)
    }

    fun setSwipeDisabled(disabled: Boolean) {
        this.swipeDisabled = disabled
    }

//    fun isSwipeDisabled(): Boolean {
//        return swipeDisabled
//    }
}