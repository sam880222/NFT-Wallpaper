package com.hsiang.nftwallpaper

import android.app.Application
import android.content.Context
import org.conscrypt.Conscrypt
import java.security.Security

class MyApplication : Application() {
    companion object {
        private lateinit var instance: MyApplication

        fun getContext(): Context {
            return instance.applicationContext
        }
    }

    init {
        instance = this
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
}