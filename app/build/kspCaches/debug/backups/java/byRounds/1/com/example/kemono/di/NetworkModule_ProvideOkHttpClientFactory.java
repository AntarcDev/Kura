package com.example.kemono.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<CookieJar> cookieJarProvider;

  public NetworkModule_ProvideOkHttpClientFactory(Provider<CookieJar> cookieJarProvider) {
    this.cookieJarProvider = cookieJarProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(cookieJarProvider.get());
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(
      Provider<CookieJar> cookieJarProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(cookieJarProvider);
  }

  public static OkHttpClient provideOkHttpClient(CookieJar cookieJar) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideOkHttpClient(cookieJar));
  }
}
