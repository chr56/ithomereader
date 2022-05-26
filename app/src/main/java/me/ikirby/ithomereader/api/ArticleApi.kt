package me.ikirby.ithomereader.api

import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.ArticleGrade
import me.ikirby.ithomereader.entity.FullArticle

interface ArticleApi {

    fun getArticleList(
        page: Int,
        filterLapin: Boolean,
        keywordsList: List<String>,
        oldList: ArrayList<Article>?
    ): List<Article>?

    fun getSearchResults(keyword: String): List<Article>?

    fun getSearchResultsWithPages(keyword: String, page: Int, cookie: String?): List<Article>?

    fun getArticleGrade(id: String, cookie: String?): ArticleGrade?

    fun articleVote(id: String, type: Int, cookie: String): String?

    fun getFullArticle(url: String, loadImageAutomatically: Boolean, isLiveInfo: Boolean): FullArticle?
}
