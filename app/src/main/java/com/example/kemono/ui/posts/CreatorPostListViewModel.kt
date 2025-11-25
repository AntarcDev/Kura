package com.example.kemono.ui.posts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Post
import com.example.kemono.data.repository.KemonoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatorPostListViewModel @Inject constructor(
    private val repository: KemonoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val service: String = checkNotNull(savedStateHandle["service"])
    val creatorId: String = checkNotNull(savedStateHandle["creatorId"])

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetching first page for now
                _posts.value = repository.getCreatorPosts(service, creatorId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load posts"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
