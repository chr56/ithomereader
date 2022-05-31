package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FullArticle(
    val newsId: String,
    val newsIdHash: String,
    val title: String,
    val time: String,
    val content: String
) : Parcelable {
    constructor() : this("", "", "", "", "")
}