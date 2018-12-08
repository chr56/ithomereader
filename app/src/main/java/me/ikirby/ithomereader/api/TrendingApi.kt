package me.ikirby.ithomereader.api

import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.Trending

interface TrendingApi {
    fun getTrendingList(): List<Trending>?

    fun getFocusBannerArticles(): List<Article>?
}