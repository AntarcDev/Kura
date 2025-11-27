package com.example.kemono.ui.creators;

import com.example.kemono.data.repository.DownloadRepository;
import com.example.kemono.data.repository.KemonoRepository;
import com.example.kemono.data.repository.SettingsRepository;
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
public final class CreatorViewModel_Factory implements Factory<CreatorViewModel> {
  private final Provider<KemonoRepository> repositoryProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  public CreatorViewModel_Factory(Provider<KemonoRepository> repositoryProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.networkMonitorProvider = networkMonitorProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
  }

  @Override
  public CreatorViewModel get() {
    CreatorViewModel instance = newInstance(repositoryProvider.get(), networkMonitorProvider.get(), settingsRepositoryProvider.get());
    CreatorViewModel_MembersInjector.injectDownloadRepository(instance, downloadRepositoryProvider.get());
    return instance;
  }

  public static CreatorViewModel_Factory create(Provider<KemonoRepository> repositoryProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider) {
    return new CreatorViewModel_Factory(repositoryProvider, networkMonitorProvider, settingsRepositoryProvider, downloadRepositoryProvider);
  }

  public static CreatorViewModel newInstance(KemonoRepository repository,
      NetworkMonitor networkMonitor, SettingsRepository settingsRepository) {
    return new CreatorViewModel(repository, networkMonitor, settingsRepository);
  }
}
