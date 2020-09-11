package me.ikirby.ithomereader.api.impl

import me.ikirby.ithomereader.api.CommentApi
import me.ikirby.ithomereader.entity.Comment
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.getMatchInt
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.regex.Pattern

object CommentApiImpl : CommentApi {
    private const val TAG = "CommentApiImpl"
    private const val BLOCKED_CONTENT = "***无法获取评论内容，可能因举报被屏蔽***"

    override fun getAllCommentsList(
        newsId: String,
        hash: String,
        page: Int,
        oldList: ArrayList<Comment>?,
        isLapin: Boolean
    ): List<Comment>? {
        try {
            val doc = ITHomeApi.getCommentsDoc(newsId, hash, page, isLapin)
            val list = mutableListOf<Comment>()
            val comments = doc.select(".entry")
            if (!comments.isEmpty()) {
                for (comment in comments) {
                    val nick = comment.select(".info .nick").text()
                    val floor = comment.select(".info .p_floor").text()
                    val posAndTime = trimPosAndTime(comment.select(".info .posandtime").text())

                    val contentElements = comment.select(".comm p")
                    val content: String
                    val modifyTime: String
                    if (contentElements.size > 0) {
                        content = getTextContent(contentElements[0])
                        modifyTime = comment.select(".comm p.modifytime").text()
                    } else {
                        content = BLOCKED_CONTENT
                        modifyTime = ""
                    }

                    // ithome doesn't provide device info now
                    val device = "" //comment.select(".info .mobile a").text()

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
                            againstCount,
                            modifyTime = modifyTime
                        )
                    )

                    val replies = comment.select(".reply li.gh")
                    if (!replies.isEmpty()) {
                        for (reply in replies) {
                            val reNick = reply.select(".nick a").text()
                            val reFloor = reply.select(".p_floor").text()
                            val rePosAndTime = trimPosAndTime(reply.select(".posandtime").text())

                            val reContentElements = reply.getElementsByTag("p")
                            val reContent: String
                            val reModifyTime: String
                            if (reContentElements.size > 0) {
                                reContent = getTextContent(reContentElements[0])
                                reModifyTime = reply.select(".modifytime").text()
                            } else {
                                reContent = BLOCKED_CONTENT
                                reModifyTime = ""
                            }

                            // ithome doesn't provide device info for now
                            val reDevice = "" //reply.select(".mobile a").text()

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
                                    reAgainstCount,
                                    modifyTime = reModifyTime
                                )
                            )
                        }
                        if (replies.size >= 5 && parentId != "0") {
                            val moreReplies = getMoreRepliesList(parentId, newsId)
                            if (moreReplies != null && moreReplies.isNotEmpty()) {
                                list.addAll(moreReplies)
                            }
                        }
                    }
                }
            }
            return removeDuplicate(removeDiscontinuousFloor(list), oldList)
        } catch (e: Exception) {
            Logger.e(TAG, "getAllCommentsList", e)
            return null
        }
    }

    override fun getHotCommentList(
        newsId: String,
        hash: String,
        page: Int,
        oldList: ArrayList<Comment>?,
        isLapin: Boolean
    ): List<Comment>? {
        try {
            val doc = ITHomeApi.getHotCommentsDoc(newsId, hash, page, isLapin)
            val list = mutableListOf<Comment>()
            val comments = doc.select(".entry")
            if (!comments.isEmpty()) {
                for (comment in comments) {
                    val nick = comment.select(".nick a").text()
                    val floor = comment.select(".p_floor").text()
                    val posAndTime = trimPosAndTime(comment.select(".posandtime").text())

                    val contentElements = comment.select("p")
                    val content: String
                    val modifyTime: String
                    if (contentElements.size > 0) {
                        content = getTextContent(contentElements[0])
                        modifyTime = comment.select(".modifytime").text()
                    } else {
                        content = BLOCKED_CONTENT
                        modifyTime = ""
                    }

                    // ithome doesn't provide device info now
                    val device = "" //comment.select(".mobile a").text()

                    val cid = comment.attr("cid") // actually parentId

                    val commentExpandArea = comment.select(".l .comm_reply")[0]
                    val expandCount = getMatchInt(commentExpandArea.select(".comment_co").text())

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
                            cid,
                            selfId,
                            supportCount,
                            againstCount,
                            expandCount = expandCount,
                            modifyTime = modifyTime
                        )
                    )
                }
            }
            return removeDuplicate(list, oldList)
        } catch (e: Exception) {
            Logger.e(TAG, "getHotCommentsList", e)
            return null
        }
    }

    override fun getMoreRepliesList(parentId: String, newsId: String): List<Comment>? {
        try {
            val doc = ITHomeApi.getMoreReplies(parentId, newsId)
            val list = mutableListOf<Comment>()
            val moreReplies = doc.select("li.gh")
            if (!moreReplies.isEmpty()) {
                for (reply in moreReplies) {
                    val reNick = reply.select(".nick a").text()
                    val reFloor = reply.select(".p_floor").text()
                    val rePosAndTime = trimPosAndTime(reply.select(".posandtime").text())

                    val reContentElements = reply.getElementsByTag("p")
                    val reContent: String
                    val reModifyTime: String
                    if (reContentElements.size > 0) {
                        reContent = getTextContent(reContentElements[0])
                        reModifyTime = reply.select(".modifytime").text()
                    } else {
                        reContent = BLOCKED_CONTENT
                        reModifyTime = ""
                    }

                    // ithome doesn't provide device info now
                    val reDevice = "" //reply.select(".mobile a").text()

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
                            reAgainstCount,
                            modifyTime = reModifyTime
                        )
                    )
                }
            }
            return list
        } catch (e: Exception) {
            Logger.e(TAG, "getMoreRepliesList", e)
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
            Logger.e(TAG, "postComment", e)
            null
        }
    }

    override fun commentVote(id: String, typeId: Int, isCancel: Boolean, cookie: String): String? {
        return try {
            ITHomeApi.commentVote(id, typeId, isCancel, cookie)
        } catch (e: Exception) {
            Logger.e(TAG, "commentVote", e)
            null
        }
    }

    override fun getCommentHash(id: String): String? {
        try {
            val doc = Jsoup.connect(ITHomeApi.IFCOMMENT_URL + id).timeout(5000).get()
            val pattern = Pattern.compile("var .+ = '(.{16})';")
            val matcher = pattern.matcher(doc.html())
            if (matcher.find()) {
                return matcher.group(1)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "getCommentHash", e)
        }
        return null
    }

    override fun getSingleComment(commentId: String, newsId: String): List<Comment>? {
        try {
            val doc = ITHomeApi.getSingleComment(commentId, newsId)
            val list = mutableListOf<Comment>()
            val comment = doc.select(".codiv")
            if (comment != null) {
                val nick = comment.select(".info .nick").text()
                val floor = comment.select(".info .p_floor").text()
                val posAndTime = trimPosAndTime(comment.select(".info .posandtime").text())
                val contentElement = comment.select(".comm p")[0]
                val content = getTextContent(contentElement)
                val modifyTime = comment.select(".comm p.modifytime").text()

                // ithome doesn't provide device info now
                val device = "" //comment.select(".info .mobile a").text()

                val commentVoteArea = comment.select(".r .comm_reply")[0]
//                val parentId = getCommentId(commentVoteArea.select("a.s").attr("id"))
                val supportCount = getMatchInt(commentVoteArea.select("a.s").text())
                val againstCount = getMatchInt(commentVoteArea.select("a.a").text())

                list.add(
                    Comment(
                        nick,
                        floor,
                        posAndTime,
                        content,
                        device,
                        commentId,
                        commentId,
                        supportCount,
                        againstCount,
                        modifyTime = modifyTime
                    )
                )

                val replies = comment.select(".reply li.gh")
                if (!replies.isEmpty()) {
                    for (reply in replies) {
                        val reNick = reply.select(".nick a").text()
                        val reFloor = reply.select(".p_floor").text()
                        val rePosAndTime = trimPosAndTime(reply.select(".posandtime").text())
                        val reContentElement = reply.getElementsByTag("p")[0]
                        val reContent = getTextContent(reContentElement)
                        val reModifyTime = reply.select(".modifytime").text()

                        // ithome doesn't provide device info for now
                        val reDevice = "" //reply.select(".mobile a").text()

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
                                commentId,
                                reSelfId,
                                reSupportCount,
                                reAgainstCount,
                                modifyTime = reModifyTime
                            )
                        )
                    }
                    if (replies.size >= 5) {
                        val moreReplies = getMoreRepliesList(commentId, newsId)
                        if (moreReplies != null && moreReplies.isNotEmpty()) {
                            list.addAll(moreReplies)
                        }
                    }
                }
            }
            return list
        } catch (e: Exception) {
            Logger.e(TAG, "getSingleComment", e)
        }
        return null
    }

    private fun getCommentId(strContainingId: String): String {
        val idMatch = Pattern.compile("\\d+")
        val matcher = idMatch.matcher(strContainingId)
        return if (matcher.find()) {
            matcher.group()
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

    private fun getTextContent(element: Element): String {
        val result = StringBuilder()
        element.childNodes().forEach { child ->
            if (child is Element && child.tagName() == "img") {
                result.append("[${child.attr("title")}]")
            } else {
                result.append(child.outerHtml())
            }
        }
        return result.toString()
    }
}