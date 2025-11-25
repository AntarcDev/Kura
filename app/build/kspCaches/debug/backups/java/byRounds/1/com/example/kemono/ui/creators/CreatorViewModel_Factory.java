package com.example.kemono.ui.creators;

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
public final class CreatorViewModel_Factory implements Factory<CreatorViewModel> {
  private final Provider<KemonoRepository> repositoryProvider;

  public CreatorViewModel_Factory(Provider<KemonoRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CreatorViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CreatorViewModel_Factory create(Provider<KemonoRepository> repositoryProvider) {
    return new CreatorViewModel_Factory(repositoryProvider);
  }

  public static CreatorViewModel newInstance(KemonoRepository repository) {
    return new CreatorViewModel(repository);
  }
}
