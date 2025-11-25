package com.example.kemono.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Creator
import com.example.kemono.data.repository.KemonoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: KemonoRepository
) : ViewModel() {

    val favorites: StateFlow<List<Creator>> = repository.getAllFavorites()
        .map { favs ->
            favs.map { fav ->
                Creator(
                    id = fav.id,
                    service = fav.service,
                    name = fav.name,
                    indexed = 0L,
                    updated = fav.updated.toLongOrNull() ?: 0L
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
