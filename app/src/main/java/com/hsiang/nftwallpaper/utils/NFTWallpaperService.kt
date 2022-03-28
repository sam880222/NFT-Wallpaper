package com.hsiang.nftwallpaper.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.hsiang.nftwallpaper.network.AKASWAP_IPFS_BASEURL
import com.hsiang.nftwallpaper.network.NetworkManager.akaswapApi
import kotlinx.coroutines.*
import java.lang.Runnable
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

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
            setOffsetNotificationsEnabled(true)
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
            drawFrame()
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
                val tokenUrl = getLatestCreationUrl()
                bitmap = Glide.with(this@NFTWallpaperService)
                    .asBitmap()
                    .load(tokenUrl)
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

                drawFrame()
                handler.removeCallbacks(drawRunner)
                if (visible) {
//                    handler.postDelayed(drawRunner, 5000)
                }
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            val canvas = holder.lockCanvas()

            if (bitmap?.height != height) {
                bitmap = bitmap?.let {
                    Bitmap.createScaledBitmap(
                        it,
                        it.width * height / it.height,
                        height,
                        true
                    )
                }
            }

            bitmap?.let {
                canvas?.drawColor(Color.BLACK)
                canvas?.drawBitmap(
                    it, -offset * (it.width - width), 0f, null
                )
            }

            if (canvas != null)
                holder.unlockCanvasAndPost(canvas)
        }

        private suspend fun getLatestCreationUrl(): String? {
            val res = akaswapApi.getAccountCreations("tz1MpVZXooZ2M74ZCpZegU4VEEf9DwNdPPJL")
            return if (res.isSuccessful) {
                val url = res.body()?.tokens?.get(0)?.displayUri?.replace("ipfs://", AKASWAP_IPFS_BASEURL)
                Log.d(TAG, "Got token url $url")
                url
            } else {
                null
            }
        }
    }
}