package com.example.kemono.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.DownloadedItem
import com.example.kemono.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

@HiltViewModel
class GalleryViewModel @Inject constructor(repository: DownloadRepository) : ViewModel() {

    val galleryItems: StateFlow<List<GalleryUiItem>> =
            repository
                    .getAllDownloadedItems()
                    .map { items ->
                        items.groupBy { it.creatorName }
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
}

sealed interface GalleryUiItem {
    data class Header(val title: String) : GalleryUiItem
    data class Image(val item: DownloadedItem) : GalleryUiItem
}
