package me.ikirby.ithomereader.api.impl

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import me.ikirby.ithomereader.api.UserApi
import me.ikirby.ithomereader.network.ITHomeApi
import me.ikirby.ithomereader.util.Logger
import java.io.IOException

object UserApiImpl : UserApi {
    private val tag = javaClass.simpleName

    override fun login(username: String, password: String): Deferred<String?> = GlobalScope.async {
        return@async try {
            ITHomeApi.login(username, password)
        } catch (e: IOException) {
            Logger.e(tag, "login", e)
            null
        }
    }

}