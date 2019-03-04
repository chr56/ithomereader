package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArticleGrade(
    val score: String,
    val trashCount: String,
    val sosoCount: String,
    val greatCount: String
) : Parcelable
