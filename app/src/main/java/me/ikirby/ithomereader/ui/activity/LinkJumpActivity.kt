package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle

import me.ikirby.ithomereader.ui.base.BaseActivity


class LinkJumpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var url = intent.dataString

        if (url != null) {
            url = url.replace("http://", "https://")
            val intent = Intent(this, ArticleActivity::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
        }

        finish()
    }
}
