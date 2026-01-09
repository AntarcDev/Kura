package com.example.kemono.ui.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.repository.DownloadRepository
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.util.MediaType
import com.example.kemono.util.getMediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ViewerMediaItem(
        val url: String,
        val type: MediaType,
        val name: String,
        val isLocal: Boolean = false
)

@HiltViewModel
class ImageViewerViewModel
@Inject
constructor(
        savedStateHandle: SavedStateHandle,
        private val kemonoRepository: KemonoRepository,
        private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val type: String = checkNotNull(savedStateHandle["type"])
    private val id: String = checkNotNull(savedStateHandle["id"])
    private val initialIndexArg: String = checkNotNull(savedStateHandle["initialIndex"])
    val initialIndex = initialIndexArg.toIntOrNull() ?: 0

    private val _mediaItems = MutableStateFlow<List<ViewerMediaItem>>(emptyList())
    val mediaItems: StateFlow<List<ViewerMediaItem>> = _mediaItems.asStateFlow()

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            if (type == "post") {
                // ID format for post: service/creatorId/postId
                // But we passed just the ID in the plan?
                // Let's check the plan. The plan said "id: The ID of the post".
                // But getPost needs service and creatorId too.
                // I should probably pass them all or encode them in the ID.
                // For simplicity, let's assume the ID passed is "service|creatorId|postId" or
                // handle it in the route.
                // Actually, let's look at how I'll construct the route.
                // route = "viewer/{type}/{id}/{initialIndex}"
                // If type is post, id could be "service|creatorId|postId"

                val parts = id.split("|")
                if (parts.size == 3) {
                    val service = parts[0]
                    val creatorId = parts[1]
                    val postId = parts[2]
                    try {
                        val post = kemonoRepository.getPost(service, creatorId, postId)
                        post?.let {
                            val items = mutableListOf<ViewerMediaItem>()
                            // Main file
                            it.file?.let { file ->
                                if (!file.path.isNullOrEmpty()) {
                                    items.add(
                                            ViewerMediaItem(
                                                    url = "https://kemono.cr${file.path}",
                                                    type = getMediaType(file.path!!),
                                                    name = file.name ?: "File"
                                            )
                                    )
                                }
                            }
                            // Attachments
                            it.attachments.forEach { attachment ->
                                if (!attachment.path.isNullOrEmpty()) {
                                    items.add(
                                            ViewerMediaItem(
                                                    url = "https://kemono.cr${attachment.path}",
                                                    type = getMediaType(attachment.path!!),
                                                    name = attachment.name ?: "Attachment"
                                            )
                                    )
                                }
                            }
                            _mediaItems.value = items
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            } else if (type == "gallery") {
                downloadRepository.getAllDownloadedItems().collect { downloadedItems ->
                    val items =
                            downloadedItems.map { item ->
                                ViewerMediaItem(
                                        url = item.filePath,
                                        type =
                                                if (item.mediaType == "VIDEO") MediaType.VIDEO
                                                else MediaType.IMAGE,
                                        name = item.fileName,
                                        isLocal = true
                                )
                            }
                    _mediaItems.value = items
                }
            }
        }
    }
}
