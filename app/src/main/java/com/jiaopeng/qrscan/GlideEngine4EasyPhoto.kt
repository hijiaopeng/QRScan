package com.jiaopeng.qrscan

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.engine.ImageEngine

/**
 * 描述：
 *
 * @author JiaoPeng by 1/18/21
 */
class GlideEngine4EasyPhoto : ImageEngine {

    companion object {
        val instance: GlideEngine4EasyPhoto by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            GlideEngine4EasyPhoto()
        }
    }

    override fun loadPhoto(context: Context, photoPath: String, imageView: ImageView) {
        Glide.with(context).load(photoPath).into(imageView)
    }

    override fun getCacheBitmap(context: Context, path: String, width: Int, height: Int): Bitmap {
        return Glide.with(context).asBitmap().load(path).submit(width, height).get()
    }

    override fun loadGif(context: Context, gifPath: String, imageView: ImageView) {
        Glide.with(context).asGif().load(gifPath).into(imageView)
    }

    override fun loadGifAsBitmap(context: Context, gifPath: String, imageView: ImageView) {
        Glide.with(context).asBitmap().load(gifPath).into(imageView)
    }

}