package me.ikirby.ithomereader.entity.app.comment

import com.squareup.moshi.Json

data class CommentM(
    @Json(name = "M") val comment: Comment
)
