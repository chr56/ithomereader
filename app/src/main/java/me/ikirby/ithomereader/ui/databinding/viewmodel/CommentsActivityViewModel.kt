package me.ikirby.ithomereader.ui.databinding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.ikirby.ithomereader.APP_VER
import me.ikirby.ithomereader.clientapi.Api
import me.ikirby.ithomereader.entity.app.comment.Comment
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.encryptString

class CommentsActivityViewModel : ViewModel() {
    val newsId: MutableLiveData<String> = MutableLiveData(null)
    val newsIdEncrypted: MutableLiveData<String> = MutableLiveData(null)
    val newsTitle: MutableLiveData<String> = MutableLiveData("")
    val newsUrl: MutableLiveData<String> = MutableLiveData("")

    val hotLoading = MutableLiveData(false)
    val allLoading = MutableLiveData(false)

    val hotList = MutableLiveData(listOf<Comment>())
    val allList = MutableLiveData(listOf<Comment>())

    val allCommentsLoaded = MutableLiveData(false)

    fun loadComment(hot: Boolean, all: Boolean, refresh: Boolean) {
        var cid: Long? = null
        if (hot && hotLoading.value == false) {
            hotLoading.value = true
        }
        if (all && allLoading.value == false) {
            allLoading.value = true
            if (!refresh && !allList.value.isNullOrEmpty()) {
                val last = allList.value!!.findLast { !it.isReply }
                cid = last?.cid
            }
        }
        viewModelScope.launch {
            runCatching {
                Api.api.commentApi.getNewsComment(newsIdEncrypted.value!!, cid, APP_VER)
            }.onSuccess {
                if (hot) {
                    hotList.value = it.content.getHotList()
                }
                if (all) {
                    val newList = it.content.getAllList()
                    if (newList.isEmpty()) {
                        allCommentsLoaded.value = true
                    }
                    if (refresh) {
                        allCommentsLoaded.value = false
                        allList.value = newList
                    } else {
                        val list = allList.value!!.toMutableList()
                        list.addAll(newList)
                        allList.value = list
                    }
                }
            }.onFailure {
                Logger.e(
                    "CommentsActivityViewModel",
                    "error getting comment, hot = $hot, all = $all", it
                )
            }
            if (hot) {
                hotLoading.value = false
            }
            if (all) {
                allLoading.value = false
            }
        }
    }

    fun expandComment(position: Int) {
        val commentId = encryptString(hotList.value!![position].cid.toString()) ?: return
        hotLoading.value = true
        viewModelScope.launch {
            runCatching {
                Api.api.commentApi.getCommentContent(commentId)
            }.onSuccess {
                val list = hotList.value!!.toMutableList()
                val commentContent = it.content.commentContentList[0]

                list.removeAt(position)
                list.add(position, commentContent.comment)

                val replyPosition = position + 1
                commentContent.replies?.forEachIndexed { index, comment  ->
                    comment.isReply = true
                    list.add(replyPosition + index, comment)
                }
                hotList.value = list
            }.onFailure {
                Logger.e(
                    "CommentsActivityViewModel",
                    "error getting expand comment content", it
                )
            }
            hotLoading.value = false
        }
    }
}
