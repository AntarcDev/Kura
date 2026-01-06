package com.example.kemono.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.DownloadedItem
import com.example.kemono.data.repository.DownloadRepository
import com.example.kemono.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@HiltViewModel
class GalleryViewModel @Inject constructor(
    repository: DownloadRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val gridDensity = settingsRepository.gridDensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")

    private val _searchQuery = kotlinx.coroutines.flow.MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val galleryItems: StateFlow<List<GalleryUiItem>> =
            kotlinx.coroutines.flow.combine(repository.getAllDownloadedItems(), _searchQuery) { items, query ->
                val filteredItems = if (query.isBlank()) {
                    items
                } else {
                    items.filter { 
                        it.fileName.contains(query, ignoreCase = true) || 
                        it.creatorName.contains(query, ignoreCase = true)
                    }
                }
                
                filteredItems.groupBy { it.creatorName }
                        .flatMap { (creatorName, creatorItems) ->
                            listOf(GalleryUiItem.Header(creatorName)) +
                                    creatorItems.map { GalleryUiItem.Image(it) }
                        }
            }
            .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
            )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}

sealed interface GalleryUiItem {
    data class Header(val title: String) : GalleryUiItem
    data class Image(val item: DownloadedItem) : GalleryUiItem
}
