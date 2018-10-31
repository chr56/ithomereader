package me.ikirby.ithomereader.task

import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.BuildConfig
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.entity.UpdateInfo
import me.ikirby.ithomereader.ui.activity.DialogActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.util.Logger
import org.json.JSONObject
import org.jsoup.Jsoup

class UpdateCheckNotifyTask(private val showToast: Boolean) : AsyncTask<Void, Void, UpdateInfo>() {

    override fun doInBackground(vararg voids: Void): UpdateInfo? {
        val parameter = "?_=" + System.currentTimeMillis()
        var updateInfo: UpdateInfo? = null
        val updateUrl = BaseApplication.instance.packageManager
                .getApplicationInfo(BaseApplication.instance.packageName, PackageManager.GET_META_DATA)
                .metaData.getString("update_url")

        if (!updateUrl.isNullOrBlank()) {
            try {
                val json = Jsoup.connect(updateUrl + parameter)
                        .ignoreContentType(true)
                        .timeout(5000).execute().body()
                val info = JSONObject(json)
                val versionCode = info.getInt("versionCode")
                if (versionCode > BuildConfig.VERSION_CODE) {
                    updateInfo = UpdateInfo(
                            versionCode,
                            info.getString("version"),
                            info.getString("log"),
                            info.getString("url") + parameter
                    )
                }
            } catch (e: Exception) {
                Logger.e("UpdateCheckNotifyTask", "doInBackground", e)
            }
        }

        return updateInfo
    }

    override fun onPostExecute(updateInfo: UpdateInfo?) {
        val context = BaseApplication.instance
        if (updateInfo != null) {
            val intent = Intent(context, DialogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("updateInfo", updateInfo)
            }
            context.startActivity(intent)
        } else if (showToast) {
            ToastUtil.showToast(R.string.no_update_found)
        }
    }
}
