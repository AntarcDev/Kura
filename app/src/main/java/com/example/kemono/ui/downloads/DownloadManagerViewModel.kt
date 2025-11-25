package com.example.kemono.ui.downloads

import android.app.DownloadManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.DownloadedItem
import com.example.kemono.data.repository.DownloadRepository
import com.example.kemono.data.repository.DownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DownloadItemUiState(val item: DownloadedItem, val status: DownloadStatus? = null)

@HiltViewModel
class DownloadManagerViewModel @Inject constructor(private val repository: DownloadRepository) :
        ViewModel() {

    private val _downloadStatuses = MutableStateFlow<Map<Long, DownloadStatus>>(emptyMap())

    val downloadItems: StateFlow<List<DownloadItemUiState>> =
            combine(repository.getAllDownloadedItems(), _downloadStatuses) { items, statuses ->
                        items.map { item ->
                            DownloadItemUiState(item = item, status = statuses[item.downloadId])
                        }
                    }
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                val currentItems = downloadItems.value
                val activeDownloads =
                        currentItems.filter {
                            it.item.downloadId != -1L &&
                                    (it.status == null ||
                                            (it.status.status !=
                                                    DownloadManager.STATUS_SUCCESSFUL &&
                                                    it.status.status !=
                                                            DownloadManager.STATUS_FAILED))
                        }

                if (activeDownloads.isNotEmpty()) {
                    val newStatuses = _downloadStatuses.value.toMutableMap()
                    activeDownloads.forEach { uiState ->
                        val status = repository.getDownloadStatus(uiState.item.downloadId)
                        newStatuses[uiState.item.downloadId] = status
                    }
                    _downloadStatuses.value = newStatuses
                }
                delay(1000) // Poll every second
            }
        }
    }

    fun deleteDownload(item: DownloadedItem) {
        viewModelScope.launch {
            if (item.downloadId != -1L) {
                repository.cancelDownload(item.downloadId)
            }
            repository.deleteDownloadedItem(item)
        }
    }
}
