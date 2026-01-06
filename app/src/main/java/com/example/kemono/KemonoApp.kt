package com.example.kemono

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KemonoApp : Application(), ImageLoaderFactory {

    @Inject lateinit var imageLoader: ImageLoader

    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }

    override fun onCreate() {
        super.onCreate()
        val crashHandler = com.example.kemono.util.CrashHandler(this)
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
    }
}
