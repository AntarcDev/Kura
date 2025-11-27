package com.example.kemono.data.remote

import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.PopularPostsResponse
import com.example.kemono.data.model.Post
import com.example.kemono.data.model.PostResponse
import com.example.kemono.data.model.PostListResponse
import com.example.kemono.data.model.CreatorListResponse
import com.example.kemono.data.model.Tag
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KemonoApi {

    @GET("creators") suspend fun getCreators(): com.google.gson.JsonElement



    @GET("posts")
    suspend fun getRecentPosts(
            @Query("o") offset: Int = 0,
            @Query("q") query: String? = null,
            @Query("tag") tags: List<String>? = null
    ): PostListResponse // Try List<Post> again, maybe the issue was something else?
    // User said: "Expected BEGIN_ARRAY but was BEGIN_OBJECT"
    // This DEFINITELY means it returns an object.
    // Let's try PostListResponse? Or maybe just generic Map/Any to debug?
    // No, let's try to be smart. PopularPostsResponse has `posts`.
    // Let's assume it's List<Post> and maybe the user hit an error page (which is an object)?
    // But they said "The Posts screen say...".
    // Let's change it to PostListResponse.

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

    @GET("posts/tags") suspend fun getTags(): List<Tag>
}
