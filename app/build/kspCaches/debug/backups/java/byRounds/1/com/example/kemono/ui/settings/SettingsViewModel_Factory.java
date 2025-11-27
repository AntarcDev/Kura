package com.example.kemono.ui.settings;

import android.content.Context;
import com.example.kemono.data.local.SessionManager;
import com.example.kemono.data.repository.KemonoRepository;
import com.example.kemono.data.repository.SettingsRepository;
import com.example.kemono.data.repository.UpdateRepository;
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

  private final Provider<UpdateRepository> updateRepositoryProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<KemonoRepository> repositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<UpdateRepository> updateRepositoryProvider, Provider<Context> contextProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.repositoryProvider = repositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.updateRepositoryProvider = updateRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(sessionManagerProvider.get(), okHttpClientProvider.get(), repositoryProvider.get(), settingsRepositoryProvider.get(), updateRepositoryProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<KemonoRepository> repositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<UpdateRepository> updateRepositoryProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(sessionManagerProvider, okHttpClientProvider, repositoryProvider, settingsRepositoryProvider, updateRepositoryProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(SessionManager sessionManager,
      OkHttpClient okHttpClient, KemonoRepository repository, SettingsRepository settingsRepository,
      UpdateRepository updateRepository, Context context) {
    return new SettingsViewModel(sessionManager, okHttpClient, repository, settingsRepository, updateRepository, context);
  }
}
