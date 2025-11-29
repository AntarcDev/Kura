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
    ): Creator

    @GET("{service}/user/{creatorId}/posts")
    suspend fun getCreatorPosts(
            @Path("service") service: String,
            @Path("creatorId") creatorId: String,
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
}

data class RandomPostRedirect(
    @com.google.gson.annotations.SerializedName("service") val service: String,
    @com.google.gson.annotations.SerializedName("artist_id") val artistId: String,
    @com.google.gson.annotations.SerializedName("post_id") val postId: String
)
