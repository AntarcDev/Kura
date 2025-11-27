package com.example.kemono.data.remote

import com.example.kemono.data.model.GithubRelease
import retrofit2.http.GET

interface GithubApi {
    @GET("repos/AntarcDev/Kura/releases/latest")
    suspend fun getLatestRelease(): GithubRelease
}
