package me.ikirby.ithomereader.ui.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.Menu
import android.view.View
import androidx.annotation.ColorRes
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu

object UiUtil {

    fun setNightMode(activity: Activity, isNightMode: Boolean, isOStyleLight: Boolean) {
        if (!isNightMode) {
            val descriptionColorRes = if (isOStyleLight) {
                activity.setTheme(R.style.AppTheme_OStyle)
                R.color.colorActionBarBackground_white
            } else {
                activity.setTheme(R.style.AppTheme_Light)
                R.color.colorActionBarBackground
            }
            val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityManager.TaskDescription(
                    activity.getString(R.string.app_name),
                    R.mipmap.ic_launcher,
                    activity.getColor(descriptionColorRes)
                )
            } else {
                val icon = BitmapFactory.decodeResource(activity.resources, R.mipmap.ic_launcher)
                @Suppress("DEPRECATION")
                ActivityManager.TaskDescription(
                    activity.getString(R.string.app_name),
                    icon,
                    activity.getColor(descriptionColorRes)
                )
            }
            activity.setTaskDescription(description)
        } else {
            activity.setTheme(R.style.AppTheme)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val window = activity.window
            if (isNightMode) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            } else {
                if (isOStyleLight) {
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                    window.navigationBarColor = activity.getColor(R.color.navigation_bar_divider_light)
                }
            }
        }
    }

    fun setNightModeForDialogActivity(dialogActivity: Activity, isNightMode: Boolean, isOStyleLight: Boolean) {
        if (!isNightMode) {
            if (isOStyleLight) {
                dialogActivity.setTheme(R.style.AppTheme_CommonDialog_OStyle)
            } else {
                dialogActivity.setTheme(R.style.AppTheme_CommonDialog_Light)
            }
        } else {
            dialogActivity.setTheme(R.style.AppTheme_CommonDialog)
        }
    }

    fun tintMenuIcon(menu: Menu, @ColorRes color: Int) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val icon = item.icon
            if (icon != null) {
                icon.setTint(BaseApplication.instance.getColor(color))
                item.icon = icon
            }
        }
    }

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

    fun getToolBarColor(): Int {
        return if (!BaseApplication.isNightMode) {
            if (BaseApplication.isOStyleLight) {
                BaseApplication.instance.getColor(R.color.colorActionBarBackground_white)
            } else {
                BaseApplication.instance.getColor(R.color.colorActionBarBackground)
            }
        } else {
            BaseApplication.instance.getColor(R.color.colorActionBarBackground_night)
        }
    }

    fun getWindowBackgroundColorRes(): Int {
        return if (BaseApplication.isNightMode) {
            R.color.background_dark
        } else {
            R.color.background_light
        }
    }

    fun getAccentColorRes(): Int {
        return if (!BaseApplication.isNightMode) {
            if (BaseApplication.isOStyleLight) {
                R.color.colorPrimary
            } else {
                R.color.colorSecondary
            }
        } else {
            R.color.colorSecondary_night
        }
    }
}
