package me.ikirby.ithomereader.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class XRecyclerView : RecyclerView {

    private var isLinearLayoutManager: Boolean = false
    private var onBottomReachedListener: OnBottomReachedListener? = null
    private var isAllContentLoaded = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        isLinearLayoutManager = layout is LinearLayoutManager
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        if (!isAllContentLoaded
            && isLinearLayoutManager
            && dy > 0
            && onBottomReachedListener != null
        ) {
            val layoutManager = layoutManager as LinearLayoutManager?
            if (layoutManager != null) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    onBottomReachedListener?.onBottomReached()
                }
            }
        }
    }

    fun setOnBottomReachedListener(onBottomReachedListener: OnBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener
    }

    fun setAllContentLoaded(isAllContentLoaded: Boolean) {
        this.isAllContentLoaded = isAllContentLoaded
    }
}
