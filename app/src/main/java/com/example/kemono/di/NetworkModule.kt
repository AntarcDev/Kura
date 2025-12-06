package com.example.kemono.di

import android.content.Context
import com.example.kemono.data.local.SessionManager
import com.example.kemono.data.remote.KemonoApi
import com.example.kemono.data.remote.GithubApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
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
    private const val GITHUB_API_URL = "https://api.github.com/"

    @Provides
    @Singleton
    fun provideCookieJar(
            @ApplicationContext context: Context,
            sessionManager: SessionManager
    ): CookieJar {
        return object : CookieJar {
            private val cookieStore = java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.CopyOnWriteArrayList<Cookie>>()
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
                            cookieStore.getOrPut(domain) { java.util.concurrent.CopyOnWriteArrayList() }.add(cookie)
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
                val existingCookies = cookieStore.getOrPut(host) { java.util.concurrent.CopyOnWriteArrayList() }

                cookies.forEach { newCookie ->
                    // Remove old cookie with same name
                    existingCookies.removeIf { it.name == newCookie.name }
                    // Add new cookie
                    existingCookies.add(newCookie)
                }

                saveCookies()
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = mutableListOf<Cookie>()
                val host = url.host

                // Add stored cookies that match the domain
                cookieStore.forEach { (domain, storedCookies) ->
                    if (host == domain || host.endsWith(".$domain")) {
                        cookies.addAll(storedCookies)
                    }
                }

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
    fun provideOkHttpClient(@ApplicationContext context: Context, cookieJar: CookieJar): OkHttpClient {
        val cacheSize = 50L * 1024L * 1024L // 50 MB
        val cache = okhttp3.Cache(context.cacheDir.resolve("http_cache"), cacheSize)

        return OkHttpClient.Builder()
                .cache(cache)
                .cookieJar(cookieJar)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor { chain ->
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

                    // Force cache for 7 days
                    val cacheControl = okhttp3.CacheControl.Builder()
                        .maxAge(7, TimeUnit.DAYS)
                        .build()

                    // Fix Content-Type: server returns text/css but it's actually JSON
                    // Also rewrite Cache-Control to force caching
                    val responseBuilder = response.newBuilder()
                        .header("Cache-Control", cacheControl.toString())
                        .removeHeader("Pragma") // Pragma: no-cache can also prevent caching

                    if (response.header("Content-Type")?.contains("text/css") == true) {
                        responseBuilder.header("Content-Type", "application/json")
                    }
                    
                    responseBuilder.build()
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
                .addConverterFactory(
                    GsonConverterFactory.create(
                        com.google.gson.GsonBuilder()
                            .registerTypeAdapter(com.example.kemono.data.model.Creator::class.java, com.example.kemono.data.model.CreatorDeserializer())
                            .create()
                    )
                )
                .build()
    }

    @Provides
    @Singleton
    fun provideKemonoApi(retrofit: Retrofit): KemonoApi {
        return retrofit.create(KemonoApi::class.java)
    }

    @Provides
    @Singleton
    @Named("GithubClient")
    fun provideGithubOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheSize = 10L * 1024L * 1024L // 10 MB
        val cache = okhttp3.Cache(context.cacheDir.resolve("github_cache"), cacheSize)

        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .build()
    }

    @Provides
    @Singleton
    @Named("GithubRetrofit")
    fun provideGithubRetrofit(@Named("GithubClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GITHUB_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGithubApi(@Named("GithubRetrofit") retrofit: Retrofit): GithubApi {
        return retrofit.create(GithubApi::class.java)
    }

    @Provides
    @Singleton
    @Named("ImageClient")
    fun provideImageOkHttpClient(@ApplicationContext context: Context, cookieJar: CookieJar): OkHttpClient {
        val cacheSize = 250L * 1024L * 1024L // 250 MB for images
        val cache = okhttp3.Cache(context.cacheDir.resolve("image_http_cache"), cacheSize)

        return OkHttpClient.Builder()
            .cache(cache)
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addNetworkInterceptor { chain ->
                val requestBuilder =
                    chain.request()
                        .newBuilder()
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        )
                        .addHeader("Referer", "https://kemono.cr/")
                        .addHeader("Origin", "https://kemono.cr")
                        // Use standard Accept header for images, or */*
                        .addHeader("Accept", "*/*")
                        .addHeader("Connection", "keep-alive")

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        @Named("ImageClient") okHttpClient: OkHttpClient
    ): coil.ImageLoader {
        return coil.ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(coil.decode.ImageDecoderDecoder.Factory())
                } else {
                    add(coil.decode.GifDecoder.Factory())
                }
                add(coil.decode.VideoFrameDecoder.Factory())
            }
                .memoryCache {
                    coil.memory.MemoryCache.Builder(context)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    coil.disk.DiskCache.Builder()
                            .directory(context.cacheDir.resolve("image_cache"))
                            .maxSizeBytes(2L * 1024L * 1024L * 1024L) // 2 GB
                            .build()
                }
                .crossfade(true)
                .build()
    }
}
