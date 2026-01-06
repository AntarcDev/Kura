package com.example.kemono.data.remote

import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.PopularPostsResponse
import com.example.kemono.data.model.Post
import com.example.kemono.data.model.PostResponse
import com.example.kemono.data.model.PostListResponse
import com.example.kemono.data.model.CreatorListResponse
import com.example.kemono.data.model.Tag
import com.example.kemono.data.model.AccountResponse
import com.example.kemono.data.model.ApiFavorite
import com.example.kemono.data.model.LoginRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Header

interface KemonoApi {

    @GET("creators") suspend fun getCreators(): ResponseBody





    @GET("posts")
    suspend fun getRecentPosts(
            @Query("limit") limit: Int = 50,
            @Query("o") offset: Int = 0,
            @Query("q") query: String? = null,
            @Query("tag") tags: List<String>? = null
    ): PostListResponse

    @GET("posts/popular")
    suspend fun getPopularPosts(
            @Query("limit") limit: Int = 50,
            @Query("date") date: String? = null,
            @Query("period") period: String? = null,
            @Query("o") offset: Int = 0
    ): PopularPostsResponse

    @GET("posts/random")
    suspend fun getRandomPostRedirect(): RandomPostRedirect

    @GET("{service}/user/{user}/post/{id}")
    suspend fun getPost(
        @Path("service") service: String,
        @Path("user") user: String,
        @Path("id") id: String
    ): PostResponse

    @GET("{service}/user/{creatorId}/profile")
    suspend fun getCreatorProfile(
            @Path("service") service: String,
            @Path("creatorId") creatorId: String
    ): ResponseBody

    @GET("{service}/user/{creatorId}/posts")
    suspend fun getCreatorPosts(
            @Path("service") service: String,
            @Path("creatorId") creatorId: String,
            @Query("limit") limit: Int = 50,
            @Query("o") offset: Int = 0,
            @Query("q") query: String? = null
    ): List<Post>

    @GET("{service}/user/{creatorId}/announcements")
    suspend fun getCreatorAnnouncements(
        @Path("service") service: String,
        @Path("creatorId") creatorId: String
    ): List<com.example.kemono.data.model.Announcement>

    @GET("{service}/user/{creatorId}/tags")
    suspend fun getCreatorTags(
        @Path("service") service: String,
        @Path("creatorId") creatorId: String
    ): List<Tag>
    // Actually, tags.json was empty. Let's assume List<String> or List<Tag> if Tag is defined.
    // Existing getTags returns List<Tag>. Let's check Tag definition.
    
    @GET("{service}/user/{creatorId}/links")
    suspend fun getCreatorLinks(
        @Path("service") service: String,
        @Path("creatorId") creatorId: String
    ): List<com.example.kemono.data.model.CreatorLink>

    @GET("{service}/user/{creatorId}/fancards")
    suspend fun getCreatorFancards(
        @Path("service") service: String,
        @Path("creatorId") creatorId: String
    ): List<com.example.kemono.data.model.Fancard>

    @GET("posts/tags") suspend fun getTags(): List<Tag>

    @POST("https://kemono.cr/api/v1/authentication/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @GET("https://kemono.cr/api/v1/account")
    suspend fun getAccount(): AccountResponse

    @GET("https://kemono.cr/api/v1/account/favorites")
    suspend fun getApiFavorites(
        @Query("type") type: String? = null
    ): List<ApiFavorite>

    @POST("https://kemono.cr/api/v1/favorites/creator/{service}/{id}")
    suspend fun addFavoriteArtist(
        @Path("service") service: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @retrofit2.http.DELETE("https://kemono.cr/api/v1/favorites/creator/{service}/{id}")
    suspend fun removeFavoriteArtist(
        @Path("service") service: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @POST("https://kemono.cr/api/v1/favorites/post/{service}/{user}/{id}")
    suspend fun addFavoritePost(
        @Path("service") service: String,
        @Path("user") user: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @retrofit2.http.DELETE("https://kemono.cr/api/v1/favorites/post/{service}/{user}/{id}")
    suspend fun removeFavoritePost(
        @Path("service") service: String,
        @Path("user") user: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @GET("discord/channel/lookup/{discord_server}")
    suspend fun getDiscordChannels(@Path("discord_server") id: String): List<com.example.kemono.data.model.DiscordChannel>

    @GET("discord/channel/{channel_id}")
    suspend fun getDiscordChannelPosts(
        @Path("channel_id") id: String,
        @Query("o") offset: Int = 0
    ): List<com.example.kemono.data.model.DiscordPost>
}

data class RandomPostRedirect(
    @com.google.gson.annotations.SerializedName("service") val service: String,
    @com.google.gson.annotations.SerializedName("artist_id") val artistId: String,
    @com.google.gson.annotations.SerializedName("post_id") val postId: String
)
