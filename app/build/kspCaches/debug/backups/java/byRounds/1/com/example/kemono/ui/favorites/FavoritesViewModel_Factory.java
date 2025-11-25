package com.example.kemono.ui.favorites;

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
public final class FavoritesViewModel_Factory implements Factory<FavoritesViewModel> {
  private final Provider<KemonoRepository> repositoryProvider;

  public FavoritesViewModel_Factory(Provider<KemonoRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public FavoritesViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static FavoritesViewModel_Factory create(Provider<KemonoRepository> repositoryProvider) {
    return new FavoritesViewModel_Factory(repositoryProvider);
  }

  public static FavoritesViewModel newInstance(KemonoRepository repository) {
    return new FavoritesViewModel(repository);
  }
}
