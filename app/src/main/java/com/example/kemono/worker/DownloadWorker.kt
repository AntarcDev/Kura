package com.example.kemono.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.kemono.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val fileUrl = inputData.getString(KEY_FILE_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "downloaded_file"

        createNotificationChannel()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $fileName")
            .setContentText("Download in progress")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, 0, true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(fileUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Failed to download file: ${response.code}")
                }

                val body = response.body ?: throw Exception("Response body is null")
                val inputStream: InputStream = body.byteStream()
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead: Long = 0
                val contentLength = body.contentLength()

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = (totalBytesRead * 100 / contentLength).toInt()
                        builder.setProgress(100, progress, false)
                        notificationManager.notify(NOTIFICATION_ID, builder.build())
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                builder.setContentText("Download complete")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                notificationManager.notify(NOTIFICATION_ID, builder.build())

                Result.success(workDataOf(KEY_FILE_PATH to file.absolutePath))
            } catch (e: Exception) {
                e.printStackTrace()
                builder.setContentText("Download failed")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                Result.failure()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Downloads"
            val descriptionText = "File downloads"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val KEY_FILE_URL = "key_file_url"
        const val KEY_FILE_NAME = "key_file_name"
        const val KEY_FILE_PATH = "key_file_path"
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1
    }
}
