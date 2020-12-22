package me.ikirby.ithomereader.clientapi

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.ikirby.ithomereader.clientapi.moshi.DateAdapter
import me.ikirby.ithomereader.entity.app.comment.CommentResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface CommentApi {

    @GET("comment/getnewscomment")
    suspend fun getNewsComment(
        @Query("sn") sn: String,
        @Query("cid") cid: Long?,
        @Query("appver") appver: String
    ): CommentResponse

    companion object {
        fun create(okHttpClient: OkHttpClient): CommentApi {
            val moshi = Moshi.Builder()
                .add(Date::class.java, DateAdapter().nullSafe())
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl("https://cmt.ithome.com/api/")
                .build()
            return retrofit.create(CommentApi::class.java)
        }
    }
}
