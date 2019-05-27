package me.ikirby.ithomereader.util

import android.webkit.WebView
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.SETTINGS_KEY_AUTO_ADD_WHITESPACE
import me.ikirby.ithomereader.SETTINGS_KEY_FONT_SIZE
import java.util.regex.Pattern

fun getCss(): String {
    return when {
        BaseApplication.isNightMode -> "<link rel='stylesheet' href='file:///android_asset/css/base_style_night.css'>"
        BaseApplication.isWhiteTheme -> "<link rel='stylesheet' href='file:///android_asset/css/base_style_white.css'>"
        else -> "<link rel='stylesheet' href='file:///android_asset/css/base_style.css'>"
    }
}

private fun getFontSize(): String {
    var fontSize = 16
    when (BaseApplication.preferences.getString(SETTINGS_KEY_FONT_SIZE, "0")) {
        "-1" -> fontSize = 15
        "1" -> fontSize = 17
        "2" -> fontSize = 18
        "-2" -> fontSize = 14
    }
    return "<style>p,table{font-size:${fontSize}px}</style>"
}

fun getJs(): String {
    var script = "<script src='file:///android_asset/js/base.js'></script>"
    if (BaseApplication.preferences.getBoolean(SETTINGS_KEY_AUTO_ADD_WHITESPACE, false)) {
        script += "<script src='file:///android_asset/js/pangu.min.js'></script>" +
                "<script>pangu.spacingElementById('header');pangu.spacingElementById('paragraph')</script>"
    }
    return script
}


fun getHead() = "<html lang='zh-CN'><head>${getCss()}${getFontSize()}</head><body>"

fun getFooter() = "${getJs()}</body></html>"

fun isUrlImgSrc(url: String): Boolean {
    val pattern = Pattern.compile("img.+\\..+\\.[a-zA-Z]")
    val matcher = pattern.matcher(url)
    return matcher.find()
}

fun getScrollProgress(webView: WebView): Float {
    val positionTop = webView.top.toFloat()
    val contentHeight = webView.contentHeight.toFloat()
    val currentPosition = webView.scrollY.toFloat()
    return (currentPosition - positionTop) / contentHeight
}
