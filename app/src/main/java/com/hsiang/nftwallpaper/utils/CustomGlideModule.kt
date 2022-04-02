package com.hsiang.nftwallpaper.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.hsiang.nftwallpaper.network.InternalSSLSocketFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import java.io.InputStream
import java.security.Security
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@GlideModule
class CustomGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val okHttpBuilder = OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(ConnectionSpec.RESTRICTED_TLS))
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)

        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        try {
            val tm: X509TrustManager = Conscrypt.getDefaultX509TrustManager()
            val sslContext: SSLContext = SSLContext.getInstance("TLS", "Conscrypt")
            sslContext.init(null, arrayOf<TrustManager>(tm), null)
            okHttpBuilder.sslSocketFactory(
                InternalSSLSocketFactory(sslContext.socketFactory),
                tm
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val factory = OkHttpUrlLoader.Factory(okHttpBuilder.build())
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}