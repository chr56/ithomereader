package me.ikirby.ithomereader.module

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.PowerManager
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        if (!(context.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode) {
            builder.setDefaultTransitionOptions(Drawable::class.java, DrawableTransitionOptions.withCrossFade())
            builder.setDefaultTransitionOptions(Bitmap::class.java, BitmapTransitionOptions.withCrossFade())
        }
    }
}