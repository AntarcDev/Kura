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
import androidx.work.ForegroundInfo
import android.content.pm.ServiceInfo
import com.example.kemono.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.documentfile.provider.DocumentFile
import android.net.Uri

import androidx.hilt.work.HiltWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.example.kemono.data.local.DownloadDao

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val downloadDao: DownloadDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val fileUrl = inputData.getString(KEY_FILE_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "downloaded_file"
        val targetUriString = inputData.getString(KEY_TARGET_URI)
        val subPath = inputData.getString(KEY_SUB_PATH)

        createNotificationChannel()
        
        // We re-create builder here for progress updates, but Initial notification is handled by getForegroundInfo
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $fileName")
            .setContentText("Starting...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, 0, true)

        // Promote to Foreground Service immediately
        setForeground(getForegroundInfo())

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
                
                // Determine Output Stream
                var finalUri: Uri? = null
                val outputStream = if (targetUriString != null) {
                    // SAF Logic
                    val targetUri = Uri.parse(targetUriString)
                    val rootDir = DocumentFile.fromTreeUri(applicationContext, targetUri) 
                        ?: throw Exception("Invalid target URI")
                    
                    var currentDir = rootDir
                    if (!subPath.isNullOrEmpty()) {
                        subPath.split("/").forEach { part ->
                            if (part.isNotEmpty()) {
                                val nextDir = currentDir.findFile(part) ?: currentDir.createDirectory(part)
                                currentDir = nextDir ?: throw Exception("Failed to create directory: $part")
                            }
                        }
                    }
                    
                    val file = currentDir.createFile(parseMimeType(fileName), fileName) 
                        ?: throw Exception("Failed to create file")
                    finalUri = file.uri
                    applicationContext.contentResolver.openOutputStream(file.uri)  
                        ?: throw Exception("Failed to open output stream")
                } else {
                    // Legacy Logic
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val finalDir = if (subPath != null) File(downloadsDir, subPath) else downloadsDir
                    if (!finalDir.exists()) finalDir.mkdirs()
                    
                    val file = File(finalDir, fileName)

                    finalUri = Uri.fromFile(file)
                    FileOutputStream(file)
                }

                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead: Long = 0
                val contentLength = body.contentLength()

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = (totalBytesRead * 100 / contentLength).toInt()
                         // Limit notification updates to avoid spam
                        if (totalBytesRead % (1024 * 1024) == 0L || totalBytesRead == contentLength) {
                            builder.setProgress(100, progress, false)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                setForeground(ForegroundInfo(NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC))
                            } else {
                                setForeground(ForegroundInfo(NOTIFICATION_ID, builder.build()))
                            }
                            setProgress(workDataOf("progress" to progress))
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                response.close()

                builder.setContentText("Download complete")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                notificationManager.notify(NOTIFICATION_ID, builder.build())

                if (finalUri != null) {
                    downloadDao.updateFilePath(id.toString(), finalUri.toString())
                }

                Result.success(workDataOf(KEY_FILE_PATH to (if (targetUriString != null) "Saved to custom location" else "Saved to Downloads")))
            } catch (e: Exception) {
                e.printStackTrace()
                builder.setContentText("Failed: ${e.message}")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                Result.failure()
            }
        }
    }

    private fun parseMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            fileName.endsWith(".gif", true) -> "image/gif"
            fileName.endsWith(".mp4", true) -> "video/mp4"
            fileName.endsWith(".zip", true) -> "application/zip"
            else -> "application/octet-stream"
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "downloading_file"
        createNotificationChannel()
        return createForegroundInfo(fileName)
    }

    private fun createForegroundInfo(fileName: String): ForegroundInfo {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $fileName")
            .setContentText("Starting...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, 0, true)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, builder.build())
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
        const val KEY_TARGET_URI = "key_target_uri"
        const val KEY_SUB_PATH = "key_sub_path"
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1
    }
}
