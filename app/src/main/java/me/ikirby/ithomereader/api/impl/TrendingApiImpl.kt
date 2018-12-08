package me.ikirby.ithomereader.api.impl

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import me.ikirby.ithomereader.api.TrendingApi
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.Trending
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger

object TrendingApiImpl : TrendingApi {
    private val tag = javaClass.simpleName

    override fun getFocusBannerArticles(): Deferred<List<Article>?> = GlobalScope.async {
        val list = mutableListOf<Article>()
        try {
            val elements = ITHomeApi.getHomePage().getElementById("coin-slider").getElementsByTag("item")
            for (element in elements) {
                val url = element.attr("data")
                if (!url.contains("www.ithome.com")) continue
                val title = element.getElementsByTag("span")[0].text()
                val thumb = element.getElementsByTag("img")[0].attr("abs:src")
                list.add(Article(title, null, url, thumb, null))
            }
            return@async list
        } catch (e: Exception) {
            Logger.e(tag, "getFocusBannerArticles", e)
            return@async null
        }
    }

    override fun getTrendingList(): Deferred<List<Trending>?> = GlobalScope.async {
        try {
            val document = ITHomeApi.getHomePage()
            val list = mutableListOf<Trending>()
            var elements = document.select("div.focus")[0].getElementsByTag("li")
            list.add(Trending(null, "焦点关注", null))
            for (element in elements) {
                val link = element.select("h2 a")[0]
                val title = link.text()
                val url = link.attr("abs:href").replace("http://", "https://")
                val thumb = element.getElementsByTag("img")[0].attr("abs:src")
                val desc = element.getElementsByTag("p").text()
                list.add(Trending(title = title, url = url, desc = desc, thumb = thumb))
            }
            elements = document.select(".hot-list *")
            for (element in elements) {
                if (element.tagName() == "h4") {
                    list.add(Trending(null, element.text(), null))
                } else if (element.tagName() == "li") {
                    val rank = element.getElementsByTag("span")[0].text()
                    val link = element.getElementsByTag("a")[0]
                    val title = link.text()
                    val url = link.attr("abs:href").replace("http://", "https://")
                    list.add(Trending(rank, title, url))
                }
            }
            return@async list
        } catch (e: Exception) {
            Logger.e(tag, "getTrendingList", e)
            return@async null
        }
    }
}