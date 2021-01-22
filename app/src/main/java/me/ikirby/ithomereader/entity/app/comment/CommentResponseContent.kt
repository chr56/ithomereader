package me.ikirby.ithomereader.entity.app.comment

import com.squareup.moshi.Json

data class CommentResponseContent(
    @Json(name = "hlist") val hlist: List<CommentM>,
    @Json(name = "clist") val clist: List<CommentM>
) {
    fun getHotList(): List<Comment> {
        val list = mutableListOf<Comment>()
        hlist.forEach {
            list.add(it.comment)
        }
        return list
    }

    fun getAllList(): List<Comment> {
        val list = mutableListOf<Comment>()
        clist.forEach {
            list.add(it.comment)
            it.replies?.forEach { reply ->
                reply.isReply = true
                list.add(reply)
            }
        }
        return list
    }
}
