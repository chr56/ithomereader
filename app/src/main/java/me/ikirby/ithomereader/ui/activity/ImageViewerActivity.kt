package me.ikirby.ithomereader.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
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
import me.ikirby.ithomereader.SAF_CREATE_REQUEST_CODE
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.*
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

        url = intent.getStringExtra(KEY_URL) ?: ""

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SAF_CREATE_REQUEST_CODE && data != null) {
                saveImage(data.data!!)
            }
        }
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

    private fun createImageFile() {
        val fileName = getFileName(url)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = getImageMimeType(fileName)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        startActivityForResult(intent, SAF_CREATE_REQUEST_CODE)
    }

    private fun saveImage(uri: Uri) {
        launch {
            withContext(Dispatchers.IO) {
                val target = Glide.with(this@ImageViewerActivity).downloadOnly().load(url).submit()
                try {
                    writeFile(contentResolver.openOutputStream(uri)!!, target.get())
                    withContext(Dispatchers.Main) {
                        MediaScannerConnection.scanFile(this@ImageViewerActivity, arrayOf(uri.toString()), null, null)
                        ToastUtil.showToast(R.string.image_saved)
                    }
                } catch (e: IOException) {
                    Logger.e("ImageViewerActivity", "saveImage", e)
                    withContext(Dispatchers.Main) {
                        ToastUtil.showToast(R.string.save_fail)
                    }
                }
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
                        R.id.context_download_img -> createImageFile()
                        R.id.copy_link -> copyToClipboard(CLIP_TAG_IMAGE_LINK, url)
                    }
                }
            })
        }
    }
}
