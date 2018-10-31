package me.ikirby.ithomereader.api

import kotlinx.coroutines.Deferred
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.ArticleGrade
import me.ikirby.ithomereader.entity.FullArticle

interface ArticleApi {

    fun getArticleList(page: Int, filterLapin: Boolean,
                       customFilter: Boolean, keywords: Array<String>?,
                       oldList: ArrayList<Article>?): Deferred<List<Article>?>

    fun getSearchResults(keyword: String, page: Int): Deferred<List<Article>?>

    fun getArticleGrade(id: String, cookie: String?): Deferred<ArticleGrade?>

    fun articleVote(id: String, type: Int, cookie: String): Deferred<String?>

    fun getFullArticle(url: String, loadImageAutomatically: Boolean,
                       isLiveInfo: Boolean): Deferred<FullArticle?>
}