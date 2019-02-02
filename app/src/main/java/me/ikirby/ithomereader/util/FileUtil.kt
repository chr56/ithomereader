package me.ikirby.ithomereader.util

import android.os.Environment
import android.webkit.URLUtil
import me.ikirby.ithomereader.BaseApplication
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

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
        for (child in children) {
            if (!deleteDir(File(dir, child))) {
                return false
            }
        }
    }
    return dir.delete()
}

fun getFullPath(url: String): String {
    val path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
            + "/ITHome/" + URLUtil.guessFileName(url, "", "image/*"))
    return if (path.contains("@")) {
        path.substring(0, path.indexOf("@"))
    } else {
        path
    }
}

@Throws(IOException::class)
fun writeFile(fileName: String, file: File) {
    val inputStream = FileInputStream(file)
    val outputStream = FileOutputStream(fileName)
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