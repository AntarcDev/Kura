package com.example.kemono

import org.junit.Test
import okhttp3.OkHttpClient
import okhttp3.Request

class LinkFetchTest {
    @Test
    fun fetchProfileAndPrintLinks() {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .addHeader("Referer", "https://kemono.cr/")
                    .build()
                chain.proceed(request)
            }
            .build()
            
        val request = Request.Builder()
            .url("https://kemono.cr/api/v1/patreon/user/112165758/profile")
            .build()
            
        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: "No body"
            println("RAW_JSON_START")
            println(body)
            println("RAW_JSON_END")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
