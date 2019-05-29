package me.ikirby.ithomereader.ui.util

import android.content.Context
import android.view.View
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu

object UiUtil {

    fun switchVisibility(list: View, placeholder: View, size: Int) {
        if (size > 0) {
            list.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
        } else {
            placeholder.visibility = View.VISIBLE
            list.visibility = View.GONE
        }
    }

    fun showBottomSheetMenu(context: Context, listener: BottomSheetMenu.BottomSheetMenuListener) {
        BottomSheetMenu.Builder(context, listener).show()
    }
}
