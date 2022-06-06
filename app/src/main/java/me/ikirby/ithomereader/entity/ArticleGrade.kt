package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArticleGrade(
    val score: String,
    val trashCount: String,
    val greatCount: String
) : Parcelable
