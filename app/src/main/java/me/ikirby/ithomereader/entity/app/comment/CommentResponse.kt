package me.ikirby.ithomereader.entity.app.comment

import com.squareup.moshi.Json

data class CommentResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "content") val content: CommentResponseContent
)
