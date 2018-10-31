package me.ikirby.ithomereader.api

import kotlinx.coroutines.Deferred

interface UserApi {
    fun login(username: String, password: String): Deferred<String?>
}