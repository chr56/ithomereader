package me.ikirby.ithomereader.api.impl

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import me.ikirby.ithomereader.LIVE_MSG_TYPE_IMAGE
import me.ikirby.ithomereader.api.LiveApi
import me.ikirby.ithomereader.entity.LiveMsg
import me.ikirby.ithomereader.network.ITHomeApi
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException

object LiveApiImpl : LiveApi {
    override fun getLiveMessages(id: String): Deferred<List<LiveMsg>?> = GlobalScope.async {

        try {
            val msgs = JSONObject(ITHomeApi.getLiveMsgJson(id)).getJSONArray("contents")
            val list = mutableListOf<LiveMsg>()
            for (i in msgs.length() - 1 downTo 0) {
                val msg = msgs.getJSONObject(i)
                val postTime = msg.getString("PostTime")
                val html = Jsoup.parse(msg.getString("NewsHtml"))
                val txt = html.getElementsByClass("txt").text()
                list.add(LiveMsg(postTime, txt, 0))
                for (img in html.getElementsByTag("img")) {
                    list.add(LiveMsg(null, img.attr("data-original"), LIVE_MSG_TYPE_IMAGE))
                }
            }
            return@async list
        } catch (e: JSONException) {
        } catch (e: IOException) {
        }
        return@async null
    }
}