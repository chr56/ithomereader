package me.ikirby.ithomereader.util

import java.util.*
import java.util.regex.Pattern

fun getMatchInt(s: String): Int {
    val pattern = Pattern.compile("(?<=[/(])(\\d+)")
    val matcher = pattern.matcher(s)
    var result = ""
    return try {
        while (matcher.find()) {
            val group = matcher.group(0)
            if (group != "0") {
                result += group
            }
        }
        if (result.isNotEmpty()) {
            Integer.parseInt(result)
        } else {
            0
        }
    } catch (e: Exception) {
        Logger.e("ParseUtil", "getMatchInt", e)
        0
    }
}

fun canHandleURL(url: String): Boolean {
    return url.contains("www.ithome.com/html")
            || url.contains("www.ithome.com/0/")
            || url.contains("m.ithome.com/html")
            || url.contains("live.ithome.com")
            || url.contains("lapin.ithome.com/html")
}

/**
 * convert mobile url to desktop url
 *
 * www.ithome.com/0/???/???.htm
 * m.ithome.com/html/??????.htm
 */
fun convertUrl(url: String): String? =
    try {
        regex.replace(url) {
            "https://www.ithome.com/0/${it.groupValues[1]}/${it.groupValues[2]}.htm"
        }
    } catch (e: Exception) {
        Logger.e("Parser", "Failed to parse $url", e)
        null
    }

val regex by lazy(LazyThreadSafetyMode.PUBLICATION) { Regex("""\D*/(\d\d\d)(\d*)\.htm""") }

fun shouldEnableNightMode(startTime: String?, endTime: String?): Boolean {
    if (startTime == null || endTime == null) return false

    val startValues = startTime.split(":")
    val start = Calendar.getInstance()
    start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startValues[0]))
    start.set(Calendar.MINUTE, Integer.parseInt(startValues[1]))
    start.set(Calendar.SECOND, 0)

    val endValues = endTime.split(":")
    val end = Calendar.getInstance()
    end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endValues[0]))
    end.set(Calendar.MINUTE, Integer.parseInt(endValues[1]))
    end.set(Calendar.SECOND, 0)

    val current = Calendar.getInstance()

    return when {
        startValues[0] > endValues[0] -> !(current.before(start) && current.after(end))
        startValues[0] == endValues[0] && startValues[1] > endValues[1] -> !(current.before(start) && current.after(end))
        else -> current.before(end) && current.after(start)
    }
}
