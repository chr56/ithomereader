package me.ikirby.ithomereader.api.impl

import me.ikirby.ithomereader.api.TrendingApi
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.Trending
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.addWhiteSpace

object TrendingApiImpl : TrendingApi {
    private val tag = javaClass.simpleName

    override fun getFocusBannerArticles(): List<Article>? {
        val list = mutableListOf<Article>()
        return try {
            val elements = ITHomeApi.getHomePage().getElementById("coin-slider").getElementsByTag("item")
            for (element in elements) {
                val url = element.attr("data")
                if (!url.contains("www.ithome.com")) continue
                val title = addWhiteSpace(element.getElementsByTag("span")[0].text())
                val thumb = element.getElementsByTag("img")[0].attr("abs:src")
                list.add(Article(title, null, url, thumb, null))
            }
            list
        } catch (e: Exception) {
            Logger.e(tag, "getFocusBannerArticles", e)
            null
        }
    }

    override fun getTrendingList(): List<Trending>? {
        try {
            val document = ITHomeApi.getHomePage()
            val list = mutableListOf<Trending>()
            var elements = document.select("div.focus")[0].getElementsByTag("li")
            list.add(Trending(null, "焦点关注", null))
            for (element in elements) {
                val link = element.select("h2 a")[0]
                val title = addWhiteSpace(link.text())
                val url = link.attr("abs:href").replace("http://", "https://")
                val thumb = element.getElementsByTag("img")[0].attr("abs:src")
                val desc = addWhiteSpace(element.getElementsByTag("p").text())
                list.add(Trending(title = title, url = url, desc = desc, thumb = thumb))
            }
            elements = document.select(".hot-list *")
            for (element in elements) {
                if (element.tagName() == "h4") {
                    list.add(Trending(null, addWhiteSpace(element.text()), null))
                } else if (element.tagName() == "li") {
                    val rank = element.getElementsByTag("span")[0].text()
                    val link = element.getElementsByTag("a")[0]
                    val title = addWhiteSpace(link.text())
                    val url = link.attr("abs:href").replace("http://", "https://")
                    list.add(Trending(rank, title, url))
                }
            }
            return list
        } catch (e: Exception) {
            Logger.e(tag, "getTrendingList", e)
            return null
        }
    }
}