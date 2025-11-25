package com.example.kemono.data.repository;

import com.example.kemono.data.local.FavoriteDao;
import com.example.kemono.data.remote.KemonoApi;
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

  public KemonoRepository_Factory(Provider<KemonoApi> apiProvider,
      Provider<FavoriteDao> favoriteDaoProvider) {
    this.apiProvider = apiProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
  }

  @Override
  public KemonoRepository get() {
    return newInstance(apiProvider.get(), favoriteDaoProvider.get());
  }

  public static KemonoRepository_Factory create(Provider<KemonoApi> apiProvider,
      Provider<FavoriteDao> favoriteDaoProvider) {
    return new KemonoRepository_Factory(apiProvider, favoriteDaoProvider);
  }

  public static KemonoRepository newInstance(KemonoApi api, FavoriteDao favoriteDao) {
    return new KemonoRepository(api, favoriteDao);
  }
}
