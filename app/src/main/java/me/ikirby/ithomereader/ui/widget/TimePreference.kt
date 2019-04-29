package me.ikirby.ithomereader.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference

class TimePreference : DialogPreference {
    var hour = 0
    var minute = 0

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)

    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)
        val value = if (defaultValue == null) {
            getPersistedString("00:00")
        } else {
            getPersistedString(defaultValue.toString())
        }
        setSummaryTime(value)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index) ?: "00:00"
    }

    fun persistStringValue(value: String) {
        persistString(value)
        setSummaryTime(value)
    }

    private fun setSummaryTime(value: String) {
        summary = value
        hour = parseHour(value)
        minute = parseMinute(value)
    }

    private fun parseHour(time: String): Int = Integer.parseInt(time.split(":")[0])

    private fun parseMinute(time: String): Int = Integer.parseInt(time.split(":")[1])

    companion object {
        fun timeToString(hour: Int, minute: Int) = String.format("%02d:%02d", hour, minute)
    }

}