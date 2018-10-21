package me.ikirby.ithomereader.api

import kotlinx.coroutines.experimental.Deferred
import me.ikirby.ithomereader.entity.Article
import me.ikirby.ithomereader.entity.Trending

interface TrendingApi {
    fun getTrendingList(): Deferred<List<Trending>?>

    fun getFocusBannerArticles(): Deferred<List<Article>?>
}