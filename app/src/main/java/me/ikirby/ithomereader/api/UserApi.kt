package me.ikirby.ithomereader.api

interface UserApi {
    fun login(username: String, password: String): String?
}