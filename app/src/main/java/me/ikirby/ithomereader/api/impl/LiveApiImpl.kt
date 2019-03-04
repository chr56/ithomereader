package me.ikirby.ithomereader.api.impl

import me.ikirby.ithomereader.LIVE_MSG_TYPE_IMAGE
import me.ikirby.ithomereader.api.LiveApi
import me.ikirby.ithomereader.entity.LiveMsg
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

object LiveApiImpl : LiveApi {
    private val tag = javaClass.simpleName

    override fun getLiveMessages(id: String): List<LiveMsg>? {
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
            return list
        } catch (e: JSONException) {
            Logger.e(tag, "getLiveMessages:JSON format", e)
        } catch (e: Exception) {
            Logger.e(tag, "getLiveMessages", e)
        }
        return null
    }
}