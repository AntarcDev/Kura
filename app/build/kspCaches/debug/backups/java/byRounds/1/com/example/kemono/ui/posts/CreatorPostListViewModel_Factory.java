package com.example.kemono.ui.posts;

import androidx.lifecycle.SavedStateHandle;
import com.example.kemono.data.repository.DownloadRepository;
import com.example.kemono.data.repository.KemonoRepository;
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
public final class CreatorPostListViewModel_Factory implements Factory<CreatorPostListViewModel> {
  private final Provider<KemonoRepository> repositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public CreatorPostListViewModel_Factory(Provider<KemonoRepository> repositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public CreatorPostListViewModel get() {
    return newInstance(repositoryProvider.get(), downloadRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static CreatorPostListViewModel_Factory create(
      Provider<KemonoRepository> repositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new CreatorPostListViewModel_Factory(repositoryProvider, downloadRepositoryProvider, savedStateHandleProvider);
  }

  public static CreatorPostListViewModel newInstance(KemonoRepository repository,
      DownloadRepository downloadRepository, SavedStateHandle savedStateHandle) {
    return new CreatorPostListViewModel(repository, downloadRepository, savedStateHandle);
  }
}
