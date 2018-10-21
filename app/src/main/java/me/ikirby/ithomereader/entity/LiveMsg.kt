package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LiveMsg(val postTime: String?,
                   val content: String,
                   val type: Int) : Parcelable
