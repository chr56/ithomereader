package me.ikirby.ithomereader.api

import kotlinx.coroutines.experimental.Deferred

interface UserApi {
    fun login(username: String, password: String): Deferred<String?>
}