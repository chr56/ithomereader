package me.ikirby.ithomereader.util

import java.util.regex.Pattern

fun getMatchInt(s: String): Int {
    val pattern = Pattern.compile("(?<=[/(])(\\d+)")
    val matcher = pattern.matcher(s)
    var result: String
    try {
        if (matcher.find()) {
            result = matcher.group(0)
        } else {
            return 0
        }
        if (result.length == 1) {
            result = matcher.group(1) + matcher.group(2)
        }
        return Integer.parseInt(result)
    } catch (e: Exception) {
        Logger.e("ParseUtil", "getMatchInt", e)
        return 0
    }
}

fun canHandleURL(url: String): Boolean {
    return url.contains("www.ithome.com/html")
            || url.contains("www.ithome.com/0/")
            || url.contains("live.ithome.com")
            || url.contains("m.ithome.com/html")
            || url.contains("lapin.ithome.com/html")
}
