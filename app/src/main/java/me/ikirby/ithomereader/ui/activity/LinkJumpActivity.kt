package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import me.ikirby.ithomereader.KEY_URL

import me.ikirby.ithomereader.ui.base.BaseActivity


class LinkJumpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var url = intent.dataString

        if (url != null) {
            url = url.replace("http://", "https://")
            val intent = Intent(this, ArticleActivity::class.java)
            intent.putExtra(KEY_URL, url)
            startActivity(intent)
        }

        finish()
    }
}
