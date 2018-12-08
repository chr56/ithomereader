package me.ikirby.ithomereader.api

import me.ikirby.ithomereader.entity.LiveMsg

interface LiveApi {
    fun getLiveMessages(id: String): List<LiveMsg>?
}