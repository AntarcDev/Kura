package com.example.kemono.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupRestoreUtils {

    private const val DB_NAME = "kemono_db"
    private const val DATASTORE_NAME = "settings.preferences_pb"

    suspend fun createBackup(context: Context, outputUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            resolver.openOutputStream(outputUri)?.use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // 1. Database Files
                    val dbFile = context.getDatabasePath(DB_NAME)
                    val dbWalFile = context.getDatabasePath("$DB_NAME-wal")
                    val dbShmFile = context.getDatabasePath("$DB_NAME-shm")

                    listOf(dbFile, dbWalFile, dbShmFile).forEach { file ->
                        if (file.exists()) {
                            addToZip(file, file.name, zos)
                        }
                    }

                    // 2. DataStore (Settings)
                    val dataStoreFile = File(context.filesDir, "datastore/$DATASTORE_NAME")
                    if (dataStoreFile.exists()) {
                        addToZip(dataStoreFile, DATASTORE_NAME, zos)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(context: Context, inputUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            resolver.openInputStream(inputUri)?.use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val fileName = entry.name
                        val targetFile = when (fileName) {
                            DB_NAME, "$DB_NAME-wal", "$DB_NAME-shm" -> context.getDatabasePath(fileName)
                            DATASTORE_NAME -> {
                                val dir = File(context.filesDir, "datastore")
                                if (!dir.exists()) dir.mkdirs()
                                File(dir, DATASTORE_NAME)
                            }
                            else -> null
                        }

                        targetFile?.let { file ->
                            FileOutputStream(file).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addToZip(file: File, entryName: String, zos: ZipOutputStream) {
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry(entryName)
            zos.putNextEntry(zipEntry)
            fis.copyTo(zos)
            zos.closeEntry()
        }
    }
}
