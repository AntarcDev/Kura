package com.example.kemono.di;

import android.content.Context;
import com.example.kemono.data.local.SessionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.CookieJar;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_ProvideCookieJarFactory implements Factory<CookieJar> {
  private final Provider<Context> contextProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public NetworkModule_ProvideCookieJarFactory(Provider<Context> contextProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.contextProvider = contextProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public CookieJar get() {
    return provideCookieJar(contextProvider.get(), sessionManagerProvider.get());
  }

  public static NetworkModule_ProvideCookieJarFactory create(Provider<Context> contextProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new NetworkModule_ProvideCookieJarFactory(contextProvider, sessionManagerProvider);
  }

  public static CookieJar provideCookieJar(Context context, SessionManager sessionManager) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideCookieJar(context, sessionManager));
  }
}
