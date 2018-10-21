package me.ikirby.ithomereader.util

import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import java.util.regex.Pattern

fun getCss(): String {
    return when {
        BaseApplication.isNightMode -> "<style>" + BaseApplication.instance.getString(R.string.base_style_night) +
                getFontSize() + "</style>"
        BaseApplication.isOStyleLight -> "<style>" + BaseApplication.instance.getString(R.string.base_style_white) +
                getFontSize() + "</style>"
        else -> "<style>" + BaseApplication.instance.getString(R.string.base_style) +
                getFontSize() + "</style>"
    }
}

private fun getFontSize(): String {
    var fontSize = "p{font-size: 16px}"
    when (BaseApplication.preferences.getString("font_size", "0")) {
        "-1" -> fontSize = "p{font-size: 15px}"
        "1" -> fontSize = "p{font-size: 17px}"
        "2" -> fontSize = "p{font-size: 18px}"
        "-2" -> fontSize = "p{font-size: 14px}"
    }
    return fontSize
}

fun getJs() = "<script>" + BaseApplication.instance.getString(R.string.base_js) + "</script>"

fun isUrlImgSrc(url: String): Boolean {
    val pattern = Pattern.compile("img.+\\..+\\.[a-zA-Z]")
    val matcher = pattern.matcher(url)
    return matcher.find()
}
