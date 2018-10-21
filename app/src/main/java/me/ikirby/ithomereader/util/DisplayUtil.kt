package me.ikirby.ithomereader.util

import android.content.res.Resources
import android.util.TypedValue


fun convertDpToPixel(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
}