package com.example.kemono.di;

import com.example.kemono.data.remote.KemonoApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideKemonoApiFactory implements Factory<KemonoApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideKemonoApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public KemonoApi get() {
    return provideKemonoApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideKemonoApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideKemonoApiFactory(retrofitProvider);
  }

  public static KemonoApi provideKemonoApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideKemonoApi(retrofit));
  }
}
