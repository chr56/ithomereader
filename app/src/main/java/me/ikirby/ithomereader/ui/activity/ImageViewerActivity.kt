package me.ikirby.ithomereader.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_image_viewer.*
import kotlinx.coroutines.*
import me.ikirby.ithomereader.CLIP_TAG_IMAGE_LINK
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.STORAGE_PERMISSION_REQUEST_CODE
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.getFullPath
import me.ikirby.ithomereader.util.writeFile
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class ImageViewerActivity : AppCompatActivity(), View.OnClickListener, CoroutineScope {

    companion object {
        const val flagsFullscreen = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var url: String

    private lateinit var fadeOutAnim: Animation
    private lateinit var fadeInAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        url = intent.getStringExtra(KEY_URL)

        photo_view.setOnClickListener(this)
        image_menu_btn.setOnClickListener(this)

        fadeOutAnim = AlphaAnimation(1F, 0F)
        fadeOutAnim.interpolator = AccelerateInterpolator()
        fadeOutAnim.duration = 400

        fadeInAnim = AlphaAnimation(0F, 1F)
        fadeInAnim.interpolator = DecelerateInterpolator()
        fadeInAnim.duration = 400

        loadImage()
    }

    override fun onDestroy() {
        coroutineContext.cancelChildren()
        super.onDestroy()
    }

    private fun loadImage() {
        load_tip.setOnClickListener(null)
        load_tip.visibility = View.VISIBLE
        load_text.visibility = View.GONE
        load_progress.visibility = View.VISIBLE
        Glide.with(this).load(url).transition(withCrossFade()).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                load_progress.visibility = View.GONE
                load_progress.visibility = View.VISIBLE
                photo_view.visibility = View.INVISIBLE
                load_tip.setOnClickListener(this@ImageViewerActivity)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                load_tip.visibility = View.GONE
                photo_view.visibility = View.VISIBLE
                image_menu_btn.visibility = View.VISIBLE
                return false
            }
        }).into(photo_view)
    }

    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveImage()
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun saveImage() {
        launch {
            withContext(Dispatchers.IO) {
                val target = Glide.with(this@ImageViewerActivity).downloadOnly().load(url).submit()
                val pathname = getFullPath(url)
                try {
                    val file = File(pathname)
                    file.parentFile.mkdirs()
                    if (file.exists()) {
                        withContext(Dispatchers.Main) { ToastUtil.showToast(R.string.file_exists) }
                        return@withContext
                    }
                    writeFile(pathname, target.get())
                    MediaScannerConnection.scanFile(
                        this@ImageViewerActivity,
                        arrayOf(pathname),
                        arrayOf("image/*"),
                        null
                    )
                    withContext(Dispatchers.Main) {
                        ToastUtil.showToast(getString(R.string.image_saved_to) + pathname)
                    }
                } catch (e: IOException) {
                    Logger.e("ImageViewerActivity", "saveImage", e)
                    withContext(Dispatchers.Main) { ToastUtil.showToast(R.string.save_fail) }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                ToastUtil.showToast(R.string.permission_denied)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.photo_view -> {
                if (window.decorView.systemUiVisibility == flagsFullscreen) {
                    window.decorView.systemUiVisibility = 0
                    image_menu_btn.visibility = View.VISIBLE
                    image_menu_btn.startAnimation(fadeInAnim)
                } else {
                    window.decorView.systemUiVisibility = flagsFullscreen
                    image_menu_btn.visibility = View.GONE
                    image_menu_btn.startAnimation(fadeOutAnim)
                }
            }
            R.id.load_tip -> loadImage()
            R.id.image_menu_btn -> UiUtil.showBottomSheetMenu(this, object : BottomSheetMenu.BottomSheetMenuListener {
                override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                    inflater.inflate(R.menu.menu_img_viewer, menu)
                }

                override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                    when (item.itemId) {
                        R.id.context_download_img -> checkPermission()
                        R.id.copy_link -> copyToClipboard(CLIP_TAG_IMAGE_LINK, url)
                    }
                }
            })
        }
    }
}
