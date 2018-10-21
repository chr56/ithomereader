package me.ikirby.ithomereader.api

import kotlinx.coroutines.experimental.Deferred
import me.ikirby.ithomereader.entity.Comment

interface CommentApi {

    fun getAllCommentsList(id: String, hash: String, page: Int,
                           oldList: ArrayList<Comment>?,
                           isLapin: Boolean): Deferred<List<Comment>?>

    fun getHotCommentList(id: String, hash: String, page: Int,
                          oldList: ArrayList<Comment>?,
                          isLapin: Boolean): Deferred<List<Comment>?>

    fun getMoreRepliesList(parentId: String): List<Comment>?

    fun postComment(id: String, parentId: String?, selfId: String?,
                    commentContent: String, cookie: String): Deferred<String?>

    fun commentVote(id: String, typeId: Int,
                    isCancel: Boolean, cookie: String): Deferred<String?>

    fun getCommentHash(id: String): Deferred<String?>
}