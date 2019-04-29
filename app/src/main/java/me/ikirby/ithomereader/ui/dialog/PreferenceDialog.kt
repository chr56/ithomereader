package me.ikirby.ithomereader.ui.dialog

import android.content.Context
import android.view.View
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.ui.widget.TimePreference

fun showListPreferenceDialog(context: Context, preference: ListPreference) {
    MaterialAlertDialogBuilder(context)
        .setTitle(preference.title)
        .setItems(preference.entries) { _, which ->
            preference.value = preference.entryValues[which].toString()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

fun showTimePreferenceDialog(context: Context, preference: TimePreference) {
    val timePickerViewId = View.generateViewId()
    MaterialAlertDialogBuilder(context)
        .setView(TimePicker(context).apply {
            id = timePickerViewId
            hour = preference.hour
            minute = preference.minute
            setIs24HourView(true)
        })
        .setPositiveButton(R.string.ok) { dialogInterface, _ ->
            val timePicker = (dialogInterface as AlertDialog).findViewById<TimePicker>(timePickerViewId)!!
            preference.persistStringValue(TimePreference.timeToString(timePicker.hour, timePicker.minute))
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
