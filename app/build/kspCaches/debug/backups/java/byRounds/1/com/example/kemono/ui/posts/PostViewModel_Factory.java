package com.example.kemono.ui.posts;

import android.app.Application;
import androidx.lifecycle.SavedStateHandle;
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
public final class PostViewModel_Factory implements Factory<PostViewModel> {
  private final Provider<KemonoRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<Application> applicationProvider;

  public PostViewModel_Factory(Provider<KemonoRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<Application> applicationProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public PostViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get(), applicationProvider.get());
  }

  public static PostViewModel_Factory create(Provider<KemonoRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<Application> applicationProvider) {
    return new PostViewModel_Factory(repositoryProvider, savedStateHandleProvider, applicationProvider);
  }

  public static PostViewModel newInstance(KemonoRepository repository,
      SavedStateHandle savedStateHandle, Application application) {
    return new PostViewModel(repository, savedStateHandle, application);
  }
}
