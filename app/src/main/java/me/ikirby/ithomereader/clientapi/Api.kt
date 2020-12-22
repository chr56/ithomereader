package me.ikirby.ithomereader.clientapi

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class Api private constructor() {
    companion object {
        val api by lazy { Api() }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    val commentApi = CommentApi.create(okHttpClient)
}
