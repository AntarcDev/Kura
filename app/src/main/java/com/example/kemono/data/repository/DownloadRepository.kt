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

                val downloadId = downloadManager.enqueue(request)

                val item =
                        DownloadedItem(
                                postId = postId,
                                creatorId = creatorId,
                                fileName = fileName,
                                filePath =
                                        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/Kemono/$fileName",
                                mediaType = mediaType,
                                downloadId = downloadId
                        )
                downloadDao.insert(item)
        }

        fun getAllDownloadedItems() = downloadDao.getAllDownloadedItems()

        suspend fun deleteDownloadedItem(item: DownloadedItem) {
                downloadDao.delete(item)
        }

        fun getDownloadStatus(id: Long): DownloadStatus {
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                        val status =
                                cursor.getInt(
                                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                                )
                        val bytesDownloaded =
                                cursor.getLong(
                                        cursor.getColumnIndexOrThrow(
                                                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                                        )
                                )
                        val bytesTotal =
                                cursor.getLong(
                                        cursor.getColumnIndexOrThrow(
                                                DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                                        )
                                )
                        val reason =
                                cursor.getInt(
                                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)
                                )
                        cursor.close()

                        val progress =
                                if (bytesTotal > 0) bytesDownloaded.toFloat() / bytesTotal.toFloat()
                                else 0f
                        return DownloadStatus(id, status, progress, reason)
                }
                cursor.close()
                return DownloadStatus(id, DownloadManager.STATUS_FAILED, 0f, 0)
        }

        fun cancelDownload(id: Long) {
                downloadManager.remove(id)
        }
}

data class DownloadStatus(val id: Long, val status: Int, val progress: Float, val reason: Int)
