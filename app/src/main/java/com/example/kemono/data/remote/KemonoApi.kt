package com.example.kemono.data.remote

import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.PopularPostsResponse
import com.example.kemono.data.model.Post
import com.example.kemono.data.model.PostResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KemonoApi {

    @GET("creators") suspend fun getCreators(): List<Creator>

    @GET("posts")
    suspend fun getRecentPosts(
            @Query("o") offset: Int = 0,
            @Query("q") query: String? = null
    ): List<Post>

    @GET("posts/popular")
    suspend fun getPopularPosts(
            @Query("o") offset: Int = 0,
            @Query("q") query: String? = null
    ): PopularPostsResponse

    @GET("{service}/user/{creatorId}/profile")
    suspend fun getCreatorProfile(
            @Path("service") service: String,
            @Path("creatorId") creatorId: String
    ): Creator

    @GET("{service}/user/{creatorId}/posts")
    suspend fun getCreatorPosts(
            @Path("service") service: String,
            @Path("creatorId") creatorId: String,
            @Query("o") offset: Int = 0,
            @Query("q") query: String? = null
    ): List<Post>

    @GET("{service}/user/{creatorId}/post/{postId}")
    suspend fun getPost(
            @Path("service") service: String,
            @Path("creatorId") creatorId: String,
            @Path("postId") postId: String
    ): PostResponse
}
