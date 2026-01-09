package com.example.kemono

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration

@HiltAndroidApp
class KemonoApp : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject lateinit var imageLoader: ImageLoader
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        val crashHandler = com.example.kemono.util.CrashHandler(this)
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
    }
}
