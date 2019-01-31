package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FullArticle(val newsId: String,
                       val title: String,
                       val time: String,
                       val content: String) : Parcelable {
    constructor() : this("", "", "", "")
}