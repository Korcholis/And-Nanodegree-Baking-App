package com.korcholis.bakingapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.korcholis.bakingapp.provider.RecipesApi;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public abstract class CakeActivity extends AppCompatActivity {
    @Inject
    RecipesApi recipes;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
    }
}
