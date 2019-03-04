package me.ikirby.ithomereader.ui.base

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.SWIPE_GESTURE_DISTANCE
import me.ikirby.ithomereader.ui.util.UiUtil
import kotlin.coroutines.CoroutineContext

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), CoroutineScope {
    protected lateinit var preferences: SharedPreferences

    private lateinit var mGestureDetector: GestureDetector
    protected var isGestureEnabled: Boolean = false

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = BaseApplication.preferences
        isGestureEnabled = BaseApplication.isGestureEnabled
        UiUtil.setNightMode(this, BaseApplication.isNightMode, BaseApplication.isOStyleLight)
        super.onCreate(savedInstanceState)
        mGestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent, e2: MotionEvent, v: Float, v1: Float): Boolean {
                if (Math.abs(e1.rawY - e2.rawY) < 75) {
                    if (e1.rawX - e2.rawX > SWIPE_GESTURE_DISTANCE) {
                        return swipeLeft()
                    } else if (e2.rawX - e1.rawX > SWIPE_GESTURE_DISTANCE) {
                        return swipeRight()
                    }
                }
                return false
            }
        })

        initView()
    }

    protected open fun initView() {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!BaseApplication.isNightMode && BaseApplication.isOStyleLight) {
            UiUtil.tintMenuIcon(menu, R.color.colorPrimary)
        } else {
            UiUtil.tintMenuIcon(menu, android.R.color.white)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isGestureEnabled && BaseApplication.isGestureEnabled) {
            return if (mGestureDetector.onTouchEvent(ev)) {
                true
            } else {
                super.dispatchTouchEvent(ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    protected fun enableBackBtn() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        coroutineContext.cancelChildren()
        super.onDestroy()
    }

    protected open fun swipeLeft(): Boolean {
        return false
    }

    protected open fun swipeRight(): Boolean {
        finish()
        return true
    }

    protected fun setTitleCustom(title: String?) {
        if (!BaseApplication.isNightMode && BaseApplication.isOStyleLight) {
            val text = SpannableString(title)
            text.setSpan(
                ForegroundColorSpan(getColor(R.color.colorPrimary)),
                0,
                text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setTitle(text)
        } else {
            setTitle(title)
        }
    }
}
