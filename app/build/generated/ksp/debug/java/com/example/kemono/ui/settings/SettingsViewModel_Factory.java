package com.example.kemono.ui.settings;

import android.content.Context;
import com.example.kemono.data.local.SessionManager;
import com.example.kemono.data.repository.KemonoRepository;
import com.example.kemono.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<KemonoRepository> repositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<KemonoRepository> repositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider, Provider<Context> contextProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.repositoryProvider = repositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(sessionManagerProvider.get(), okHttpClientProvider.get(), repositoryProvider.get(), settingsRepositoryProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<KemonoRepository> repositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(sessionManagerProvider, okHttpClientProvider, repositoryProvider, settingsRepositoryProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(SessionManager sessionManager,
      OkHttpClient okHttpClient, KemonoRepository repository, SettingsRepository settingsRepository,
      Context context) {
    return new SettingsViewModel(sessionManager, okHttpClient, repository, settingsRepository, context);
  }
}
