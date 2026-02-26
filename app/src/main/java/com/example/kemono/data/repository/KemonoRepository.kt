package com.example.kemono.data.repository

import com.example.kemono.data.local.CacheDao
import com.example.kemono.data.local.FavoriteDao
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.model.FavoritePost
import com.example.kemono.data.model.Post
import com.example.kemono.data.model.toCached
import com.example.kemono.data.model.toCreator
import com.example.kemono.data.model.toPost
import com.example.kemono.data.model.Account
import com.example.kemono.data.model.LoginRequest
import com.example.kemono.data.remote.KemonoApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

import com.example.kemono.data.model.SearchHistory
import com.example.kemono.data.local.SearchHistoryDao

import com.example.kemono.data.local.BlacklistDao
import com.example.kemono.data.local.BlacklistEntity
import com.example.kemono.data.local.BlacklistType

@Singleton
class KemonoRepository
@Inject
constructor(
        private val api: KemonoApi,
        private val favoriteDao: FavoriteDao,
        private val cacheDao: CacheDao,
        private val searchHistoryDao: SearchHistoryDao,
        private val blacklistDao: BlacklistDao,
        private val networkMonitor: com.example.kemono.util.NetworkMonitor
) {
    private val _loginEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()

    val loginEvent: Flow<Unit> = _loginEvent.asSharedFlow()
    
    // Account State
    private val _accountState = kotlinx.coroutines.flow.MutableStateFlow<Account?>(null)
    val accountState: Flow<Account?> = _accountState.asSharedFlow()

    suspend fun login(username: String, password: String): Result<Boolean> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return Result.failure(Exception("No internet connection"))

        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                // CookieJar handles the session cookie automatically.
                // We just need to verify success.
                _loginEvent.emit(Unit)
                Result.success(true)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAccount() {
        try {
            fetchAccount()
        } catch (e: Exception) {
            // Ignore error on refresh
        }
    }

    fun logout() {
        _accountState.value = null
        // We can't clear cookies here easily without injecting SessionManager or CookieJar
        // So we rely on the caller (ViewModel) to clear the persistent session.
    }

    suspend fun fetchAccount(): Result<Account> {
         return try {
            val response = api.getAccount()
            val acc = response.props.account
            _accountState.value = acc
            Result.success(acc)
        } catch (e: Exception) {
             if (e is retrofit2.HttpException && (e.code() == 401 || e.code() == 403)) {
                 _accountState.value = null
             }
            Result.failure(e)
        }
    }

    suspend fun getCreators(): List<Creator> {
        val blacklist = blacklistDao.getAllBlacklistedItems().first()
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) {
            return try {
                cacheDao.getAllCachedCreators().first().map { it.toCreator() }.filter { isCreatorAllowed(it, blacklist) }
            } catch (e: Exception) {
                emptyList()
            }
        }

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val responseBody = api.getCreators()
                val creators = mutableListOf<Creator>()
                
                responseBody.charStream().use { charStream ->
                val reader = com.google.gson.stream.JsonReader(charStream)
                val gson = com.google.gson.GsonBuilder()
                    .registerTypeAdapter(Creator::class.java, com.example.kemono.data.model.CreatorDeserializer())
                    .create()
                
                // Determine if it's an array or object
                val token = reader.peek()
                if (token == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        try {
                            val creator = gson.fromJson<Creator>(reader, Creator::class.java)
                            if (creator.id != null) {
                                creators.add(creator)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("KemonoRepo", "Error parsing creator: ${e.message}")
                        }
                    }
                    reader.endArray()
                } else if (token == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                    val jsonObject = gson.fromJson<com.google.gson.JsonObject>(reader, com.google.gson.JsonObject::class.java)
                     val list = when {
                        jsonObject.has("creators") -> jsonObject.getAsJsonArray("creators")
                        jsonObject.has("results") -> jsonObject.getAsJsonArray("results")
                        jsonObject.has("users") -> jsonObject.getAsJsonArray("users")
                        jsonObject.has("data") -> jsonObject.getAsJsonArray("data")
                        else -> com.google.gson.JsonArray()
                    }
                    creators.addAll(list.map { gson.fromJson(it, Creator::class.java) }.filter { it.id != null })
                }
            }
            
            if (creators.isNotEmpty()) {
                // Batch insert to avoid transaction limits/OOM
                val chunkSize = 1000
                creators.chunked(chunkSize).forEach { chunk ->
                    try {
                        cacheDao.cacheCreators(chunk.map { it.toCached() })
                    } catch (e: Exception) {
                        android.util.Log.e("KemonoRepo", "Failed to cache chunk: ${e.message}")
                    }
                }
                creators.filter { isCreatorAllowed(it, blacklist) }
            } else {
                // If parsing resulted in empty list but no exception, try cache
                val cached = cacheDao.getAllCachedCreators().first().map { it.toCreator() }
                cached.filter { isCreatorAllowed(it, blacklist) }
            }
            } catch (e: Exception) {
                android.util.Log.e("KemonoRepo", "Failed to fetch creators: ${e.message}")
                try {
                    val cached = cacheDao.getAllCachedCreators().first().map { it.toCreator() }
                    cached.filter { isCreatorAllowed(it, blacklist) }
                } catch (cacheError: Exception) {
                    // If cache fails too, rethrow original error to let ViewModel handle it
                   throw e
                }
            }
        }
    }

    suspend fun getPopularCreators(): List<Creator> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) {
             // In offline mode, we can't fetch popular posts to determine popularity.
             // Fallback: Return all cached creators (or maybe favorites?)
             // For now, let's return cached creators to show *something*
             return getCreators()
        }

        // Fetch popular posts to find popular creators
        val response = api.getPopularPosts()
        val popularCreatorIds = response.posts.map { it.user }.distinct()

        // We need full creator details. We can filter the cached creators or fetch them.
        // For efficiency, let's try to get them from our full list if available,
        // otherwise we might need to fetch profiles (which is expensive for a list).
        // Best approach: Get all creators (cached) and filter by the popular IDs.
        val allCreators = getCreators()
        return allCreators.filter { it.id in popularCreatorIds }
    }

    suspend fun getPopularPosts(limit: Int = 50, date: String? = null, period: String? = null, offset: Int = 0): List<Post> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return emptyList() // TODO: Implement caching for popular posts?

        val blacklist = blacklistDao.getAllBlacklistedItems().first()
        val response = api.getPopularPosts(limit, date, period, offset)
        return response.posts.filter { isPostAllowed(it, blacklist) }
    }

    suspend fun getRandomPosts(): List<Post> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return emptyList()

        val blacklist = blacklistDao.getAllBlacklistedItems().first()

        // Fetch 5 random posts in parallel
        return kotlinx.coroutines.coroutineScope {
            (1..5).map {
                async {
                    try {
                        val redirect = api.getRandomPostRedirect()
                        val response = api.getPost(redirect.service, redirect.artistId, redirect.postId)
                        response.post
                    } catch (e: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull().filter { it.id != null && isPostAllowed(it, blacklist) }
        }
    }

    suspend fun getRecentPosts(limit: Int = 50, offset: Int = 0, query: String? = null, tags: List<String>? = null): List<Post> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return emptyList()

        val blacklist = blacklistDao.getAllBlacklistedItems().first()
        val response = api.getRecentPosts(limit, offset, query, tags)
        return response.posts.filter { it.id != null && isPostAllowed(it, blacklist) }
    }

    suspend fun getCreatorProfile(service: String, creatorId: String): Creator {
        val isOnline = networkMonitor.isOnline.first()
        
        if (!isOnline) {
             val cached = cacheDao.getCachedCreator(creatorId)
             return cached?.toCreator() ?: throw Exception("Creator not found in cache")
        }

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val responseBody = api.getCreatorProfile(service, creatorId)
                val jsonString = responseBody.string()
                
                // Debug log content
            // android.util.Log.d("KemonoRepo", "Profile JSON for $creatorId: $jsonString")
            
            val gson = com.google.gson.GsonBuilder()
                .registerTypeAdapter(Creator::class.java, com.example.kemono.data.model.CreatorDeserializer())
                .create()
            try {
                val creator = gson.fromJson(jsonString, Creator::class.java)
                cacheDao.cacheCreators(listOf(creator.toCached()))
                creator
            } catch (e: Exception) {
                 android.util.Log.e("KemonoRepo", "Failed to parse profile JSON: $jsonString", e)
                 throw e
            }
            } catch (e: Exception) {
                val cached = cacheDao.getCachedCreator(creatorId)
                cached?.toCreator() ?: throw e
            }
        }
    }

    suspend fun getCreatorPosts(
            service: String,
            creatorId: String,
            limit: Int = 50,
            offset: Int = 0,
            query: String? = null
    ): List<Post> {
        val blacklist = blacklistDao.getAllBlacklistedItems().first()
        val isOnline = networkMonitor.isOnline.first()

        if (!isOnline) {
             if (offset == 0 && query == null) {
                 return try {
                    cacheDao.getCachedPosts(service, creatorId).first().map { it.toPost() }.filter { isPostAllowed(it, blacklist) }
                 } catch (e: Exception) {
                     emptyList()
                 }
             } else {
                 return emptyList()
             }
        }

        return try {
            val posts = api.getCreatorPosts(service, creatorId, limit, offset, query)
            cacheDao.cachePosts(posts.map { it.toCached() })
            posts.filter { it.id != null && isPostAllowed(it, blacklist) }
        } catch (e: Exception) {
            if (offset == 0 && query == null) {
                try {
                    cacheDao.getCachedPosts(service, creatorId).first().map { it.toPost() }.filter { isPostAllowed(it, blacklist) }
                } catch (cacheError: Exception) {
                    throw e
                }
            } else {
                throw e
            }
        }
    }



    suspend fun getCreatorAnnouncements(service: String, creatorId: String): List<com.example.kemono.data.model.Announcement> {
        return try {
            api.getCreatorAnnouncements(service, creatorId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCreatorTags(service: String, creatorId: String): List<String> {
        return try {
            api.getCreatorTags(service, creatorId).map { it.tag }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCreatorLinks(service: String, creatorId: String): List<com.example.kemono.data.model.CreatorLink> {
        return try {
            api.getCreatorLinks(service, creatorId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCreatorFancards(service: String, creatorId: String): List<com.example.kemono.data.model.Fancard> {
        return try {
            api.getCreatorFancards(service, creatorId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPost(service: String, creatorId: String, postId: String): Post {
        val isOnline = networkMonitor.isOnline.first()
        
        if (!isOnline) {
            return cacheDao.getCachedPost(postId)?.toPost() ?: throw Exception("Post not found in cache")
        }

        return try {
            val post = api.getPost(service, creatorId, postId).post
            if (post.id != null) {
                cacheDao.cachePosts(listOf(post.toCached()))
            }
            post
        } catch (e: Exception) {
            cacheDao.getCachedPost(postId)?.toPost() ?: throw e
        }
    }

    suspend fun cleanExpiredCache() {
        val expiryTime = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) // 7 days
        cacheDao.deleteExpiredCreators(expiryTime)
        cacheDao.deleteExpiredPosts(expiryTime)
    }

    // Favorites
    fun getAllFavorites(): Flow<List<FavoriteCreator>> {
        return favoriteDao.getAllFavorites()
    }

    suspend fun addFavorite(creator: FavoriteCreator) {
        favoriteDao.insertFavorite(creator)
    }

    suspend fun removeFavorite(creator: FavoriteCreator) {
        favoriteDao.deleteFavorite(creator)
    }

    fun isFavorite(id: String): Flow<Boolean> {
        return favoriteDao.isFavorite(id)
    }

    fun getAllFavoritePosts(): Flow<List<FavoritePost>> {
        return favoriteDao.getAllFavoritePosts()
    }

    suspend fun addFavoritePost(post: FavoritePost) {
        favoriteDao.insertFavoritePost(post)
    }

    suspend fun removeFavoritePost(post: FavoritePost) {
        favoriteDao.deleteFavoritePost(post)
    }

    fun isPostFavorite(id: String): Flow<Boolean> {
        return favoriteDao.isPostFavorite(id)
    }

    suspend fun getTags(): List<String> {
        return api.getTags().map { it.tag }
    }

    suspend fun getDiscordChannels(serverId: String): List<com.example.kemono.data.model.DiscordChannel> {
        return try {
            api.getDiscordChannels(serverId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDiscordChannelPosts(channelId: String, offset: Int = 0): List<com.example.kemono.data.model.DiscordPost> {
        return try {
            api.getDiscordChannelPosts(channelId, offset)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun importFavorites(): Int {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) throw Exception("No internet connection")
        
        var count = 0

        // 1. Import Artists
        val apiArtists = api.getApiFavorites(type = "artist")
        android.util.Log.d("KemonoRepo", "Fetched ${apiArtists.size} favorite artists")
        apiArtists.forEach { fav ->
             val favorite = FavoriteCreator(
                id = fav.id,
                service = fav.service,
                name = fav.name,
                updated = fav.updated
            )
            try {
                favoriteDao.insertFavorite(favorite)
                count++
            } catch (e: Exception) {
                android.util.Log.e("KemonoRepo", "Failed to import artist ${fav.id}: ${e.message}")
            }
        }

        // 2. Import Posts
        val apiPosts = api.getApiFavorites(type = "post")
        android.util.Log.d("KemonoRepo", "Fetched ${apiPosts.size} favorite posts")

        // Fetch details in parallel chunks
        kotlinx.coroutines.coroutineScope {
            val chunkedPosts = apiPosts.chunked(5)
            chunkedPosts.forEach { chunk ->
                val deferredDetails = chunk.map { fav ->
                    async {
                        try {
                            if (!fav.user.isNullOrBlank()) {
                                api.getPost(fav.service, fav.user, fav.id).post
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("KemonoRepo", "Failed to fetch details for post ${fav.id}: ${e.message}")
                            null
                        }
                    }
                }
                
                val details = deferredDetails.awaitAll()
                
                chunk.zip(details).forEach { (fav, fullPost) ->
                    val thumbnail = fullPost?.file?.path ?: fullPost?.attachments?.firstOrNull()?.path
                    val favoritePost = FavoritePost(
                        id = fav.id,
                        service = fav.service,
                        user = fav.user ?: "",
                        title = fullPost?.title ?: fav.name ?: "Untitled",
                        content = fullPost?.content ?: "",
                        published = fullPost?.published ?: fav.indexed ?: "",
                        thumbnailPath = thumbnail,
                        added = System.currentTimeMillis()
                    )
                    
                    try {
                        favoriteDao.insertFavoritePost(favoritePost)
                        count++
                    } catch (e: Exception) {
                        android.util.Log.e("KemonoRepo", "Failed to insert imported post ${fav.id}: ${e.message}")
                    }
                }
            }
        }
        
        android.util.Log.d("KemonoRepo", "Successfully imported $count items")
        return count
    }

    suspend fun pushFavoritesToAccount(): Int {
         val isOnline = networkMonitor.isOnline.first()
         if (!isOnline) throw Exception("No internet connection")
         
         var successCount = 0

         // --- Artists ---
         val localFavorites = favoriteDao.getAllFavorites().first()
         val apiFavorites = api.getApiFavorites(type = "artist")
         val remoteIds = apiFavorites.map { it.id }.toSet()
         val toPush = localFavorites.filter { it.id !in remoteIds }

         android.util.Log.d("KemonoRepo", "Found ${toPush.size} artists to push")
         toPush.forEach { fav ->
             try {
                 val response = api.addFavoriteArtist(fav.service, fav.id)
                 if (response.isSuccessful) successCount++
             } catch (e: Exception) {
                 android.util.Log.e("KemonoRepo", "Error pushing artist ${fav.name}: ${e.message}")
             }
         }

         // --- Posts ---
         val localPosts = favoriteDao.getAllFavoritePosts().first()
         val apiPosts = api.getApiFavorites(type = "post")
         val remotePostIds = apiPosts.map { it.id }.toSet()
         val postsToPush = localPosts.filter { it.id !in remotePostIds }

         android.util.Log.d("KemonoRepo", "Found ${postsToPush.size} posts to push")
         postsToPush.forEach { post ->
             try {
                 val response = api.addFavoritePost(post.service, post.user, post.id)
                 if (response.isSuccessful) successCount++
             } catch (e: Exception) {
                 android.util.Log.e("KemonoRepo", "Error pushing post ${post.title}: ${e.message}")
             }
         }

         return successCount
    }

    // Search History
    fun getSearchHistory(limit: Int = 10): Flow<List<SearchHistory>> {
        return searchHistoryDao.getRecentSearchHistory(limit)
    }

    suspend fun addToSearchHistory(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insert(SearchHistory(query.trim()))
        }
    }

    suspend fun removeFromSearchHistory(query: String) {
        searchHistoryDao.delete(query)
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clear()
    }

    // Blacklist
    fun getAllBlacklistedItems(): Flow<List<BlacklistEntity>> {
        return blacklistDao.getAllBlacklistedItems()
    }

    suspend fun addToBlacklist(item: BlacklistEntity) {
        blacklistDao.addToBlacklist(item)
    }

    suspend fun removeFromBlacklist(item: BlacklistEntity) {
        blacklistDao.removeFromBlacklist(item)
    }

    private fun isPostAllowed(post: Post, blacklist: List<BlacklistEntity>): Boolean {
        val creatorBlacklisted = blacklist.any { it.type == BlacklistType.CREATOR && it.id == post.user }
        if (creatorBlacklisted) return false

        val postTags = post.tags.orEmpty()
        val tagBlacklisted = blacklist.any { 
             it.type == BlacklistType.TAG && postTags.any { tag -> tag.equals(it.id, ignoreCase = true) } 
        }
        if (tagBlacklisted) return false

        val keywordBlacklisted = blacklist.any { 
            it.type == BlacklistType.KEYWORD && (
                (post.title?.contains(it.id, ignoreCase = true) == true) || 
                (post.content?.contains(it.id, ignoreCase = true) == true)
            )
        }
        if (keywordBlacklisted) return false

        return true
    }

    private fun isCreatorAllowed(creator: Creator, blacklist: List<BlacklistEntity>): Boolean {
        return blacklist.none { it.type == BlacklistType.CREATOR && it.id == creator.id }
    }
}
