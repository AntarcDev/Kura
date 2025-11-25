package com.example.kemono;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = KemonoApp.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface KemonoApp_GeneratedInjector {
  void injectKemonoApp(KemonoApp kemonoApp);
}
