package me.ikirby.ithomereader.util

import android.webkit.URLUtil
import me.ikirby.ithomereader.BaseApplication
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

fun clearCache() {
    try {
        val dir = BaseApplication.instance.cacheDir
        if (dir != null && dir.isDirectory) {
            deleteDir(dir)
        }
    } catch (e: Exception) {
        Logger.e("FileUtil", "clearCache", e)
    }

}

private fun deleteDir(dir: File): Boolean {
    if (dir.isDirectory) {
        val children = dir.list()
        if (children != null) {
            for (child in children) {
                if (!deleteDir(File(dir, child))) {
                    return false
                }
            }
        }
    }
    return dir.delete()
}

fun getFileName(url: String): String {
    val fileName = URLUtil.guessFileName(url, "", "image/*")
    return if (fileName.contains("@")) {
        fileName.substring(0, fileName.indexOf("@"))
    } else {
        fileName
    }
}

fun getImageMimeType(fileName: String): String {
    return when (fileName.substringAfterLast(".")) {
        "jpg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "jpeg" -> "image/jpeg"
        else -> "image/*"
    }
}

@Throws(IOException::class)
fun writeFile(outputStream: OutputStream, file: File) {
    val inputStream = FileInputStream(file)
    val buffer = ByteArray(1024)
    var length: Int
    while (true) {
        length = inputStream.read(buffer)
        if (length > 0) {
            outputStream.write(buffer, 0, length)
        } else {
            break
        }
    }
    inputStream.close()
    outputStream.close()
}