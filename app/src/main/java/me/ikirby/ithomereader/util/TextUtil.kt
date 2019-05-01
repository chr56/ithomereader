package me.ikirby.ithomereader.util

import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.SETTINGS_KEY_AUTO_ADD_WHITESPACE

fun addWhiteSpace(str: String): String {
    val autoAddWhiteSpace = BaseApplication.preferences.getBoolean(SETTINGS_KEY_AUTO_ADD_WHITESPACE, false)
    return if (autoAddWhiteSpace && str.isNotBlank()) {
        Pangu.spacingText(str)
    } else {
        str
    }
}
