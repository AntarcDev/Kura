package com.example.kemono.data.repository

import android.content.Context
import com.example.kemono.data.model.GithubRelease
import com.example.kemono.data.remote.GithubApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val api: GithubApi,
    @ApplicationContext private val context: Context
) {

    suspend fun checkForUpdate(currentVersionName: String): Result<GithubRelease?> {
        return try {
            val release = api.getLatestRelease()
            val remoteVersion = release.tagName.removePrefix("v")
            
            if (compareVersions(remoteVersion, currentVersionName) > 0) {
                Result.success(release)
            } else {
                Result.success(null)
            }
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) {
                Result.success(null)
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun downloadApk(url: String): Flow<Float> = flow {
        android.util.Log.d("UpdateRepository", "Starting download from: $url")
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        
        try {
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                android.util.Log.e("UpdateRepository", "Download failed. Code: ${response.code}, Message: ${response.message}")
                throw Exception("Failed to download APK: HTTP ${response.code}")
            }

            val body = response.body ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            val inputStream = body.byteStream()
            
            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) updateDir.mkdirs()
            
            val file = File(updateDir, "update.apk")
            val outputStream = FileOutputStream(file)
            
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            var totalBytesRead = 0L
            
            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        emit(totalBytesRead.toFloat() / contentLength)
                    }
                }
                outputStream.flush()
                android.util.Log.d("UpdateRepository", "Download complete. File size: ${file.length()}")
            } finally {
                inputStream.close()
                outputStream.close()
                body.close()
            }
        } catch (e: Exception) {
            android.util.Log.e("UpdateRepository", "Download exception", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    fun getApkFile(): File {
        return File(context.cacheDir, "updates/update.apk")
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val length = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until length) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}
