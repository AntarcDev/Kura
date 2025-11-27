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

    private val _creator = MutableStateFlow<Creator?>(null)
    val creator: StateFlow<Creator?> = _creator.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchCreatorProfile()
        fetchPosts()
        checkIfFavorite()
    }

    private fun checkIfFavorite() {
        viewModelScope.launch {
            repository.isFavorite(creatorId).collect {
                _isFavorite.value = it
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentCreator = _creator.value ?: return@launch
            val favoriteCreator = FavoriteCreator(
                id = currentCreator.id,
                name = currentCreator.name,
                service = currentCreator.service,
                updated = currentCreator.updated.toString()
            )
            
            if (_isFavorite.value) {
                repository.removeFavorite(favoriteCreator)
            } else {
                repository.addFavorite(favoriteCreator)
            }
        }
    }

    private fun fetchCreatorProfile() {
        viewModelScope.launch {
            try {
                _creator.value = repository.getCreatorProfile(service, creatorId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
