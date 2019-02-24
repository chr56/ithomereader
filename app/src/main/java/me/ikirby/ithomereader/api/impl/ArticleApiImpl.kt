package me.ikirby.ithomereader.api.impl

import me.ikirby.ithomereader.api.ArticleApi
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.ArticleGrade
import me.ikirby.ithomereader.entity.FullArticle
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.getMatchInt
import me.ikirby.ithomereader.util.isUrlImgSrc
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.regex.Pattern

object ArticleApiImpl : ArticleApi {
    private val tag = javaClass.simpleName

    override fun getArticleList(page: Int, filterLapin: Boolean, customFilter: Boolean, keywords: Array<String>?,
                                oldList: ArrayList<Article>?): List<Article>? {
        try {
            val doc = ITHomeApi.getNewsListDoc(page)
            val list = mutableListOf<Article>()
            val posts = doc.select(".ulcl li")
            if (!posts.isEmpty()) {
                for (post in posts) {
                    val item = getArticleObj(post, false)
                    if (customFilter && keywords != null) {
                        if (shouldAddItem(item, keywords, filterLapin)) {
                            list.add(item)
                        }
                    } else if (filterLapin) {
                        if (!item.isAd) {
                            list.add(item)
                        }
                    } else {
                        list.add(item)
                    }
                }
            }
            return removeDuplicate(list, oldList)
        } catch (e: Exception) {
            Logger.e(tag, "getArticleList", e)
            return null
        }
    }

    override fun getSearchResults(keyword: String, page: Int): List<Article>? {
        return try {
            val doc = ITHomeApi.getSearchDoc(keyword, page)
            val list = mutableListOf<Article>()
            val posts = doc.select(".ulcl li")
            if (!posts.isEmpty()) {
                for (post in posts) {
                    list.add(getArticleObj(post, true))
                }
            }
            list
        } catch (e: Exception) {
            Logger.e(tag, "getSearchResults", e)
            null
        }
    }

    override fun getArticleGrade(id: String, cookie: String?): ArticleGrade? {
        var grade: ArticleGrade? = null
        try {
            var gradeHtml = ITHomeApi.getNewsGrade(id, cookie)
            val pattern = Pattern.compile("^var gradestr = '(.+)';$", Pattern.MULTILINE)
            val matcher = pattern.matcher(gradeHtml)
            if (matcher.find()) {
                gradeHtml = matcher.group(1).replace(");", "")
                val gradeDoc = Jsoup.parse(gradeHtml)
                val score = gradeDoc.selectFirst(".text .sd").text()

                val bt = gradeDoc.select(".bt span div")
                val trash = bt[0].text()
                val soso = bt[1].text()
                val great = bt[2].text()
                grade = ArticleGrade(score, trash, soso, great)
            }
        } catch (e: Exception) {
            Logger.e(tag, "getArticleGrade", e)
        }
        return grade
    }

    override fun articleVote(id: String, type: Int, cookie: String): String? {
        return try {
            ITHomeApi.newsVote(id, type, cookie)
        } catch (e: Exception) {
            Logger.e(tag, "articleVote", e)
            null
        }
    }

    private fun removeDuplicate(newList: MutableList<Article>, oldList: List<Article>?): MutableList<Article> {
        if (oldList != null && newList.isNotEmpty()) {
            var needRemove = true
            for (i in oldList.indices.reversed()) {
                val oldItem = oldList[i]
                for (newItem in newList) {
                    if (newItem.url == oldItem.url) {
                        needRemove = true
                        newList.remove(newItem)
                        break
                    } else {
                        needRemove = false
                    }
                }
                if (!needRemove) {
                    break
                }
            }
        }
        return newList
    }

    private fun getArticleObj(post: Element, isSearch: Boolean): Article {
        val title = post.select("h2 a").text()
        val date = post.select("h2 .state").text()
        var url = post.select("h2 a").attr("abs:href")
        var thumb: String = if (isSearch) {
            post.select(".list_thumbnail img").attr("abs:data-original")
        } else {
            post.select(".list_thumbnail img").attr("abs:src")
        }
        thumb = thumb.replace("http://", "https://")
        val desc = post.select(".memo p").text()
        if (url.contains("umeng.com")) {
            url = parseSpecialUrl(url)
        }
        url = url.replace("http://", "https://")

        val tagImages = post.select(".block img")

        val isAd = if (tagImages.isNotEmpty()) {
            tagImages.attr("src").contains("/42.png")
        } else {
            false
        }

        return Article(title, date, url, thumb, desc, isAd)
    }

    private fun parseSpecialUrl(url: String): String {
        val pattern = Pattern.compile("www\\.ithome\\.com.+\\.htm")
        val matcher = pattern.matcher(url)
        if (matcher.find()) {
            try {
                return "https://" + URLDecoder.decode(matcher.group(0), "utf-8")
            } catch (e: UnsupportedEncodingException) {
                Logger.e(tag, "parseSpecialUrl", e)
            }
        }
        return url
    }

    private fun shouldAddItem(item: Article, keywords: Array<String>, filterLapin: Boolean): Boolean {
        if (filterLapin && item.isAd) {
            return false
        }
        for (keyword in keywords) {
            if (item.title.contains(keyword)) {
                return false
            }
        }
        return true
    }

    override fun getFullArticle(url: String, loadImageAutomatically: Boolean, isLiveInfo: Boolean): FullArticle? {
        try {
            val doc = Jsoup.connect(url).timeout(5000).get()
            val title: String
            val time: String
            val newsId: String
            val post: Element?
            if (isLiveInfo) {
                post = doc.getElementById("about_info_show")
                title = doc.getElementsByTag("title")?.text() ?: ""
                time = ""
            } else if (url.contains("wap.") || url.contains("m.")) {
                post = doc.select(".news-content")?.get(0)
                title = doc.select("h1.title")?.text() ?: ""
                time = doc.select(".news-mes")?.text() ?: ""
            } else {
                post = doc.getElementById("paragraph")
                val meta = doc.getElementsByClass("post_title")
                title = meta.select("h1")?.text() ?: ""
                time = (meta.select("#pubtime_baidu")?.text() ?: "") +
                        " " + (meta.select("#source_baidu a")?.text() ?: "") +
                        "(" + (meta.select("#author_baidu strong")?.text() ?: "") + ")"
            }

            if (post == null) return FullArticle()

            val imgs = post.getElementsByTag("img")
            if (loadImageAutomatically && !isLiveInfo) {
                for (img in imgs) {
                    val origUrl = img.attr("data-original")
                    img.attr("src", origUrl)
                    img.addClass("loaded")
                }
            } else if (!loadImageAutomatically && isLiveInfo) {
                for (img in imgs) {
                    val origUrl = img.attr("abs:src")
                    img.attr("data-original", origUrl)
                    img.removeAttr("src")
                }
            } else if (loadImageAutomatically) {
                for (img in imgs) {
                    img.addClass("loaded")
                }
            }
            val links = post.getElementsByTag("a")
            for (i in links.indices) {
                val link = links[i]
                if (isUrlImgSrc(link.attr("href"))) {
                    link.removeAttr("href")
                }
            }
//            post.select("iframe").remove()
//            post.select("embed").remove()
            var content = "<div id=\"header\"><h2>$title</h2><p>$time</p></div>"
            content += post.toString().replace("<script.*</script>".toRegex(), "")
            if (url.contains("wap.") || url.contains("m.")) {
                val ids = doc.getElementById("newsID")
                newsId = if (ids != null) {
                    ids.attr("data-news-id")
                    //lapinId = ids.attr("data-lapin-id")
                } else {
                    "" + getMatchInt(url)
                }
            } else if (url.contains("live.")) {
                newsId = "" + getMatchInt(url)
            } else {
                val ifComment = doc.getElementById("ifcomment")
                newsId = if (ifComment != null) {
                    ifComment.attr("data")
                    //                        if (url!!.contains("lapin")) {
                    //                            lapinId = ifComment.attr("datalapin")
                    //                        }
                } else {
                    "" + getMatchInt(url)
                }
            }
            return FullArticle(newsId, title, time, content)
        } catch (e: Exception) {
            Logger.e(tag, "getFullArticle", e)
            return null
        }
    }
}