package com.hsiang.nftwallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.random.Random

class NFTWallpaperService : WallpaperService() {
    private val TAG = "NFTWallpaperService"
    override fun onCreateEngine(): Engine {
        Log.d(TAG, "onCreateEngine")
        return NFTWallpaperEngine()
    }

    inner class NFTWallpaperEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val drawRunner = Runnable { draw() }
        private var width: Int = 0
        private var height: Int = 0
        private var visible = true
        private val touchEnabled: Boolean
        private var i = 0
        private var offset = 0f
        private var bitmap: Bitmap? = null
        private val scope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = Job()
        }

        init {
            val prefs = PreferenceManager
                .getDefaultSharedPreferences(this@NFTWallpaperService)
            touchEnabled = prefs.getBoolean("touch", false)
            handler.post(drawRunner)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            this.visible = false
            handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int,
            width: Int, height: Int
        ) {
            this.width = width
            this.height = height
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            super.onOffsetsChanged(
                xOffset,
                yOffset,
                xOffsetStep,
                yOffsetStep,
                xPixelOffset,
                yPixelOffset
            )
            offset = xOffset
            Log.d(TAG, "$xOffsetStep")
            drawFrame()
        }

        private fun draw() {
            i = 1 - i
            scope.launch(Dispatchers.IO) {
                val bitmapTemp = Glide.with(this@NFTWallpaperService)
                    .asBitmap()
                    .load(if (i == 0) R.drawable.test0 else R.drawable.test1)
                    .submit()
                    .get()
//                    .override(this.width, this.height)
//                    .centerCrop()
//                    .into(object : CustomTarget<Bitmap>() {
//                        override fun onResourceReady(
//                            resource: Bitmap,
//                            transition: Transition<in Bitmap>?
//                        ) {
//                            canvas.drawBitmap(resource, 0f, 0f, null)
//                        }
//
//                        override fun onLoadCleared(placeholder: Drawable?) {}
//                    })
                bitmap = Bitmap.createScaledBitmap(
                    bitmapTemp,
                    bitmapTemp.width * height / bitmapTemp.height,
                    height,
                    true
                )
                drawFrame()
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            val canvas = holder.lockCanvas()

            bitmap?.let {
                canvas?.drawColor(Color.BLACK)
                canvas?.drawBitmap(
                    it, -offset * abs(it.width - width), 0f, null
                )
            }

            if (canvas != null)
                holder.unlockCanvasAndPost(canvas)

            handler.removeCallbacks(drawRunner)
            if (visible) {
                handler.postDelayed(drawRunner, 5000)
            }
        }
    }
}