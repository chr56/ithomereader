package me.ikirby.ithomereader.ui.activity

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_image_viewer.*
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.STORAGE_PERMISSION_REQUEST_CODE
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.copyToClipboard
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutionException

class ImageViewerActivity : Activity(), View.OnClickListener, View.OnLongClickListener {

    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        url = intent.getStringExtra("url")
        if (url == null) {
            finish()
            return
        }

        photo_view.setOnClickListener(this)
        photo_view.setOnLongClickListener(this)

        loadImage()
    }

    private fun loadImage() {
        load_tip.setOnClickListener(null)
        load_tip.visibility = View.VISIBLE
        load_text.visibility = View.GONE
        load_progress.visibility = View.VISIBLE
        Glide.with(this).load(url).transition(withCrossFade()).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                load_progress.visibility = View.GONE
                load_progress.visibility = View.VISIBLE
                photo_view.visibility = View.INVISIBLE
                load_tip.setOnClickListener(this@ImageViewerActivity)
                return false
            }

            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                load_tip.visibility = View.GONE
                photo_view.visibility = View.VISIBLE
                return false
            }
        }).into(photo_view)
    }

    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadFile()
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun downloadFile() = Thread(Runnable {
        val target = Glide.with(this@ImageViewerActivity).downloadOnly().load(url).submit()
        try {
            val path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                    + "/ITHome/" + URLUtil.guessFileName(url, "", "image/*"))
            val fileName: String
            fileName = if (path.contains("@")) {
                path.substring(0, path.indexOf("@"))
            } else {
                path
            }
            val file = File(fileName)
            if (!file.parentFile.exists()) {

                file.parentFile.mkdirs()
            }
            if (file.exists()) {
                runOnUiThread { ToastUtil.showToast(R.string.file_exists) }
                return@Runnable
            }
            val inputStream = FileInputStream(target.get())
            val outputStream = FileOutputStream(fileName)
            val buffer = ByteArray(1024)
            var length: Int
            while (true) {
                length = inputStream.read(buffer)
                if (length > 0) {
                    outputStream.write(buffer, 0, length)
                } else {
                    break
                }
            }
            inputStream.close()
            outputStream.close()
            MediaScannerConnection.scanFile(this@ImageViewerActivity, arrayOf(fileName), arrayOf("image/*"), null)
            runOnUiThread { ToastUtil.showToast(getString(R.string.image_saved_to) + fileName) }
        } catch (e: IOException) {
            runOnUiThread { ToastUtil.showToast(R.string.save_fail) }
            e.printStackTrace()
        } catch (e: InterruptedException) {
            runOnUiThread { ToastUtil.showToast(R.string.save_fail) }
            e.printStackTrace()
        } catch (e: ExecutionException) {
            runOnUiThread { ToastUtil.showToast(R.string.save_fail) }
            e.printStackTrace()
        }
    }).start()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile()
            } else {
                ToastUtil.showToast(R.string.permission_denied)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.photo_view -> finish()
            R.id.load_tip -> loadImage()
        }
    }

    override fun onLongClick(view: View): Boolean {
        UiUtil.showBottomSheetMenu(this, object : BottomSheetMenu.BottomSheetMenuListener {
            override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                inflater.inflate(R.menu.menu_img_viewer, menu)
            }

            override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                when (item.itemId) {
                    R.id.context_download_img -> checkPermission()
                    R.id.copy_link -> copyToClipboard("ITHomeImageLink", url!!)
                }
            }
        })
        return true
    }
}
