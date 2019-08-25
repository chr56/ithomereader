package me.ikirby.ithomereader.api

import me.ikirby.ithomereader.entity.Comment

interface CommentApi {

    fun getAllCommentsList(
        id: String,
        hash: String,
        page: Int,
        oldList: ArrayList<Comment>?,
        isLapin: Boolean
    ): List<Comment>?

    fun getHotCommentList(
        id: String,
        hash: String,
        page: Int,
        oldList: ArrayList<Comment>?,
        isLapin: Boolean
    ): List<Comment>?

    fun getMoreRepliesList(parentId: String): List<Comment>?

    fun postComment(id: String, parentId: String?, selfId: String?, commentContent: String, cookie: String): String?

    fun commentVote(id: String, typeId: Int, isCancel: Boolean, cookie: String): String?

    fun getCommentHash(id: String): String?

    fun getSingleComment(commentId: String, newsId: String): List<Comment>?
}