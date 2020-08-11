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
            val elements = ITHomeApi.getHomePage().getElementById("focus-owl-wrap").getElementsByTag("a")
            for (element in elements) {
                val url = element.attr("abs:href")
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

            val headlineElements = homeDocument.select("#tt a")
            if (headlineElements.isNotEmpty()) {
                list.add(Trending(null, "头条", null))
                headlineElements.forEachIndexed { index, element ->
                    val rank = (index + 1).toString()
                    val url = element.attr("abs:href")
                    val title = addWhiteSpace(element.text())
                    list.add(Trending(rank, title, url))
                }
            }

            val focusElements = homeDocument.select("#news .fr .gb .p a.img")
            if (focusElements.isNotEmpty()) {
                list.add(Trending(null, "焦点关注", null))
                for (element in focusElements) {
                    val url = element.attr("abs:href")
                    val imgElement = element.getElementsByTag("img")[0]
                    val title = addWhiteSpace(imgElement.attr("alt"))
                    val thumb = imgElement.attr("abs:src")
//                val desc = addWhiteSpace(element.getElementsByTag("p").text())
                    list.add(Trending(title = title, url = url, thumb = thumb))
                }
            }

            val rankDocument = ITHomeApi.getRankBlock()
            val titles = rankDocument.select("ul.bar li")
            titles.forEachIndexed { index, element ->
                val rankList = rankDocument.select(".bd#d-${index + 1} li a")
                if (rankList.isNotEmpty()) {
                    list.add(Trending(null, addWhiteSpace(element.text()), null))
                    rankList.forEachIndexed { i, e ->
                        val rank = (i + 1).toString()
                        val title = e.attr("title")
                        val url = e.attr("abs:href")
                        list.add(Trending(rank, title, url))
                    }
                }
            }
            return list
        } catch (e: Exception) {
            Logger.e(tag, "getTrendingList", e)
            return null
        }
    }
}