package me.ikirby.ithomereader.entity.app.comment

import com.squareup.moshi.Json
import java.util.*

data class Comment(
    @Json(name = "Ci") val cid: Long,
    @Json(name = "C") val content: String,
    @Json(name = "N") val nickname: String,
    @Json(name = "Ui") val uid: Long,
    @Json(name = "Y") val region: String,
    @Json(name = "T") val time: Date,
    @Json(name = "S") val support: Int,
    @Json(name = "A") val against: Int,
    @Json(name = "Ta") val device: String,
    @Json(name = "SF") val floor: String
) {
    var isReply = false
}
