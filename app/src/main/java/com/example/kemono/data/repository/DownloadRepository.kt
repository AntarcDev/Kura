package com.example.kemono.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.OutOfQuotaPolicy
import androidx.work.workDataOf
import com.example.kemono.data.local.DownloadDao
import com.example.kemono.data.model.DownloadedItem
import com.example.kemono.worker.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val settingsRepository: SettingsRepository
) {
    private val workManager = WorkManager.getInstance(context)

    suspend fun downloadFile(
        url: String,
        fileName: String,
        postId: String,
        postTitle: String,
        creatorId: String,
        creatorName: String,
        mediaType: String,
        subFolder: String? = null
    ) {
        val downloadLocation = settingsRepository.downloadLocation.first()
        
        val sanitizedCreator = sanitizeFileName(creatorName)
        val sanitizedPost = sanitizeFileName(postTitle)
        val sanitizedFile = sanitizeFileName(fileName)

        val subPath = if (subFolder != null) {
            "Kemono/$sanitizedCreator/${sanitizeFileName(subFolder)}/$sanitizedFile"
        } else {
            "Kemono/$sanitizedCreator/$sanitizedPost/$sanitizedFile" // Logic might need adjustment for SAF relative path
        }
        
        // For SAF, subPath should be directory structure relative to root, not including filename usually if we want to organize.
        // DownloadWorker logic expects subPath to be folders: "Kemono/Creator/Post"
        // Let's adjust subPath passing.
        
        val folderPath = if (subFolder != null) {
            "Kemono/$sanitizedCreator/${sanitizeFileName(subFolder)}"
        } else {
            "Kemono/$sanitizedCreator/$sanitizedPost"
        }

        val workData = workDataOf(
            DownloadWorker.KEY_FILE_URL to url,
            DownloadWorker.KEY_FILE_NAME to sanitizedFile,
            DownloadWorker.KEY_TARGET_URI to downloadLocation,
            DownloadWorker.KEY_SUB_PATH to folderPath
        )

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workData)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("download")
            .build()

        workManager.enqueue(workRequest)

        val item = DownloadedItem(
            postId = postId,
            creatorId = creatorId,
            creatorName = creatorName,
            fileName = fileName,
            filePath = if (downloadLocation != null) "Custom: $sanitizedFile" else "Downloads/$folderPath/$sanitizedFile", 
            mediaType = mediaType,
            downloadId = workRequest.id.toString()
        )
        downloadDao.insert(item)
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    fun getAllDownloadedItems() = downloadDao.getAllDownloadedItems()

    suspend fun deleteDownloadedItem(item: DownloadedItem) {
        downloadDao.delete(item)
    }

    suspend fun getDownloadStatus(id: String): DownloadStatus {
        try {
            val uuid = UUID.fromString(id)
            val workInfo = workManager.getWorkInfoById(uuid).get() // This is blocking, but simpler for now. Or use flow?
            // Wait, get() is Future.get(). In suspend function, use await() or standard loop?
            // WorkManager allows `getWorkInfoByIdFlow` or `getWorkInfoById(uuid)` returning ListenableFuture.
            // For one-shot check, future.get() is usually okay if wrapped in Dispatchers.IO?
            // Actually, existing implementation was synchronous (Cursor).
            
            if (workInfo != null) {
                 val progressInt = workInfo.progress.getInt("progress", 0)
                 val progress = progressInt / 100f
                 
                 val status = when (workInfo.state) {
                     WorkInfo.State.SUCCEEDED -> 8 // DownloadManager.STATUS_SUCCESSFUL
                     WorkInfo.State.FAILED -> 16 // STATUS_FAILED
                     WorkInfo.State.RUNNING -> 2 // STATUS_RUNNING
                     WorkInfo.State.ENQUEUED -> 1 // STATUS_PENDING
                     else -> 0
                 }
                 
                 return DownloadStatus(id, status, progress, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return DownloadStatus(id, 16, 0f, 0)
    }

    fun cancelDownload(id: String) {
        try {
            workManager.cancelWorkById(UUID.fromString(id))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class DownloadStatus(val id: String, val status: Int, val progress: Float, val reason: Int)
