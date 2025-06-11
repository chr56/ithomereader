package me.ikirby.ithomereader.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.CLIP_TAG_IMAGE_LINK
import me.ikirby.ithomereader.KEY_PAGE
import me.ikirby.ithomereader.KEY_URLS
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.ActivityImageViewerBinding
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.getFileName
import me.ikirby.ithomereader.util.getImageMimeType
import me.ikirby.ithomereader.util.writeFile
import java.io.IOException
import kotlin.math.abs

class ImageViewerActivity : AppCompatActivity() {

    companion object {
        fun intent(context: Context, urls: Collection<String>, selected: Int = -1): Intent =
            Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(KEY_URLS, ArrayList(urls))
                if (selected > -1) putExtra(KEY_PAGE, selected)
            }
    }

    private lateinit var binding: ActivityImageViewerBinding

    private var urls: List<String> = emptyList()
    private var current = -1

    private lateinit var fadeOutAnim: Animation
    private lateinit var fadeInAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        urls = intent.getStringArrayListExtra(KEY_URLS).orEmpty()
        current = intent.getIntExtra(KEY_PAGE, urls.lastIndex)

        fadeInAnim = AlphaAnimation(0F, 1F).apply {
            interpolator = DecelerateInterpolator()
            duration = 400
        }

        fadeOutAnim = AlphaAnimation(1F, 0F).apply {
            interpolator = AccelerateInterpolator()
            duration = 400
        }

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                binding.photoView.setOnClickListener {
                    WindowInsetsControllerCompat(window, view).hide(WindowInsetsCompat.Type.systemBars())
                    binding.imageMenuBtn.visibility = View.GONE
                    binding.imageMenuBtn.startAnimation(fadeOutAnim)
                }
            } else {
                binding.photoView.setOnClickListener {
                    WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
                    binding.imageMenuBtn.visibility = View.VISIBLE
                    binding.imageMenuBtn.startAnimation(fadeInAnim)
                }
            }
            windowInsets
        }

        setupGesture()

        binding.loadTip.setOnClickListener {
            loadImage(current) // retry
        }

        binding.imageMenuBtn.setOnClickListener {
            UiUtil.showBottomSheetMenu(
                this,
                object : BottomSheetMenu.BottomSheetMenuListener {
                    override fun onCreateBottomSheetMenu(inflater: MenuInflater, menu: Menu) {
                        inflater.inflate(R.menu.menu_img_viewer, menu)
                    }

                    override fun onBottomSheetMenuItemSelected(item: MenuItem) {
                        when (item.itemId) {
                            R.id.context_download_img -> downloadAndSaveCurrent()
                            R.id.copy_link -> copyToClipboard(CLIP_TAG_IMAGE_LINK, urls[current])
                            R.id.context_next -> next()
                            R.id.context_previous -> previous()
                        }
                    }
                }
            )
        }

        loadImage(current)
    }

    private fun previous() {
        current = (current - 1).mod(urls.size)
        loadImage(current)
    }

    private fun next() {
        current = (current + 1).mod(urls.size)
        loadImage(current)
    }


    private fun loadImage(position: Int) {
        binding.loadTip.setOnClickListener(null)
        binding.loadTip.visibility = View.VISIBLE
        binding.loadText.visibility = View.GONE
        binding.loadProgress.visibility = View.VISIBLE
        Coil.imageLoader(this).enqueue(
            ImageRequest.Builder(this)
                .data(urls[position])
                .target(object : coil.target.Target {
                    override fun onError(error: Drawable?) {
                        binding.loadProgress.visibility = View.GONE
                        binding.loadProgress.visibility = View.VISIBLE
                        binding.photoView.visibility = View.INVISIBLE
                        binding.loadTip.setOnClickListener {
                            loadImage(current) // retry
                        }
                    }

                    override fun onSuccess(result: Drawable) {
                        binding.photoView.setImageDrawable(result)
                        binding.loadTip.visibility = View.GONE
                        binding.photoView.visibility = View.VISIBLE
                        binding.imageMenuBtn.visibility = View.VISIBLE
                    }
                })
                .build()
        )
    }

    //region Save
    private fun downloadAndSaveCurrent() {
        val fileName = getFileName(urls[current])
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = getImageMimeType(fileName)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        saveImage.launch(intent)
    }

    private val saveImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let {
                    saveImage(it.data!!, current)
                }
            }
        }

    private fun saveImage(uri: Uri, position: Int) {
        lifecycleScope.launch(SupervisorJob()) {
            withContext(Dispatchers.IO) {
                Coil.imageLoader(this@ImageViewerActivity).enqueue(
                    ImageRequest.Builder(this@ImageViewerActivity)
                        .data(urls[position])
                        .target { drawable ->
                            try {
                                val stream =
                                    contentResolver.openOutputStream(uri)?.buffered(4096)!!
                                stream.use {
                                    writeFile(it, drawable)
                                }
                                launch(Dispatchers.Main) {
                                    MediaScannerConnection.scanFile(this@ImageViewerActivity, arrayOf(uri.toString()), null, null)
                                    ToastUtil.showToast(R.string.image_saved)
                                }
                            } catch (e: IOException) {
                                Logger.e("ImageViewerActivity", "saveImage", e)
                                launch(Dispatchers.Main) {
                                    ToastUtil.showToast(R.string.save_fail)
                                }
                            }
                        }
                        .build()
                )
            }
        }
    }
    //endregion

    //region Gestures
    private lateinit var gestureDetector: GestureDetector
    private fun setupGesture() {
        val distance = resources.displayMetrics.widthPixels / 2
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, v: Float, v1: Float): Boolean {
                if (urls.size > 1 && binding.photoView.scale == 1f) {
                    // switch among images, only when it's not zoomed
                    if (e1 != null && abs(e1.rawY - e2.rawY) < 75) {
                        // horizontal fling with threshold
                        val deltaX = e1.rawX - e2.rawX
                        if (deltaX > distance) next() else if (deltaX < -distance) previous()
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean =
        gestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev)
    //endregion
}
