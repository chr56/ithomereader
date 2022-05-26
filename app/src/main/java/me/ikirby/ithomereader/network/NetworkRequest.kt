package me.ikirby.ithomereader.network

import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.concurrent.TimeUnit

internal object NetworkRequest {
    private const val TIMEOUT = 7000

    /**
     * 获取指定页面文档
     *
     * @param url 页面 URL
     * @return 页面 document
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getDocument(url: String, headers: Map<String, String>): Document {
        return Jsoup.connect(url)
            .headers(headers)
            .ignoreContentType(true)
            .timeout(TIMEOUT)
            .get()
    }

    /**
     * 获取指定页面文档带 post 数据
     *
     * @param url 页面 URL
     * @return 页面 document
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getDocument(url: String, headers: Map<String, String>, postData: Map<String, String>): Document {
        return Jsoup.connect(url)
            .headers(headers)
            .ignoreContentType(true)
            .timeout(TIMEOUT)
            .data(postData)
            .method(Connection.Method.POST)
            .get()
    }

    /**
     * 获取 Jsoup Response
     *
     * @param url URL
     * @return Jsoup Response
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getResponse(url: String, headers: Map<String, String>): Connection.Response {
        return Jsoup.connect(url)
            .headers(headers)
            .ignoreContentType(true)
            .timeout(TIMEOUT)
            .execute()
    }
    // todo
    @Throws(IOException::class)
    fun getPostResponse(url: String, headers: Map<String, String>): Response {
        val client = OkHttpClient.Builder().connectTimeout(TIMEOUT.toLong(), TimeUnit.MILLISECONDS).build()
        val call = client.newCall(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .post(FormBody.Builder().build())//todo
                .build()
        )
        return call.execute()
    }

    /**
     * 获取 Jsoup Response 带 post 数据
     *
     * @param url URL
     * @param postData post 数据
     * @return Jsoup Response
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getResponse(url: String, headers: Map<String, String>, postData: Map<String, String>): Connection.Response {
        return Jsoup.connect(url)
            .headers(headers)
            .ignoreContentType(true)
            .timeout(TIMEOUT)
            .data(postData)
            .method(Connection.Method.POST)
            .execute()
    }

    /**
     * 获取 Jsoup Response 带 post 数据
     *
     * @param url URL
     * @param payload post payload 数据
     * @return Jsoup Response
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getResponse(url: String, headers: Map<String, String>, payload: String): Connection.Response {
        return Jsoup.connect(url)
            .headers(headers)
            .ignoreContentType(true)
            .timeout(TIMEOUT)
            .requestBody(payload)
            .method(Connection.Method.POST)
            .execute()
    }
}
