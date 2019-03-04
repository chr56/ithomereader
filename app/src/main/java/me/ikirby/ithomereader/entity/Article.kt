package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article(
    val title: String,
    val date: String?,
    val url: String,
    val thumb: String?,
    val description: String?,
    val isAd: Boolean = false
) : Parcelable
