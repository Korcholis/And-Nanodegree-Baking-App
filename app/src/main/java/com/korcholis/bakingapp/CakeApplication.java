package com.korcholis.bakingapp;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;

import com.korcholis.bakingapp.injection.ContextModule;
import com.korcholis.bakingapp.injection.DaggerApplicationComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasContentProviderInjector;

public class CakeApplication extends Application implements HasActivityInjector, HasContentProviderInjector {
    @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject DispatchingAndroidInjector<ContentProvider> dispatchingProviderInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<ContentProvider> contentProviderInjector() {
        return dispatchingProviderInjector;
    }
}
