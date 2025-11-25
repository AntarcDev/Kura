package com.example.kemono.di

import android.content.Context
import com.example.kemono.data.local.SessionManager
import com.example.kemono.data.remote.KemonoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://kemono.cr/api/v1/"

    @Provides
    @Singleton
    fun provideCookieJar(
            @ApplicationContext context: Context,
            sessionManager: SessionManager
    ): CookieJar {
        return object : CookieJar {
            private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
            private val prefs = context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)

            init {
                // Load persisted cookies
                loadCookies()
            }

            private fun loadCookies() {
                val cookieStrings = prefs.getStringSet("cookies", emptySet()) ?: emptySet()
                cookieStrings.forEach { cookieString ->
                    try {
                        val parts = cookieString.split("|")
                        if (parts.size >= 3) {
                            val domain = parts[0]
                            val name = parts[1]
                            val value = parts[2]
                            val cookie =
                                    Cookie.Builder().name(name).value(value).domain(domain).build()
                            cookieStore.getOrPut(domain) { mutableListOf() }.add(cookie)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            private fun saveCookies() {
                val cookieStrings = mutableSetOf<String>()
                cookieStore.forEach { (domain, cookies) ->
                    cookies.forEach { cookie ->
                        cookieStrings.add("$domain|${cookie.name}|${cookie.value}")
                    }
                }
                prefs.edit().putStringSet("cookies", cookieStrings).apply()
            }

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val host = url.host
                val existingCookies = cookieStore.getOrPut(host) { mutableListOf() }

                cookies.forEach { newCookie ->
                    // Remove old cookie with same name
                    existingCookies.removeAll { it.name == newCookie.name }
                    // Add new cookie
                    existingCookies.add(newCookie)
                }

                saveCookies()
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = mutableListOf<Cookie>()
                val host = url.host

                // Add stored cookies for this domain
                cookieStore[host]?.let { cookies.addAll(it) }

                // Add session cookie from user input
                sessionManager.getSessionCookie()?.let { cookieString ->
                    cookieString.split(";").forEach { pair ->
                        val trimmed = pair.trim()
                        if (trimmed.isNotEmpty()) {
                            val parts = trimmed.split("=", limit = 2)
                            if (parts.size == 2) {
                                val cookie =
                                        Cookie.Builder()
                                                .name(parts[0].trim())
                                                .value(parts[1].trim())
                                                .domain(host)
                                                .build()
                                cookies.add(cookie)
                            }
                        }
                    }
                }

                return cookies
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        return OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val requestBuilder =
                            chain.request()
                                    .newBuilder()
                                    .addHeader(
                                            "User-Agent",
                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                    )
                                    .addHeader(
                                            "Accept",
                                            "text/css"
                                    ) // Required by kemono.cr to bypass DDoS-Guard
                                    .addHeader("Accept-Language", "en-US,en;q=0.9")
                                    .addHeader("Referer", "https://kemono.cr/")
                                    .addHeader("Origin", "https://kemono.cr")
                                    .addHeader("DNT", "1")
                                    .addHeader("Connection", "keep-alive")
                                    .addHeader("Sec-Fetch-Dest", "empty")
                                    .addHeader("Sec-Fetch-Mode", "cors")
                                    .addHeader("Sec-Fetch-Site", "same-origin")

                    val response = chain.proceed(requestBuilder.build())

                    // Fix Content-Type: server returns text/css but it's actually JSON
                    if (response.header("Content-Type")?.contains("text/css") == true) {
                        response.newBuilder().header("Content-Type", "application/json").build()
                    } else {
                        response
                    }
                }
                .addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
                .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @Singleton
    fun provideKemonoApi(retrofit: Retrofit): KemonoApi {
        return retrofit.create(KemonoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): coil.ImageLoader {
        return coil.ImageLoader.Builder(context)
                .components {
                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                        add(coil.decode.ImageDecoderDecoder.Factory())
                    } else {
                        add(coil.decode.GifDecoder.Factory())
                    }
                }
                .diskCache {
                    coil.disk.DiskCache.Builder()
                            .directory(context.cacheDir.resolve("image_cache"))
                            .maxSizeBytes(100 * 1024 * 1024) // 100 MB
                            .build()
                }
                .build()
    }
}
