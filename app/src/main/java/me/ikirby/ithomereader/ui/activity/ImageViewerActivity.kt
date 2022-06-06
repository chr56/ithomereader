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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.*
import me.ikirby.ithomereader.CLIP_TAG_IMAGE_LINK
import me.ikirby.ithomereader.KEY_URL
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.ActivityImageViewerBinding
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class ImageViewerActivity : AppCompatActivity(), View.OnClickListener, CoroutineScope {

    private lateinit var binding: ActivityImageViewerBinding

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

    private val saveImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                saveImage(it.data!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        url = intent.getStringExtra(KEY_URL) ?: ""

        binding.photoView.setOnClickListener(this)
        binding.imageMenuBtn.setOnClickListener(this)

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
        binding.loadTip.setOnClickListener(null)
        binding.loadTip.visibility = View.VISIBLE
        binding.loadText.visibility = View.GONE
        binding.loadProgress.visibility = View.VISIBLE
        Glide.with(this).load(url).transition(withCrossFade()).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                binding.loadProgress.visibility = View.GONE
                binding.loadProgress.visibility = View.VISIBLE
                binding.photoView.visibility = View.INVISIBLE
                binding.loadTip.setOnClickListener(this@ImageViewerActivity)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                binding.loadTip.visibility = View.GONE
                binding.photoView.visibility = View.VISIBLE
                binding.imageMenuBtn.visibility = View.VISIBLE
                return false
            }
        }).into(binding.photoView)
    }

    private fun createImageFile() {
        val fileName = getFileName(url)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = getImageMimeType(fileName)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        saveImage.launch(intent)
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
                    binding.imageMenuBtn.visibility = View.VISIBLE
                    binding.imageMenuBtn.startAnimation(fadeInAnim)
                } else {
                    window.decorView.systemUiVisibility = flagsFullscreen
                    binding.imageMenuBtn.visibility = View.GONE
                    binding.imageMenuBtn.startAnimation(fadeOutAnim)
                }
            }
            R.id.load_tip -> loadImage()
            R.id.image_menu_btn -> UiUtil.showBottomSheetMenu(
                this,
                object : BottomSheetMenu.BottomSheetMenuListener {
                    override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                        inflater.inflate(R.menu.menu_img_viewer, menu)
                    }

                    override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                        when (item.itemId) {
                            R.id.context_download_img -> createImageFile()
                            R.id.copy_link -> copyToClipboard(CLIP_TAG_IMAGE_LINK, url)
                        }
                    }
                }
            )
        }
    }
}
