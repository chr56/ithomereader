package me.ikirby.ithomereader.api.impl

import me.ikirby.ithomereader.api.ArticleApi
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.ArticleGrade
import me.ikirby.ithomereader.entity.FullArticle
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.addWhiteSpace
import me.ikirby.ithomereader.util.getMatchInt
import me.ikirby.ithomereader.util.isUrlImgSrc
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.regex.Pattern

object ArticleApiImpl : ArticleApi {
    private const val TAG = "ArticleApiImpl"

    override fun getArticleList(
        page: Int,
        filterLapin: Boolean,
        keywordsList: List<String>,
        oldList: ArrayList<Article>?
    ): List<Article>? {
        try {
            val doc = ITHomeApi.getNewsListDoc(page)
            val list = mutableListOf<Article>()
            val posts = doc.select("li")
            if (!posts.isEmpty()) {
                for (post in posts) {
                    val item = getArticleObj(post)
                    if (keywordsList.isNotEmpty()) {
                        if (shouldAddItem(item, keywordsList, filterLapin)) {
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
            Logger.e(TAG, "getArticleList", e)
            return null
        }
    }

    override fun getSearchResults(keyword: String): List<Article>? {
        return try {
            val doc = ITHomeApi.getSearchDoc(keyword)
            val list = mutableListOf<Article>()
            val posts = doc.select("ul.bl li")
            if (!posts.isEmpty()) {
                for (post in posts) {
                    list.add(getSearchArticleObj(post))
                }
            }
            list
        } catch (e: Exception) {
            Logger.e(TAG, "getSearchResults", e)
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
                gradeHtml = matcher.group(1)!!.replace(");", "")
                val gradeDoc = Jsoup.parse(gradeHtml)
                val score =
                    addWhiteSpace(gradeDoc.selectFirst(".text .sd")?.text() ?: gradeDoc.selectFirst(".text")?.text() ?: "")

                val bt = gradeDoc.select(".bt span div")
                val trash = bt[1].text()
                val great = bt[0].text()
                grade = ArticleGrade(score, trash, great)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "getArticleGrade", e)
        }
        return grade
    }

    override fun articleVote(id: String, type: Int, cookie: String): String? {
        return try {
            ITHomeApi.newsVote(id, type, cookie)
        } catch (e: Exception) {
            Logger.e(TAG, "articleVote", e)
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

    private fun getArticleObj(post: Element): Article {
        val title = addWhiteSpace(post.select("a.title").text())
        val date = post.select(".c").attr("data-ot")
        var url = post.select("a.title").attr("abs:href")
        var thumb = post.select("a.img > img").attr("src")
        if (thumb.startsWith("//")) {
            thumb = thumb.replace("//", "https://")
        } else if (thumb.startsWith("http://")) {
            thumb = thumb.replace("http://", "https://")
        }

        // desc is not used for now
        val desc = "" // addWhiteSpace(post.select(".memo p").text())

        if (url.contains("umeng.com")) {
            url = parseSpecialUrl(url)
        }
        if (url.startsWith("http://")) {
            url = url.replace("http://", "https://")
        }

        val isAd = url.contains("/lapin.")

        return Article(title, date, url, thumb, desc, isAd)
    }

    private fun getSearchArticleObj(post: Element): Article {
        val title = addWhiteSpace(post.select("h2 a").text())
        val date = post.select("span.state").text()
        val url = post.select("h2 a").attr("abs:href")
        val thumb = post.getElementsByTag("img")[0].attr("abs:src")
        return Article(title, date, url, thumb, "")
    }

    private fun parseSpecialUrl(url: String): String {
        val pattern = Pattern.compile("www\\.ithome\\.com.+\\.htm")
        val matcher = pattern.matcher(url)
        if (matcher.find()) {
            try {
                return "https://" + URLDecoder.decode(matcher.group(0), "utf-8")
            } catch (e: UnsupportedEncodingException) {
                Logger.e(TAG, "parseSpecialUrl", e)
            }
        }
        return url
    }

    private fun shouldAddItem(item: Article, keywords: List<String>, filterLapin: Boolean): Boolean {
        if (filterLapin && item.isAd) {
            return false
        }
        keywords.forEach {
            if (item.title.contains(it)) {
                return false
            }
        }
        return true
    }

    override fun getFullArticle(url: String, loadImageAutomatically: Boolean, isLiveInfo: Boolean): FullArticle? {
        try {
            val doc = ITHomeApi.getPageDoc(url)
            val title: String
            val time: String
            val newsId: String = "" + getMatchInt(url)
            val newsIdHash: String
            val post: Element?
            if (isLiveInfo) {
                post = doc.getElementById("about_info_show")
                title = doc.getElementsByTag("title")?.text() ?: ""
                time = ""
            } else {
                post = doc.getElementById("paragraph")
                title = doc.select("h1")?.text() ?: ""
                var meta = doc.select(".info .l")
                if (meta.isEmpty()) { // for old version of article page
                    meta = doc.select(".pt_info")
                }

                val avatar = meta.select("a.avatar")
                val pubtime = meta.select("#pubtime_baidu")?.text() ?: ""
                time = pubtime + if (avatar.isNotEmpty()) {
                    " " + avatar.text() + "(IT号)"
                } else {
                    " " + (meta.select("#author_baidu strong")?.text() ?: "") +
                            "(" + (meta.select("#source_baidu a")?.text() ?: "") + ")"
                }
            }

            if (post == null) return FullArticle()

            val imgs = post.getElementsByTag("img")
            if (loadImageAutomatically) {
                imgs.forEach {
                    val origUrl = it.attr("data-original")
                    if (origUrl.isNotBlank()) {
                        it.attr("src", origUrl)
                    }
                    it.attr("loading", "lazy")
                    it.removeAttr("srcset")
                    it.addClass("loaded")
                }
            } else {
                imgs.forEach {
                    if (it.attr("data-original").isBlank()) {
                        it.attr("data-original", it.attr("abs:src"))
                    }
                    it.removeAttr("src")
                    it.removeAttr("srcset")
                    it.attr("title", "点按加载图片")
                }
            }

            val links = post.getElementsByTag("a")
            links.forEach {
                if (isUrlImgSrc(it.attr("href"))) {
                    it.removeAttr("href")
                }
            }
//            post.select("iframe").remove()
//            post.select("embed").remove()
            var content = "<div id=\"header\"><h2>$title</h2><p>$time</p></div>"
            content += post.toString().replace("<script.*</script>".toRegex(), "")

            val ifComment = doc.getElementById("ifcomment")
            newsIdHash = if (ifComment != null) {
                if (isLiveInfo) {
                    ifComment.attr("src").split("/").last()
                } else {
                    ifComment.attr("data")
                }
            } else {
                ""
            }
            return FullArticle(newsId, newsIdHash, addWhiteSpace(title), time, content)
        } catch (e: Exception) {
            Logger.e(TAG, "getFullArticle", e)
            return null
        }
    }
}