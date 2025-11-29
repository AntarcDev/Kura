package com.example.kemono.data.repository;

import com.example.kemono.data.local.CacheDao;
import com.example.kemono.data.local.FavoriteDao;
import com.example.kemono.data.remote.KemonoApi;
import com.example.kemono.util.NetworkMonitor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class KemonoRepository_Factory implements Factory<KemonoRepository> {
  private final Provider<KemonoApi> apiProvider;

  private final Provider<FavoriteDao> favoriteDaoProvider;

  private final Provider<CacheDao> cacheDaoProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public KemonoRepository_Factory(Provider<KemonoApi> apiProvider,
      Provider<FavoriteDao> favoriteDaoProvider, Provider<CacheDao> cacheDaoProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.apiProvider = apiProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
    this.cacheDaoProvider = cacheDaoProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public KemonoRepository get() {
    return newInstance(apiProvider.get(), favoriteDaoProvider.get(), cacheDaoProvider.get(), networkMonitorProvider.get());
  }

  public static KemonoRepository_Factory create(Provider<KemonoApi> apiProvider,
      Provider<FavoriteDao> favoriteDaoProvider, Provider<CacheDao> cacheDaoProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new KemonoRepository_Factory(apiProvider, favoriteDaoProvider, cacheDaoProvider, networkMonitorProvider);
  }

  public static KemonoRepository newInstance(KemonoApi api, FavoriteDao favoriteDao,
      CacheDao cacheDao, NetworkMonitor networkMonitor) {
    return new KemonoRepository(api, favoriteDao, cacheDao, networkMonitor);
  }
}
