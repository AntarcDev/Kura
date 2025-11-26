package com.example.kemono.ui.creators;

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

  public CreatorViewModel_Factory(Provider<KemonoRepository> repositoryProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.networkMonitorProvider = networkMonitorProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public CreatorViewModel get() {
    return newInstance(repositoryProvider.get(), networkMonitorProvider.get(), settingsRepositoryProvider.get());
  }

  public static CreatorViewModel_Factory create(Provider<KemonoRepository> repositoryProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new CreatorViewModel_Factory(repositoryProvider, networkMonitorProvider, settingsRepositoryProvider);
  }

  public static CreatorViewModel newInstance(KemonoRepository repository,
      NetworkMonitor networkMonitor, SettingsRepository settingsRepository) {
    return new CreatorViewModel(repository, networkMonitor, settingsRepository);
  }
}
