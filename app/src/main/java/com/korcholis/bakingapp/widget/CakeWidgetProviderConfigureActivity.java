package com.korcholis.bakingapp.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.adapters.RecipesListAdapter;
import com.korcholis.bakingapp.exceptions.ConnectionNotAvailableException;
import com.korcholis.bakingapp.models.Ingredient;
import com.korcholis.bakingapp.models.Recipe;
import com.korcholis.bakingapp.models.RecipeStep;
import com.korcholis.bakingapp.provider.RecipesApi;
import com.korcholis.bakingapp.provider.RecipesDBContract;
import com.korcholis.bakingapp.provider.RecipesDBHelper;
import com.korcholis.bakingapp.utils.ConnectionChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * The configuration screen for the {@link CakeWidgetProvider CakeWidgetProvider} AppWidget.
 */
public class CakeWidgetProviderConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.korcholis.bakingapp.widget.CakeWidgetProvider";
    private static final String PREF_PREFIX_KEY = "cakewidget_";
    private static final String PREF_SUFFIX_ID = "_id";
    private static final String PREF_SUFFIX_NAME = "_name";
    @Inject
    RecipesApi recipes;
    @BindView(R.id.recipes_rv)
    RecyclerView recipesList;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private int mAppWidgetId = 100;
    private RecipesListAdapter adapter;

    public CakeWidgetProviderConfigureActivity() {
        super();
    }

    private static void saveRecipeData(Context context, int appWidgetId, int recipeId, String recipeName) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + PREF_SUFFIX_ID, recipeId);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + PREF_SUFFIX_NAME, recipeName);
        prefs.apply();
    }

    static int loadRecipeId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId + PREF_SUFFIX_ID, -1);
    }

    static String loadRecipeName(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_PREFIX_KEY + appWidgetId + PREF_SUFFIX_NAME, "");
    }

    static void deleteRecipeIdPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + PREF_SUFFIX_ID);
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + PREF_SUFFIX_NAME);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        AndroidInjection.inject(this);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.cake_widget_provider_configure);
        ButterKnife.bind(this);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        List<Recipe> recipes = new ArrayList<>();

        adapter = new RecipesListAdapter(recipes, this);
        adapter.setOnItemClickListener(new RecipesListAdapter.OnItemClickListener() {
            @Override
            public void onClick(final Recipe recipe) {
                final Context context = CakeWidgetProviderConfigureActivity.this;

                // When the button is clicked, store the string locally
                saveRecipeData(context, mAppWidgetId, recipe.getId(), recipe.getName());

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                CakeWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        recipesList.setAdapter(adapter);

        loadRecipes();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private void loadRecipes() {
        compositeDisposable.add(
                Observable.fromCallable(new Callable<List<Recipe>>() {
                    @Override
                    public List<Recipe> call() {
                        ContentResolver resolver = getContentResolver();

                        Uri recipesUri = RecipesDBContract.RecipeEntry.CONTENT_URI;

                        Cursor recipeCursor = resolver.query(recipesUri, null, null, null, null);

                        return RecipesDBHelper.cursorToRecipes(recipeCursor);
                    }
                })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onRecipesListLoaded(), new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                if (throwable instanceof ConnectionNotAvailableException) {
                                    throwable.printStackTrace();
                                }
                            }
                        })
        );

        compositeDisposable.add(
                recipes.api().recipes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess(new Consumer<List<Recipe>>() {
                            @Override
                            public void accept(List<Recipe> recipes) {
                                ContentResolver resolver = getContentResolver();
                                for (final Recipe recipe : recipes) {
                                    ContentValues recipeCV = new ContentValues();
                                    recipeCV.put(RecipesDBContract.RecipeEntry.COLUMN_REMOTE_ID, recipe.getId());
                                    recipeCV.put(RecipesDBContract.RecipeEntry.COLUMN_NAME, recipe.getName());
                                    recipeCV.put(RecipesDBContract.RecipeEntry.COLUMN_IMAGE, recipe.getImage());
                                    recipeCV.put(RecipesDBContract.RecipeEntry.COLUMN_SERVINGS, recipe.getServings());
                                    try {
                                        Uri recipeUri = resolver.insert(RecipesDBContract.RecipeEntry.CONTENT_URI, recipeCV);
                                        String recipeId = recipeUri.getLastPathSegment();

                                        for (Ingredient ingredient : recipe.getIngredients()) {
                                            ContentValues ingredientCV = new ContentValues();
                                            ingredientCV.put(RecipesDBContract.IngredientEntry.COLUMN_INGREDIENT, ingredient.getName());
                                            ingredientCV.put(RecipesDBContract.IngredientEntry.COLUMN_MEASURE, ingredient.getMeasure());
                                            ingredientCV.put(RecipesDBContract.IngredientEntry.COLUMN_QUANTITY, ingredient.getQuantity());
                                            resolver.insert(RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId).appendPath(RecipesDBContract.PATH_INGREDIENTS).build(), ingredientCV);
                                        }

                                        for (RecipeStep step : recipe.getSteps()) {
                                            ContentValues stepCV = new ContentValues();
                                            stepCV.put(RecipesDBContract.RecipeStepEntry.COLUMN_DESCRIPTION, step.getDescription());
                                            stepCV.put(RecipesDBContract.RecipeStepEntry.COLUMN_SHORT_DESCRIPTION, step.getShortDescription());
                                            stepCV.put(RecipesDBContract.RecipeStepEntry.COLUMN_STEP, step.getStep());
                                            stepCV.put(RecipesDBContract.RecipeStepEntry.COLUMN_THUMBNAIL_URL, step.getThumbnailURL());
                                            stepCV.put(RecipesDBContract.RecipeStepEntry.COLUMN_VIDEO_URL, step.getVideoURL());
                                            resolver.insert(RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId).appendPath(RecipesDBContract.PATH_STEPS).build(), stepCV);
                                        }
                                    } catch (SQLiteConstraintException constraintException) {
                                        constraintException.printStackTrace();
                                    }
                                }
                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                compositeDisposable.add(
                                        Observable.fromCallable(new Callable<List<Recipe>>() {
                                            @Override
                                            public List<Recipe> call() {
                                                List<Recipe> recipes;
                                                ContentResolver resolver = getContentResolver();
                                                Cursor cursor = resolver.query(RecipesDBContract.RecipeEntry.CONTENT_URI, null, null, null, null);
                                                recipes = RecipesDBHelper.cursorToRecipes(cursor);
                                                return recipes;
                                            }
                                        })
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(onRecipesListLoaded())
                                );
                            }
                        })
                        .subscribe(onRecipesListLoaded(), new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                if (throwable instanceof ConnectionNotAvailableException) {
                                    throwable.printStackTrace();
                                }
                            }
                        }));
    }

    private Consumer<List<Recipe>> onRecipesListLoaded() {
        return new Consumer<List<Recipe>>() {

            @Override
            public void accept(List<Recipe> recipes) {
                if (recipes == null) {
                    if (!ConnectionChecker.isNetworkAvailable(CakeWidgetProviderConfigureActivity.this)) {
                        Toast.makeText(CakeWidgetProviderConfigureActivity.this, "No network", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    adapter.swapContent(recipes);
                    if (!recipes.isEmpty()) {
                        showList();
                    }
                }
            }
        };
    }

    private void showList() {
        recipesList.setVisibility(View.VISIBLE);
    }
}

