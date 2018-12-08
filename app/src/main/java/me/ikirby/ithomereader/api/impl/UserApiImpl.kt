package me.ikirby.ithomereader.api.impl

import me.ikirby.ithomereader.api.UserApi
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger

object UserApiImpl : UserApi {
    private val tag = javaClass.simpleName

    override fun login(username: String, password: String): String? {
        return try {
            ITHomeApi.login(username, password)
        } catch (e: Exception) {
            Logger.e(tag, "login", e)
            null
        }
    }

}