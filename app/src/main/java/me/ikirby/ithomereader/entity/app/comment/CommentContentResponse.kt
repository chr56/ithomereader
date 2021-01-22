package me.ikirby.ithomereader.entity.app.comment

import com.squareup.moshi.Json

data class CommentContentResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "content") val content: CommentContentResponseContent
)
