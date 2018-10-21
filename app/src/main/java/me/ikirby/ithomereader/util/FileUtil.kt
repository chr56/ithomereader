package me.ikirby.ithomereader.util

import me.ikirby.ithomereader.BaseApplication
import java.io.File

fun clearCache() {
    try {
        val dir = BaseApplication.instance.cacheDir
        if (dir != null && dir.isDirectory) {
            deleteDir(dir)
        }
    } catch (e: Exception) {
        e.printStackTrace()
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