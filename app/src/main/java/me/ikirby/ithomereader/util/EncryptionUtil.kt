package me.ikirby.ithomereader.util

import okhttp3.internal.and
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

private const val ENCRYPTION_KEY = "(#i@x*l%"

@Throws(Exception::class)
private fun encrypt(str: String, key: String, bool: Boolean): String {
    val secretKeySpec = SecretKeySpec(key.toByteArray(), "DES")
    val cipher = Cipher.getInstance("DES/ECB/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    var length = str.length
    var times = 8 - length
    if (length >= 8) {
        length %= 8
        if (length == 0) {
            times = 0
        }
    }
    val sb = StringBuilder(str)
    for (i in 0 until times) {
        sb.append("\u0000")
    }
    var bytes = sb.toString().toByteArray()
    if (bool && bytes.size % 8 != 0) {
        val arr = ByteArray((bytes.size + 8) - (bytes.size % 8))
        System.arraycopy(bytes, 0, arr, 0, bytes.size)
        bytes = arr
    }
    return build(cipher.doFinal(bytes))
}

private fun build(arr: ByteArray): String {
    val sb = StringBuilder()
    arr.forEach {
        val hexString = Integer.toHexString(it and 255)
        if (hexString.length == 1) {
            sb.append("0")
        }
        sb.append(hexString)
    }
    return sb.toString()
}

fun encryptString(str: String): String? {
    return runCatching {
        encrypt(str, ENCRYPTION_KEY, false)
    }.getOrNull()
}

fun encryptInt(int: Int): String? {
    return runCatching {
        encryptString(int.toString())
    }.getOrNull()
}
