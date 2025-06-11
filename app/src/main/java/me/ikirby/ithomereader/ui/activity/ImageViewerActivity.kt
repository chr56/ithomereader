package me.ikirby.ithomereader.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.Coil
import coil.request.ImageRequest
import coil.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.CLIP_TAG_IMAGE_LINK
import me.ikirby.ithomereader.KEY_PAGE
import me.ikirby.ithomereader.KEY_URLS
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.ActivityImageViewerBinding
import me.ikirby.ithomereader.databinding.FragmentImageViewerBinding
import me.ikirby.ithomereader.ui.dialog.BottomSheetMenu
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.Logger
import me.ikirby.ithomereader.util.copyToClipboard
import me.ikirby.ithomereader.util.getFileName
import me.ikirby.ithomereader.util.getImageMimeType
import me.ikirby.ithomereader.util.writeFile
import java.io.IOException


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
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            SystemBarStyle.dark(Color.BLACK)
        )
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

        setupViewPager()
        setupWindowsInsets()

        binding.imageMenuBtn.setOnClickListener { showImageMenu() }

        // Initial load
        binding.viewPager.setCurrentItem(current, false)
        updateCountIndicator(current)
    }

    private fun setupWindowsInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.countIndicator.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }
            binding.imageMenuBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            hidden = !windowInsets.isVisible(WindowInsetsCompat.Type.systemBars())
            windowInsets
        }
    }

    class ImageAdapter(
        private val urls: List<String>, val onClick: () -> Unit
    ) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding: FragmentImageViewerBinding =
                FragmentImageViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(position, urls) { onClick }
        }

        override fun getItemCount(): Int = urls.size

        class ViewHolder(val binding: FragmentImageViewerBinding) : RecyclerView.ViewHolder(binding.root) {

            fun onBind(position: Int, urls: List<String>, onClick: () -> Unit) {
                val context = itemView.context
                val url = urls[position]
                loadImages(context, url)
                binding.photoView.setOnLongClickListener { onClick();true }
            }

            private fun loadImages(context: Context, url: String) {
                Coil.imageLoader(context).enqueue(
                    ImageRequest.Builder(context)
                        .data(url)
                        .target(object : Target {
                            override fun onStart(placeholder: Drawable?) {
                                binding.photoView.visibility = View.INVISIBLE
                                binding.loadProgress.visibility = View.VISIBLE
                                binding.loadText.visibility = View.GONE
                                binding.loadTip.visibility = View.VISIBLE
                                binding.loadTip.setOnClickListener(null)
                            }

                            override fun onError(error: Drawable?) {
                                binding.photoView.visibility = View.INVISIBLE
                                binding.loadProgress.visibility = View.GONE
                                binding.loadText.visibility = View.VISIBLE
                                binding.loadTip.setOnClickListener {
                                    loadImages(context, url) // retry
                                }
                            }

                            override fun onSuccess(result: Drawable) {
                                binding.photoView.visibility = View.VISIBLE
                                binding.loadProgress.visibility = View.GONE
                                binding.photoView.setImageDrawable(result)
                                binding.loadTip.visibility = View.GONE
                            }
                        })
                        .build()
                )
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = ImageAdapter(urls, ::toggleButtons)

        binding.viewPager.offscreenPageLimit = 1 // Keep one page on each side loaded
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                current = position
                updateCountIndicator(position)
            }
        })
    }

    private var hidden = false

    private fun hideButtons() {
        binding.imageMenuBtn.startAnimation(fadeOutAnim)
        binding.imageMenuBtn.visibility = View.GONE
        binding.countIndicator.startAnimation(fadeOutAnim)
        binding.countIndicator.visibility = View.GONE
        WindowInsetsControllerCompat(window, binding.root).hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun showButtons() {
        binding.imageMenuBtn.startAnimation(fadeInAnim)
        binding.imageMenuBtn.visibility = View.VISIBLE
        binding.countIndicator.startAnimation(fadeInAnim)
        binding.countIndicator.visibility = View.VISIBLE
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun toggleButtons() {
        if (hidden) showButtons() else hideButtons()
    }

    private fun showImageMenu() {
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
                        R.id.context_next -> binding.viewPager.currentItem = (current + 1).mod(urls.size)
                        R.id.context_previous -> binding.viewPager.currentItem = (current - 1).mod(urls.size)
                    }
                }
            }
        )
    }


    private fun updateCountIndicator(position: Int) {
        if (urls.size > 1) {
            @SuppressLint("SetTextI18n")
            binding.countIndicator.text = "${position + 1} / ${urls.size}"
            binding.countIndicator.visibility = View.VISIBLE
        } else {
            binding.countIndicator.visibility = View.GONE
        }
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
}