package me.ikirby.ithomereader

import kotlinx.coroutines.runBlocking
import me.ikirby.ithomereader.clientapi.Api
import me.ikirby.ithomereader.util.encryptString
import org.junit.Assert
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCommentResponse() {
        val commentResponse = runBlocking {
            Api.api.commentApi.getNewsComment("bc8bd1b872f5ce80", null, "760")
        }

        println(commentResponse.content.getAllList())
    }

    @Test
    fun testEncryptNewsId() {
        val newsId = "525531"
        val expected = "7838f76dd3849873"

        Assert.assertEquals(expected, encryptString(newsId))
    }

    @Test
    fun testCommentContentResponse() {
        val commentContentResponse = runBlocking {
            Api.api.commentApi.getCommentContent(encryptString("53815721")!!)
        }

        println(commentContentResponse.content.commentContentList)
    }
}
