package com.korcholis.bakingapp.injection;

import com.korcholis.bakingapp.InstructionsActivity;
import com.korcholis.bakingapp.RecipeDetailActivity;
import com.korcholis.bakingapp.RecipesActivity;
import com.korcholis.bakingapp.widget.CakeWidgetProviderConfigureActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ApplicationModule {
    @ContributesAndroidInjector
    abstract RecipesActivity contributeRecipesActivityInjector();
    @ContributesAndroidInjector
    abstract RecipeDetailActivity contributeRecipeDetailActivityInjector();
    @ContributesAndroidInjector
    abstract InstructionsActivity contributeInstructionsActivityInjector();
    @ContributesAndroidInjector
    abstract CakeWidgetProviderConfigureActivity contributeWidgetActivityInjector();
}
