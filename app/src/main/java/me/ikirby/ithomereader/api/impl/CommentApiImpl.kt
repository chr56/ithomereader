package me.ikirby.ithomereader.api.impl

import me.ikirby.ithomereader.api.CommentApi
import me.ikirby.ithomereader.entity.Comment
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.getMatchInt
import org.jsoup.Jsoup
import java.util.regex.Pattern

object CommentApiImpl : CommentApi {
    private val tag = javaClass.simpleName

    override fun getAllCommentsList(
        id: String,
        hash: String,
        page: Int,
        oldList: ArrayList<Comment>?,
        isLapin: Boolean
    ): List<Comment>? {
        try {
            val doc = ITHomeApi.getCommentsDoc(id, hash, page, isLapin)
            val list = mutableListOf<Comment>()
            val comments = doc.select(".entry")
            if (!comments.isEmpty()) {
                for (comment in comments) {
                    val nick = comment.select(".info .nick").text()
                    val floor = comment.select(".info .p_floor").text()
                    val posAndTime = trimPosAndTime(comment.select(".info .posandtime").text())
                    val content = comment.select(".comm p").html().replace("<br>", "\n")
                        .replace("<span>", "").replace("</span>", "")
                    val device = comment.select(".info .mobile a").text()

                    val commentVoteArea = comment.select(".comm_reply")[0]
                    val parentId = getCommentId(commentVoteArea.select("a.s").attr("id"))
                    val supportCount = getMatchInt(commentVoteArea.select("a.s").text())
                    val againstCount = getMatchInt(commentVoteArea.select("a.a").text())

                    list.add(
                        Comment(
                            nick,
                            floor,
                            posAndTime,
                            content,
                            device,
                            parentId,
                            parentId,
                            supportCount,
                            againstCount
                        )
                    )

                    val replies = comment.select(".reply li.gh")
                    if (!replies.isEmpty()) {
                        for (reply in replies) {
                            val reNick = reply.select(".nick a").text()
                            val reFloor = reply.select(".p_floor").text()
                            val rePosAndTime = trimPosAndTime(reply.select(".posandtime").text())
                            val reContent = reply.getElementsByTag("p").html().replace("<br>", "\n")
                                .replace("<span>", "").replace("</span>", "")
                            val reDevice = reply.select(".mobile a").text()

                            val replyVoteArea = reply.select(".comm_reply")[0]
                            val reSelfId = getCommentId(replyVoteArea.select("a.s").attr("id"))
                            val reSupportCount = getMatchInt(replyVoteArea.select("a.s").text())
                            val reAgainstCount = getMatchInt(replyVoteArea.select("a.a").text())

                            list.add(
                                Comment(
                                    reNick,
                                    reFloor,
                                    rePosAndTime,
                                    reContent,
                                    reDevice,
                                    parentId,
                                    reSelfId,
                                    reSupportCount,
                                    reAgainstCount
                                )
                            )
                        }
                        if (replies.size >= 5 && parentId != "0") {
                            val moreReplies = getMoreRepliesList(parentId)
                            if (moreReplies != null && moreReplies.isNotEmpty()) {
                                list.addAll(moreReplies)
                            }
                        }
                    }
                }
            }
            return removeDuplicate(removeDiscontinuousFloor(list), oldList)
        } catch (e: Exception) {
            Logger.e(tag, "getAllCommentsList", e)
            return null
        }
    }

    override fun getHotCommentList(
        id: String,
        hash: String,
        page: Int,
        oldList: ArrayList<Comment>?,
        isLapin: Boolean
    ): List<Comment>? {
        try {
            val doc = ITHomeApi.getHotCommentsDoc(id, hash, page, isLapin)
            val list = mutableListOf<Comment>()
            val comments = doc.select(".entry")
            if (!comments.isEmpty()) {
                for (comment in comments) {
                    val nick = comment.select(".nick a").text()
                    val floor = comment.select(".p_floor").text()
                    val posAndTime = trimPosAndTime(comment.select(".posandtime").text())
                    val content = comment.getElementsByTag("p").html().replace("<br>", "\n")
                        .replace("<span>", "").replace("</span>", "")
                    val device = comment.select(".mobile a").text()

                    val commentVoteArea = comment.select(".r .comm_reply")[0]
                    val selfId = getCommentId(commentVoteArea.select("a.s").attr("id"))
                    val supportCount = getMatchInt(commentVoteArea.select("a.s").text())
                    val againstCount = getMatchInt(commentVoteArea.select("a.a").text())

                    list.add(
                        Comment(
                            nick,
                            floor,
                            posAndTime,
                            content,
                            device,
                            null,
                            selfId,
                            supportCount,
                            againstCount
                        )
                    )
                }
            }
            return removeDuplicate(list, oldList)
        } catch (e: Exception) {
            Logger.e(tag, "getHotCommentsList", e)
            return null
        }
    }

    override fun getMoreRepliesList(parentId: String): List<Comment>? {
        try {
            val doc = ITHomeApi.getMoreReplies(parentId)
            val list = mutableListOf<Comment>()
            val moreReplies = doc.select("li.gh")
            if (!moreReplies.isEmpty()) {
                for (reply in moreReplies) {
                    val reNick = reply.select(".nick a").text()
                    val reFloor = reply.select(".p_floor").text()
                    val rePosAndTime = trimPosAndTime(reply.select(".posandtime").text())
                    val reContent = reply.getElementsByTag("p").html().replace("<br>", "\n")
                        .replace("<span>", "").replace("</span>", "")
                    val reDevice = reply.select(".mobile a").text()

                    val replyVoteArea = reply.select(".comm_reply")[0]
                    val reSelfId = getCommentId(replyVoteArea.select("a.s").attr("id"))
                    val reSupportCount = getMatchInt(replyVoteArea.select("a.s").text())
                    val reAgainstCount = getMatchInt(replyVoteArea.select("a.a").text())

                    list.add(
                        Comment(
                            reNick,
                            reFloor,
                            rePosAndTime,
                            reContent,
                            reDevice,
                            parentId,
                            reSelfId,
                            reSupportCount,
                            reAgainstCount
                        )
                    )
                }
            }
            return list
        } catch (e: Exception) {
            Logger.e(tag, "getMoreRepliesList", e)
            return null
        }
    }

    override fun postComment(
        id: String,
        parentId: String?,
        selfId: String?,
        commentContent: String,
        cookie: String
    ): String? {
        return try {
            ITHomeApi.postComment(id, parentId, selfId, commentContent, cookie)
        } catch (e: Exception) {
            Logger.e(tag, "postComment", e)
            null
        }
    }

    override fun commentVote(id: String, typeId: Int, isCancel: Boolean, cookie: String): String? {
        return try {
            ITHomeApi.commentVote(id, typeId, isCancel, cookie)
        } catch (e: Exception) {
            Logger.e(tag, "commentVote", e)
            null
        }
    }

    override fun getCommentHash(id: String): String? {
        try {
            val doc = Jsoup.connect(ITHomeApi.IFCOMMENT_URL + id).timeout(5000).get()
            val pattern = Pattern.compile("var commentpage = '(.+)';")
            val matcher = pattern.matcher(doc.html())
            if (matcher.find()) {
                return matcher.group(1)
            }
        } catch (e: Exception) {
            Logger.e(tag, "getCommentHash", e)
        }
        return null
    }

    private fun getCommentId(strContainingId: String): String {
        val idMatch = Pattern.compile("\\d+")
        val matcher = idMatch.matcher(strContainingId)
        return if (matcher.find()) {
            matcher.group(0)
        } else "0"
    }


    private fun removeDuplicate(newList: MutableList<Comment>, oldList: List<Comment>?): List<Comment> {
        if (oldList != null && newList.isNotEmpty()) {
            var needRemove = true
            for (i in oldList.indices.reversed()) {
                val oldItem = oldList[i]
                for (newItem in newList) {
                    if (newItem.selfId == oldItem.selfId) {
                        needRemove = true
                        newList.remove(newItem)
                        break
                    } else {
                        needRemove = false
                    }
                }
                if (!needRemove) {
                    break
                }
            }
        }
        return newList
    }

    private fun removeDiscontinuousFloor(list: MutableList<Comment>): MutableList<Comment> {
        if (list.size > 0) {
            val itemsToRemove = mutableListOf<Comment>()
            for (item in list) {
                if (item.floor.contains("#")) {
                    itemsToRemove.add(item)
                } else {
                    break
                }
            }
            list.removeAll(itemsToRemove)
        }
        return list
    }

    private fun trimPosAndTime(original: String): String {
        return original.replace("IT之家", "").replace("网友", "")
    }
}