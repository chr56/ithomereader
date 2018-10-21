package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateInfo(val versionCode: Int,
                      val version: String,
                      val log: String,
                      val url: String) : Parcelable
