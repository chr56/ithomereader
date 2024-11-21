package me.ikirby.ithomereader.ui.util

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat


object CompatibilityUtil {

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
        BundleCompat.getParcelable(this, key, T::class.java)

    inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? =
        BundleCompat.getParcelableArrayList(this, key, T::class.java)

    inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? =
        IntentCompat.getParcelableExtra(this, key, T::class.java)

    inline fun <reified T : Parcelable> Intent.parcelableArrayListExtra(key: String): ArrayList<T>? =
        IntentCompat.getParcelableArrayListExtra(this, key, T::class.java)

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    inline fun <reified T> Parcel.array(classLoader: ClassLoader?): Array<T>? =
        when {
            SDK_INT >= UPSIDE_DOWN_CAKE -> readArray(classLoader, T::class.java)
            else                        -> readArray(classLoader) as? Array<T>
        }
    @Suppress("DEPRECATION")
    inline fun <reified T> Parcel.sparseArray(classLoader: ClassLoader?): SparseArray<T>? =
        when {
            SDK_INT >= UPSIDE_DOWN_CAKE -> readSparseArray(classLoader, T::class.java)
            else                        -> readSparseArray(classLoader)
        }
}