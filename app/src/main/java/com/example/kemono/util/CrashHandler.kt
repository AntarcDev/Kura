package com.example.kemono.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        saveCrashReport(throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun saveCrashReport(throwable: Throwable) {
        val stackTrace = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTrace))
        val report = """
            Crash Report
            Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
            
            Stack Trace:
            $stackTrace
        """.trimIndent()

        try {
            val dir = File(context.filesDir, "crash_logs")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "crash.txt")
            val fos = FileOutputStream(file)
            fos.write(report.toByteArray())
            fos.close()
            Log.e("CrashHandler", "Crash report saved to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("CrashHandler", "Failed to save crash report", e)
        }
    }
}
