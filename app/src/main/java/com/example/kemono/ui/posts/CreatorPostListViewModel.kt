package com.example.kemono.ui.posts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.model.Post
import com.example.kemono.data.repository.KemonoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatorPostListViewModel @Inject constructor(
    private val repository: KemonoRepository,
    private val downloadRepository: com.example.kemono.data.repository.DownloadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val service: String = checkNotNull(savedStateHandle["service"])
    val creatorId: String = checkNotNull(savedStateHandle["creatorId"])

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _creator = MutableStateFlow<Creator?>(null)
    val creator: StateFlow<Creator?> = _creator.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _announcements = MutableStateFlow<List<com.example.kemono.data.model.Announcement>>(emptyList())
    val announcements: StateFlow<List<com.example.kemono.data.model.Announcement>> = _announcements.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _links = MutableStateFlow<List<com.example.kemono.data.model.CreatorLink>>(emptyList())
    val links: StateFlow<List<com.example.kemono.data.model.CreatorLink>> = _links.asStateFlow()

    private val _fancards = MutableStateFlow<List<com.example.kemono.data.model.Fancard>>(emptyList())
    val fancards: StateFlow<List<com.example.kemono.data.model.Fancard>> = _fancards.asStateFlow()


    
    // ... [Other methods unchanged]



    private fun fetchCreatorProfile() {
        viewModelScope.launch {
            try {
                val creator = repository.getCreatorProfile(service, creatorId)
                _creator.value = creator
            } catch (e: Exception) {
                // Log or handle error, but posts might still load
            }
        }
    }

    private fun checkIfFavorite() {
        viewModelScope.launch {
            repository.isFavorite(creatorId).collect { isFav ->
                _isFavorite.value = isFav
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentCreator = _creator.value ?: return@launch
            if (_isFavorite.value) {
                // Remove
                val fav = com.example.kemono.data.model.FavoriteCreator(
                    id = currentCreator.id,
                    name = currentCreator.name ?: "",
                    service = currentCreator.service,
                    updated = currentCreator.updated.toString()
                )
                repository.removeFavorite(fav)
            } else {
                // Add
                val fav = com.example.kemono.data.model.FavoriteCreator(
                    id = currentCreator.id,
                    name = currentCreator.name ?: "",
                    service = currentCreator.service,
                    updated = currentCreator.updated.toString()
                )
                repository.addFavorite(fav)
            }
        }
    }

    private fun fetchProfileDetails() {
        viewModelScope.launch {
            // Fetch announcements
            launch {
                try {
                    _announcements.value = repository.getCreatorAnnouncements(service, creatorId)
                } catch (_: Exception) {}
            }
            // Fetch tags
            launch {
                try {
                    _tags.value = repository.getCreatorTags(service, creatorId)
                } catch (_: Exception) {}
            }
            // Fetch links
            launch {
                try {
                    _links.value = repository.getCreatorLinks(service, creatorId)
                } catch (_: Exception) {}
            }
            // Fetch fancards
            launch {
                try {
                    _fancards.value = repository.getCreatorFancards(service, creatorId)
                } catch (_: Exception) {}
            }
        }
    }

    // Favorite Posts State
    private val _favoritePostIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePostIds: StateFlow<Set<String>> = _favoritePostIds.asStateFlow()

    private fun observeFavoritePosts() {
        viewModelScope.launch {
            repository.getAllFavoritePosts().collect { favorites ->
                _favoritePostIds.value = favorites.map { it.id }.toSet()
            }
        }
    }

    // Downloaded Posts State
    val downloadedPostIds: StateFlow<Set<String>> = downloadRepository.getDownloadedPostIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun toggleFavoritePost(post: Post) {
        viewModelScope.launch {
            val postId = post.id ?: return@launch
            val isFav = _favoritePostIds.value.contains(postId)
            
            val favoritePost = com.example.kemono.data.model.FavoritePost(
                id = postId,
                user = post.user ?: creatorId, // Fallback to current creator
                service = post.service ?: service,
                title = post.title ?: "Untitled",
                content = post.content ?: "",
                thumbnailPath = post.file?.path,
                published = post.published ?: "",
                added = System.currentTimeMillis()
            )

            if (isFav) {
                repository.removeFavoritePost(favoritePost)
            } else {
                repository.addFavoritePost(favoritePost)
            }
        }
    }

    private var currentOffset = 0
    private var fetchJob: kotlinx.coroutines.Job? = null

    fun loadMorePosts() {
        if (!_isLoading.value) {
            fetchPosts(reset = false)
        }
    }

    fun fetchPosts(reset: Boolean = false) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (reset) {
                _isLoading.value = true
                currentOffset = 0
            }
            // If just loading more, we might want a separate loading state or just keep it silent/bottom loader
            // For now, let's reuse _isLoading but handle UI strictly? 
            // Better to only set isLoading=true for initial load or full refresh.
            // But complex. Let's stick to simple isLoading for now.
             if (reset) _isLoading.value = true
             
            _error.value = null
            try {
                if (service == "discord") {
                    fetchDiscordChannels(creatorId)
                } else {
                    // API does not reliably support custom limits for this endpoint.
                    // We rely on the server's default page size (usually 50) to avoid 400 errors or gaps.
                    val newPosts = repository.getCreatorPosts(
                        service = service, 
                        creatorId = creatorId, 
                        limit = 50, // Hardcoded to 50 as API seems to enforce/expect this
                        offset = currentOffset
                    )
                    
                    if (reset) {
                        _posts.value = newPosts
                    } else {
                        _posts.value = _posts.value + newPosts
                    }
                    
                    // Critical Fix: Increment offset by actual items received, not expected limit.
                    // This handles if server returns fewer items or ignores limit.
                    if (newPosts.isNotEmpty()) {
                        currentOffset += newPosts.size
                    }
                }
            } catch (e: Exception) {
                if (e is retrofit2.HttpException && e.code() == 429) {
                     // Too many requests
                } else if (e is retrofit2.HttpException && e.code() == 400) {
                    _error.value = "Error loading posts (400). Limit might be too high."
                } else {
                    _error.value = e.message ?: "Failed to load posts"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Discord State
    private val _discordChannels = MutableStateFlow<List<com.example.kemono.data.model.DiscordChannel>>(emptyList())
    val discordChannels: StateFlow<List<com.example.kemono.data.model.DiscordChannel>> = _discordChannels.asStateFlow()

    private val _selectedChannel = MutableStateFlow<com.example.kemono.data.model.DiscordChannel?>(null)
    val selectedChannel: StateFlow<com.example.kemono.data.model.DiscordChannel?> = _selectedChannel.asStateFlow()

    private val _discordPosts = MutableStateFlow<List<com.example.kemono.data.model.DiscordPost>>(emptyList())
    val discordPosts: StateFlow<List<com.example.kemono.data.model.DiscordPost>> = _discordPosts.asStateFlow()

    fun fetchDiscordChannels(serverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val channels = repository.getDiscordChannels(serverId)
                _discordChannels.value = channels
                if (channels.isNotEmpty()) {
                    selectChannel(channels.first())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load channels: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectChannel(channel: com.example.kemono.data.model.DiscordChannel) {
        _selectedChannel.value = channel
        fetchDiscordPosts(channel.id)
    }

    fun fetchDiscordPosts(channelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val posts = repository.getDiscordChannelPosts(channelId)
                _discordPosts.value = posts
            } catch (e: Exception) {
                _error.value = "Failed to load channel posts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Selection State
    private val _selectedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPostIds: StateFlow<Set<String>> = _selectedPostIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    fun toggleSelection(post: Post) {
        val current = _selectedPostIds.value
        val postId = post.id ?: return
        if (current.contains(postId)) {
            _selectedPostIds.value = current - postId
            if (_selectedPostIds.value.isEmpty()) {
                _isSelectionMode.value = false
            }
        } else {
            _selectedPostIds.value = current + postId
            _isSelectionMode.value = true
        }
    }

    fun clearSelection() {
        _selectedPostIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun downloadSelectedPosts() {
        val selectedIds = _selectedPostIds.value
        val postsToDownload = _posts.value.filter { it.id in selectedIds }
        
        viewModelScope.launch {
            postsToDownload.forEach { post ->
                // Ensure we have creator name, or fallback
                val creatorName = post.user // Ideally fetch name if needed, but ID is safe for now
                
                // Download main file
                post.file?.let { file ->
                    if (!file.path.isNullOrEmpty()) {
                        val url = "https://kemono.cr${file.path}"
                        val mediaType = if (com.example.kemono.util.getMediaType(file.path!!) == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                        downloadRepository.downloadFile(
                            url,
                            file.name ?: "file",
                            post.id ?: "",
                            post.title ?: "",
                            post.user ?: "",
                            creatorName ?: "",
                            mediaType
                        )
                    }
                }

                // Download attachments
                post.attachments.forEach { attachment ->
                    if (!attachment.path.isNullOrEmpty()) {
                        val url = "https://kemono.cr${attachment.path}"
                        val mediaType = if (com.example.kemono.util.getMediaType(attachment.path!!) == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                        downloadRepository.downloadFile(
                            url,
                            attachment.name ?: "attachment",
                            post.id ?: "",
                            post.title ?: "",
                            post.user ?: "",
                            creatorName ?: "",
                            mediaType
                        )
                    }
                }
            }
            clearSelection()
        }
    }

    fun downloadFancard(fancard: com.example.kemono.data.model.Fancard) {
        viewModelScope.launch {
            val currentCreator = _creator.value ?: return@launch
            val creatorName = currentCreator.name ?: "Unknown Creator"
            val subFolder = "Fancards"
            
            val url = if (fancard.file?.path != null) {
                "https://kemono.cr/data${fancard.file.path}"
            } else if (fancard.hash != null && fancard.ext != null) {
                val hash = fancard.hash
                val ext = fancard.ext
                val prefix1 = hash.take(2)
                val prefix2 = hash.drop(2).take(2)
                val baseUrl = fancard.server ?: "https://kemono.cr"
                "$baseUrl/data/$prefix1/$prefix2/$hash$ext"
            } else {
                fancard.coverUrl ?: "https://kemono.cr/icons/fanbox/${fancard.userId}"
            }
            
            val fileName = "Fancard ${fancard.id}${fancard.ext ?: ".jpg"}"

            downloadRepository.downloadFile(
                url = url,
                fileName = fileName,
                postId = fancard.id,
                postTitle = "Fancard ${fancard.id}",
                creatorId = fancard.userId,
                creatorName = creatorName,
                mediaType = "IMAGE",
                subFolder = subFolder
            )
        }
    }

    init {
        fetchCreatorProfile()
        fetchPosts(reset = true)
        checkIfFavorite()
        fetchProfileDetails()
        observeFavoritePosts()
    }
}
