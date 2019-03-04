package me.ikirby.ithomereader.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.ui.activity.ArticleActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil


fun openLink(context: Context, url: String) {
    if (canHandleURL(url)) {
        val intent = Intent(context, ArticleActivity::class.java).apply {
            putExtra("url", url)
        }
        context.startActivity(intent)
    } else {
        try {
            CustomTabsIntent.Builder()
                .setToolbarColor(UiUtil.getToolBarColor())
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            ToastUtil.showToast(R.string.no_app_to_handle_intent)
        }

    }
}

fun openLinkInBrowser(context: Context, url: String) {
    try {
        val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))
        val browseResolution = context.packageManager.resolveActivity(browseIntent, PackageManager.MATCH_DEFAULT_ONLY)

        val builder = CustomTabsIntent.Builder()
            .setToolbarColor(UiUtil.getToolBarColor())
            .setShowTitle(true)

        val customTabsIntent = builder.build()

        customTabsIntent.intent.component = ComponentName(
            browseResolution.activityInfo.applicationInfo.packageName,
            browseResolution.activityInfo.name
        )

        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        ToastUtil.showToast(R.string.no_app_to_handle_intent)
    }
}
