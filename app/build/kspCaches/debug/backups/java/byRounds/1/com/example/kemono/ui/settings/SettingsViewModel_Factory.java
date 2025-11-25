package com.example.kemono.ui.settings;

import com.example.kemono.data.local.SessionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  public SettingsViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(sessionManagerProvider.get(), okHttpClientProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    return new SettingsViewModel_Factory(sessionManagerProvider, okHttpClientProvider);
  }

  public static SettingsViewModel newInstance(SessionManager sessionManager,
      OkHttpClient okHttpClient) {
    return new SettingsViewModel(sessionManager, okHttpClient);
  }
}
