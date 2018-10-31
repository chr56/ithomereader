package me.ikirby.ithomereader.api

import kotlinx.coroutines.Deferred
import me.ikirby.ithomereader.entity.LiveMsg

interface LiveApi {
    fun getLiveMessages(id: String): Deferred<List<LiveMsg>?>
}