package me.ikirby.ithomereader.entity.app.comment

import com.squareup.moshi.Json

data class CommentContentResponseContent(
    @Json(name = "commentlist") val commentContentList: List<CommentM>
)
