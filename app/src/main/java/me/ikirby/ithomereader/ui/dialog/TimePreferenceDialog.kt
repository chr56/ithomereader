package me.ikirby.ithomereader.ui.dialog

import android.content.Context
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import me.ikirby.ithomereader.ui.widget.TimePreference

class TimePreferenceDialog : PreferenceDialogFragmentCompat() {

    private lateinit var timePicker: TimePicker

    override fun onCreateDialogView(context: Context?): View {
        timePicker = TimePicker(context)
        return timePicker
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        timePicker.setIs24HourView(true)

        val timePreference = preference as TimePreference
        timePicker.hour = timePreference.hour
        timePicker.minute = timePreference.minute
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val timePreference = preference as TimePreference
            timePreference.hour = timePicker.hour
            timePreference.minute = timePicker.minute

            val value = TimePreference.timeToString(timePreference.hour, timePreference.minute)
            if (timePreference.callChangeListener(value)) {
                timePreference.persistStringValue(value)
            }
        }
    }

}