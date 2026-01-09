package com.example.kemono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.local.BlacklistEntity
import com.example.kemono.data.local.BlacklistType
import com.example.kemono.data.repository.KemonoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel @Inject constructor(
    private val repository: KemonoRepository
) : ViewModel() {

    val blacklist: StateFlow<List<BlacklistEntity>> = repository.getAllBlacklistedItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creators = blacklist.map { list -> list.filter { it.type == BlacklistType.CREATOR } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags = blacklist.map { list -> list.filter { it.type == BlacklistType.TAG } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val keywords = blacklist.map { list -> list.filter { it.type == BlacklistType.KEYWORD } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCreator(id: String, name: String, service: String) {
        viewModelScope.launch {
            repository.addToBlacklist(
                BlacklistEntity(
                    id = id,
                    type = BlacklistType.CREATOR,
                    name = name,
                    service = service
                )
            )
        }
    }

    fun addTag(tag: String) {
        viewModelScope.launch {
            repository.addToBlacklist(
                BlacklistEntity(
                    id = tag.trim(),
                    type = BlacklistType.TAG,
                    name = tag.trim()
                )
            )
        }
    }

    fun addKeyword(keyword: String) {
        viewModelScope.launch {
            repository.addToBlacklist(
                BlacklistEntity(
                    id = keyword.trim(),
                    type = BlacklistType.KEYWORD,
                    name = keyword.trim()
                )
            )
        }
    }

    fun removeItem(item: BlacklistEntity) {
        viewModelScope.launch {
            repository.removeFromBlacklist(item)
        }
    }
}
