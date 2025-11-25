package com.example.kemono.ui.posts;

import androidx.lifecycle.SavedStateHandle;
import com.example.kemono.data.repository.DownloadRepository;
import com.example.kemono.data.repository.KemonoRepository;
import com.example.kemono.util.NetworkMonitor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class PostViewModel_Factory implements Factory<PostViewModel> {
  private final Provider<KemonoRepository> repositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public PostViewModel_Factory(Provider<KemonoRepository> repositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.repositoryProvider = repositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public PostViewModel get() {
    return newInstance(repositoryProvider.get(), downloadRepositoryProvider.get(), savedStateHandleProvider.get(), networkMonitorProvider.get());
  }

  public static PostViewModel_Factory create(Provider<KemonoRepository> repositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new PostViewModel_Factory(repositoryProvider, downloadRepositoryProvider, savedStateHandleProvider, networkMonitorProvider);
  }

  public static PostViewModel newInstance(KemonoRepository repository,
      DownloadRepository downloadRepository, SavedStateHandle savedStateHandle,
      NetworkMonitor networkMonitor) {
    return new PostViewModel(repository, downloadRepository, savedStateHandle, networkMonitor);
  }
}
