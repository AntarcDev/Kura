package com.example.kemono.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.kemono.data.local.DownloadDao
import com.example.kemono.data.model.DownloadedItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val downloadDao: DownloadDao
) {
    private val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun downloadFile(
            url: String,
            fileName: String,
            postId: String,
            creatorId: String,
            mediaType: String
    ) {
        val request =
                DownloadManager.Request(Uri.parse(url))
                        .setTitle(fileName)
                        .setDescription("Downloading media from Kemono")
                        .setNotificationVisibility(
                                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                "Kemono/$fileName"
                        )
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

        downloadManager.enqueue(request)

        // Track in database
        // Note: Ideally we should wait for completion, but for simplicity we track the intent to
        // download.
        // A more robust solution would use a BroadcastReceiver to listen for
        // ACTION_DOWNLOAD_COMPLETE.
        val item =
                DownloadedItem(
                        postId = postId,
                        creatorId = creatorId,
                        fileName = fileName,
                        filePath = "${Environment.DIRECTORY_DOWNLOADS}/Kemono/$fileName",
                        mediaType = mediaType
                )
        downloadDao.insert(item)
    }

    fun getAllDownloadedItems() = downloadDao.getAllDownloadedItems()
}
