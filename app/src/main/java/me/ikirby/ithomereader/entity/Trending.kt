package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trending(
    val rank: String? = null,
    val title: String,
    val url: String?,
    val desc: String? = null,
    val thumb: String? = null
) : Parcelable
