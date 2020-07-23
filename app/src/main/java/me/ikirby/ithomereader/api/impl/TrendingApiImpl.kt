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
            val elements = ITHomeApi.getHomePage().getElementById("focus-owl-wrap").getElementsByTag("li")
            for (element in elements) {
                val url = element.getElementsByTag("a")[0].attr("abs:href")
                if (!url.contains("www.ithome.com")) continue
                val image = element.getElementsByTag("img")[0]
                val title = addWhiteSpace(image.attr("alt"))
                val thumb = image.attr("abs:src")
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
            val homeDocument = ITHomeApi.getHomePage()
            val list = mutableListOf<Trending>()
            val focusElements = homeDocument.select("#p-b .fr a")
//            list.add(Trending(null, "焦点关注", null))
            for (element in focusElements) {
                val title = addWhiteSpace(element.text())
                val url = element.attr("abs:href")
                val thumb = element.getElementsByTag("img")[0].attr("abs:src")
//                val desc = addWhiteSpace(element.getElementsByTag("p").text())
                list.add(Trending(title = title, url = url, thumb = thumb))
            }
            val rankDocument = ITHomeApi.getRankBlock()
            val titles = rankDocument.select("ul.bar li")
            titles.forEachIndexed { index, element ->
                list.add(Trending(null, addWhiteSpace(element.text()), null))
                val rankList = rankDocument.select("#d-${index + 1} li a")
                rankList.forEachIndexed { i, e ->
                    val rank = (i + 1).toString()
                    val title = e.attr("title")
                    val link = e.attr("abs:href")
                    list.add(Trending(rank, title, link))
                }
            }
            return list
        } catch (e: Exception) {
            Logger.e(tag, "getTrendingList", e)
            return null
        }
    }
}