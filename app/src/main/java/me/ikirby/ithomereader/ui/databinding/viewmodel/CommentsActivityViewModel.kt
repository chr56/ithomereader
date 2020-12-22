package me.ikirby.ithomereader.ui.databinding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.ikirby.ithomereader.clientapi.Api
import me.ikirby.ithomereader.entity.app.comment.Comment
import me.ikirby.ithomereader.util.Logger

class CommentsActivityViewModel : ViewModel() {
    val newsId: MutableLiveData<String> = MutableLiveData(null)
    val newsIdEncrypted: MutableLiveData<String> = MutableLiveData(null)
    val newsTitle: MutableLiveData<String> = MutableLiveData("")
    val newsUrl: MutableLiveData<String> = MutableLiveData("")

    val hotLoading = MutableLiveData(false)
    val allLoading = MutableLiveData(false)

    val hotList = MutableLiveData(listOf<Comment>())
    val allList = MutableLiveData(listOf<Comment>())

    fun loadComment(hot: Boolean, all: Boolean, refresh: Boolean) {
        var cid: Long? = null
        if (all && !refresh) {
            cid = allList.value!!.last().cid
        }
        if (hot) {
            hotLoading.value = true
        }
        if (all) {
            allLoading.value = true
        }
        viewModelScope.launch {
            runCatching {
                Api.api.commentApi.getNewsComment(newsIdEncrypted.value!!, cid, "760")
            }.onSuccess {
                if (hot) {
                    hotList.value = it.content.getHotList()
                }
                if (all) {
                    if (refresh) {
                        allList.value = it.content.getAllList()
                    } else {
                        val list = allList.value!!.toMutableList()
                        list.addAll(it.content.getAllList())
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
}
