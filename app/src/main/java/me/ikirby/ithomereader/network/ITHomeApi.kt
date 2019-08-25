package me.ikirby.ithomereader.network

import android.annotation.SuppressLint
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

object ITHomeApi {
    const val IFCOMMENT_URL = "https://dyn.ithome.com/comment/"
    // const val LAPIN_IFCOMMENT_URL = "https://www.lapin365.com/comment/index?id="

    private const val HOME_URL = "https://www.ithome.com"
    private const val NEWS_URL = "https://www.ithome.com/ithome/getajaxdata.aspx?type=indexpage&page="
    private const val AJAX_DATA_URL = "https://dyn.ithome.com/ithome/getajaxdata.aspx"
    private const val COMMENT_POST_URL = "https://dyn.ithome.com/ithome/postComment.aspx"
    private const val LOGIN_URL = "https://dyn.ithome.com/ithome/login.aspx/btnLogin_Click"
    private const val SEARCH_URL = "https://dyn.ithome.com/search/adt_all_%s_0_%d.html"
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.18 Safari/537.36"
    private const val LIVE_URL = "https://live.ithome.com/newsinfo/getnewsph?newsid="
    private const val LAPIN_AJAX_DATA_URL = "https://www.lapin365.com/comment/getajaxdata"
    private const val NEWS_GRADE_URL = "https://dyn.ithome.com/grade/"
    private const val NEWS_GRADE_VOTE_URL = "https://m.ithome.com/api/news/newsgradeset"

    /**
     * 获取主页内容
     *
     * @return 主页内容 Document
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getHomePage(): Document {
        return NetworkRequest.getDocument(
            HOME_URL,
            getHeaders(null, null, null)
        )
    }


    /**
     * 获取新闻列表文档
     *
     * @param page 页码
     * @return 新闻列表文档
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getNewsListDoc(page: Int): Document {
        return NetworkRequest.getDocument(
            NEWS_URL + page,
            getHeaders("XMLHttpRequest", null, null)
        )
    }

    /**
     * 获取热门评论文档
     *
     * @param id 文章 ID
     * @return 热门评论文档
     * @throws IOException 网络请求异常
     * @throws JSONException json 处理异常
     */
    @Throws(IOException::class, JSONException::class)
    fun getHotCommentsDoc(id: String, hash: String, page: Int, isLapin: Boolean): Document {
        val postData = mutableMapOf(
            "newsID" to id,
            "type" to "hotcomment",
            "pid" to "" + page,
            "order" to "false"
        )

        val url = if (isLapin) {
            LAPIN_AJAX_DATA_URL
        } else {
            postData["hash"] = hash
            AJAX_DATA_URL
        }

        val response = NetworkRequest.getResponse(
            url,
            getHeaders("XMLHttpRequest", null, null),
            postData
        )

        return Jsoup.parse(JSONObject(response.body()).getString("html"))
    }

    /**
     * 获取全部评论文档
     *
     * @param id 文章 ID
     * @param hash 文章 hash
     * @param page 评论页码
     * @return 全部评论文档
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getCommentsDoc(id: String, hash: String, page: Int, isLapin: Boolean): Document {
        val postData = mutableMapOf(
            "newsID" to id,
            "type" to "commentpage",
            "page" to "" + page,
            "order" to "false"
        )

        val url = if (isLapin) {
            LAPIN_AJAX_DATA_URL
        } else {
            postData["hash"] = hash
            AJAX_DATA_URL
        }

        return NetworkRequest.getDocument(
            url,
            getHeaders("XMLHttpRequest", null, null),
            postData
        )
    }

    /**
     * 获取评论楼层中的更多回复文档
     *
     * @param parentId 父评论 ID
     * @return 更多回复文档
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getMoreReplies(parentId: String): Document {
        val postData = mapOf(
            "commentid" to parentId,
            "type" to "getmorelou"
        )

        return NetworkRequest.getDocument(
            AJAX_DATA_URL,
            getHeaders("XMLHttpRequest", null, null),
            postData
        )
    }

    /**
     * 获取单个评论串，用于展开热评
     *
     * @param id 评论 ID，应使用父评论 ID
     * @param newsId 新闻 ID
     * @return 单个评论串文档
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun getSingleComment(id: String, newsId: String): Document {
        val postData = mapOf(
            "commentid" to id,
            "newsId" to newsId,
            "type" to "getsinglecomment"
        )

        return NetworkRequest.getDocument(
            AJAX_DATA_URL,
            getHeaders("XMLHttpRequest", null, null),
            postData
        )
    }

    /**
     * 提交评论并获取返回信息
     *
     * @param id 新闻 ID
     * @param parentId 回复的评论的父评论 ID，不回复时不需要
     * @param selfId 回复的评论的自身 ID，不回复时不需要
     * @param commentContent 评论内容
     * @param cookie 帐号登录后获取的 cookie
     * @return post 后返回的信息
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun postComment(
        id: String, parentId: String?, selfId: String?,
        commentContent: String, cookie: String
    ): String {
        val postData = mutableMapOf(
            "newsid" to id,
            "commentNick" to "undefined",
            "txtCode" to "undefined",
            "type" to "comment",
            "commentContent" to commentContent
        )
        postData["newsid"] = id
        postData["commentNick"] = "undefined"
        if (parentId != null && selfId != null) {
            postData["parentCommentId"] = parentId
            postData["ppCID"] = selfId
        } else {
            postData["parentCommentID"] = "0"
        }


        return NetworkRequest.getResponse(
            COMMENT_POST_URL, getHeaders(
                "XMLHttpRequest",
                "application/x-www-form-urlencoded", cookie
            ),
            postData
        ).body()
    }

    /**
     * 登录并获取 Cookie
     *
     * @param username 用户名
     * @param password 密码
     * @return Cookie
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun login(username: String, password: String): String? {
        val payload = "{\"username\":\"$username\", \"password\":\"$password\"}"
        val response = NetworkRequest.getResponse(
            LOGIN_URL,
            getHeaders("XMLHttpRequest", "application/json", null),
            payload
        )
        return response.cookie("user")
    }

    /**
     * 获取搜索结果文档
     * @param keyword 搜索关键词
     * @param page 搜索页码
     * @return 搜索结果文档
     * @throws IOException 网络请求异常
     */
    @SuppressLint("DefaultLocale")
    @Throws(IOException::class)
    fun getSearchDoc(keyword: String, page: Int): Document {
        return NetworkRequest.getDocument(
            String.format(SEARCH_URL, keyword, page),
            getHeaders(null, null, null)
        )
    }

    /**
     * 评论评分
     *
     * @param id 评论 ID
     * @param typeId 类型 ID，1 为支持，2 为反对
     * @param isCancel 是否为取消评分
     * @param cookie 用户 cookie
     * @return 返回信息
     * @throws IOException 网络请求异常
     */
    @Throws(IOException::class)
    fun commentVote(id: String, typeId: Int, isCancel: Boolean, cookie: String): String {
        val postData = mutableMapOf(
            "commentid" to id,
            "typeid" to "" + typeId
        )
        if (isCancel) {
            postData["type"] = "loginCancleReplyVote"
        } else {
            postData["type"] = "loginReplyVote"
        }

        return NetworkRequest.getResponse(
            COMMENT_POST_URL, getHeaders(
                "XMLHttpRequest",
                "application/x-www-form-urlencoded", cookie
            ),
            postData
        ).body()
    }

    /**
     * 获取图文直播信息
     * @param id 新闻 ID
     * @return 直播信息 JSON
     * @throws IOException 网络请求错误
     */
    @Throws(IOException::class)
    fun getLiveMsgJson(id: String): String {
        return NetworkRequest.getResponse(
            LIVE_URL + id,
            getHeaders(null, null, null)
        ).body()
    }

    /**
     * 获取新闻评分信息
     * @param id 新闻 ID
     * @param cookie Cookie
     * @return 新闻评分的脚本数据
     */
    @Throws(IOException::class)
    fun getNewsGrade(id: String, cookie: String?): String {
        return NetworkRequest.getResponse(
            NEWS_GRADE_URL + id,
            getHeaders("XMLHttpRequest", null, cookie)
        ).body()
    }

    /**
     * 新闻评分并获取结果
     * @param id 新闻 ID
     * @param type 评分类型，0 无价值， 1 还可以， 2 有价值
     * @param cookie Cookie
     * @return 评分返回结果
     */
    @SuppressLint("DefaultLocale")
    @Throws(IOException::class)
    fun newsVote(id: String, type: Int, cookie: String): String {
        val postData = mapOf(
            "newsID" to id,
            "grade" to "" + type
        )
        return NetworkRequest.getResponse(
            NEWS_GRADE_VOTE_URL,
            getHeaders(
                "XMLHttpRequest",
                "application/x-www-form-urlencoded", cookie
            ),
            postData
        ).body()
    }

    /**
     * 获取页面 Document
     * @param url 页面链接
     * @return 页面 Document
     */
    @Throws(IOException::class)
    fun getPageDoc(url: String): Document {
        return NetworkRequest.getDocument(url, getHeaders(null, null, null))
    }

    /**
     * 获取请求头
     * @param xRequestedWith X-Requested-With
     * @param contentType Content-Type
     * @param cookie Cookie
     * @return 请求头 HashMap
     */
    private fun getHeaders(xRequestedWith: String?, contentType: String?, cookie: String?): Map<String, String> {
        val headers = mutableMapOf(
            "User-Agent" to USER_AGENT
        )
        if (xRequestedWith != null) {
            headers["X-Requested-With"] = xRequestedWith
        }
        if (contentType != null) {
            headers["Content-Type"] = contentType
        }
        if (cookie != null) {
            headers["Cookie"] = cookie
        }
        return headers
    }
}
