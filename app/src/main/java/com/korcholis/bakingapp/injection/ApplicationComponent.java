package com.korcholis.bakingapp.injection;

import com.korcholis.bakingapp.CakeApplication;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Component(modules = { AndroidInjectionModule.class, ApplicationModule.class, ContextModule.class })
public interface ApplicationComponent extends AndroidInjector<CakeApplication> {
}
