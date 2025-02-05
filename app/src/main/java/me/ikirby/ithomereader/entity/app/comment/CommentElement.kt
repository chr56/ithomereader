package me.ikirby.ithomereader.entity.app.comment


import com.squareup.moshi.Json

data class CommentElement(
    @Json(name = "type") val type: Int,
    @Json(name = "content") val content: String?,
    @Json(name = "src") val src: String?,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "flag") val flag: Int,
)