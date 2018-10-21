package me.ikirby.ithomereader.entity

data class FullArticle(val newsId: String,
                       val title: String,
                       val time: String,
                       val content: String) {
    constructor() : this("", "", "", "")
}