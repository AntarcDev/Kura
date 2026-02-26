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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.kemono.util.NetworkMonitor

@HiltViewModel
class CreatorPostListViewModel @Inject constructor(
    private val repository: KemonoRepository,
    private val downloadRepository: com.example.kemono.data.repository.DownloadRepository,
    private val settingsRepository: com.example.kemono.data.repository.SettingsRepository,
    savedStateHandle: SavedStateHandle,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val service: String = checkNotNull(savedStateHandle["service"])
    val creatorId: String = checkNotNull(savedStateHandle["creatorId"])

    val pagedPosts: Flow<PagingData<Post>> = repository.getPagedCreatorPosts(service, creatorId).cachedIn(viewModelScope)

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
                
                val autoDownload = settingsRepository.autoDownloadFavorites.firstOrNull() ?: false
                if (autoDownload) {
                    val creatorName = _creator.value?.name ?: creatorId
                    downloadRepository.downloadPostMedia(post, creatorName)
                }
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
    private val _selectedPosts = MutableStateFlow<Set<Post>>(emptySet())
    val selectedPosts: StateFlow<Set<Post>> = _selectedPosts.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    fun toggleSelection(post: Post) {
        val current = _selectedPosts.value
        val postId = post.id ?: return
        if (current.any { it.id == postId }) {
            _selectedPosts.value = current.filterNot { it.id == postId }.toSet()
            if (_selectedPosts.value.isEmpty()) {
                _isSelectionMode.value = false
            }
        } else {
            _selectedPosts.value = current + post
            _isSelectionMode.value = true
        }
    }

    fun clearSelection() {
        _selectedPosts.value = emptySet()
        _isSelectionMode.value = false
    }

    fun downloadSelectedPosts() {
        val postsToDownload = _selectedPosts.value
        
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
        if (service == "discord") fetchDiscordChannels(creatorId)
        checkIfFavorite()
        fetchProfileDetails()
        observeFavoritePosts()
    }
}
