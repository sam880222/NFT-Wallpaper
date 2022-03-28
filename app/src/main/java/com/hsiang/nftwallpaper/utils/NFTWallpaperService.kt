package com.hsiang.nftwallpaper.utils

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.hsiang.nftwallpaper.R
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

        //        private val drawRunner = Runnable { draw() }
        private val checkRunner = Runnable { checkToken() }
        private var width: Int = 0
        private var height: Int = 0
        private var visible = true
        private var accountToken: String? = ""
        private var offset = 0f
        private var bitmap: Bitmap? = null
        private var url: String?
        private val prefs: SharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(this@NFTWallpaperService)
        private val scope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = Job()
        }

        init {
            updateToken()
//            handler.post(drawRunner)
            handler.post(checkRunner)
            setOffsetNotificationsEnabled(true)
            url = prefs.getString("tokenUrl", "")
        }

        private fun updateToken() {
            accountToken = prefs.getString("accountToken", "")
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(checkRunner)
            }
//            if (visible) {
//                handler.post(drawRunner)
//            } else {
//                handler.removeCallbacks(drawRunner)
//            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            this.visible = false
//            handler.removeCallbacks(drawRunner)
            handler.removeCallbacks(checkRunner)
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
            drawFrame()
        }

        private fun checkToken() {
            updateToken()
            scope.launch(Dispatchers.IO) {
                val tokenUrl = getLatestCreationUrl()
                if (tokenUrl?.equals(url) == false) {
                    url = tokenUrl
                    prefs.edit()?.putString("tokenUrl", tokenUrl)?.apply()
                    draw()
                } else if (bitmap == null) {
                    draw()
                }
                handler.removeCallbacks(checkRunner)
                handler.postDelayed(checkRunner, 30000)
            }
        }

        private fun draw() {
            scope.launch(Dispatchers.IO) {
                try {
                    bitmap = Glide.with(this@NFTWallpaperService)
                        .asBitmap()
                        .load(if (url.isNullOrEmpty()) R.drawable.default_bg else url)
                        .placeholder(R.drawable.default_bg)
                        .submit()
                        .get()
                } catch (e: GlideException) {
                    e.logRootCauses(TAG)
                }
                drawFrame()
//                handler.removeCallbacks(drawRunner)
//                if (visible) {
//                    handler.postDelayed(drawRunner, 5000)
//                }
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
                val leftPadding =
                    (if (isPreview) -0.5f * (it.width - width) else -offset * (it.width - width))
                canvas?.drawBitmap(
                    it, leftPadding, 0f, null
                )
            }

            if (canvas != null)
                holder.unlockCanvasAndPost(canvas)
        }

        private suspend fun getLatestCreationUrl(): String? {
            if (!Utility.isNetworkAvailable(applicationContext)) {
                Log.i(TAG, "No network connection!")
                return null
            }
            if (accountToken.isNullOrEmpty()) {
                Log.i(TAG, "No accountToken!")
                return null
            }
            val res = akaswapApi.getAccountCreations(
                accountToken!!,
                null
            )
            return if (res.isSuccessful) {
                val body = res.body()
                if (body?.count ?: 0 < 1) {
                    return null
                }
                val url = body?.tokens?.get(0)?.displayUri?.replace("ipfs://", AKASWAP_IPFS_BASEURL)
                Log.d(TAG, "Got token url $url")
                url
            } else {
                null
            }
        }
    }
}