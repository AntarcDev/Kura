package com.example.kemono.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SessionManager_Factory implements Factory<SessionManager> {
  private final Provider<Context> contextProvider;

  public SessionManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SessionManager get() {
    return newInstance(contextProvider.get());
  }

  public static SessionManager_Factory create(Provider<Context> contextProvider) {
    return new SessionManager_Factory(contextProvider);
  }

  public static SessionManager newInstance(Context context) {
    return new SessionManager(context);
  }
}
