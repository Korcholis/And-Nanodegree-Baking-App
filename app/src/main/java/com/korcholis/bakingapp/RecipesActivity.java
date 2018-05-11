package com.korcholis.bakingapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.korcholis.bakingapp.adapters.RecipesListAdapter;
import com.korcholis.bakingapp.exceptions.ConnectionNotAvailableException;
import com.korcholis.bakingapp.models.Ingredient;
import com.korcholis.bakingapp.models.Recipe;
import com.korcholis.bakingapp.models.RecipeStep;
import com.korcholis.bakingapp.provider.RecipesDBContract;
import com.korcholis.bakingapp.provider.RecipesDBHelper;
import com.korcholis.bakingapp.utils.ConnectionChecker;
import com.korcholis.bakingapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RecipesActivity extends CakeActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recipes_rv)
    RecyclerView recipesList;
    @BindView(R.id.loading_pb)
    ProgressBar loadingPb;
    @BindView(R.id.error_layer)
    LinearLayout errorLayer;
    @BindView(R.id.error_message)
    TextView errorMessage;

    private RecipesListAdapter adapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_recipes_list);

        List<Recipe> recipes = new ArrayList<>();

        adapter = new RecipesListAdapter(recipes, this);
        adapter.setOnItemClickListener(new RecipesListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Recipe recipe) {
                Intent intent = new Intent(RecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra(Constants.PARAM_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }
        });
        recipesList.setAdapter(adapter);

        loadRecipes();
    }

    private void loadRecipes() {
        showLoading();
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

                                }
                            }
                        }));
    }

    private Consumer<List<Recipe>> onRecipesListLoaded() {
        return new Consumer<List<Recipe>>() {

            @Override
            public void accept(List<Recipe> recipes) {
                if (recipes == null) {
                    if (!ConnectionChecker.isNetworkAvailable(RecipesActivity.this)) {
                        showNoConnectionErrorToast(false);
                    } else {
                        showErrorListErrorToast(false);
                    }
                } else {
                    adapter.swapContent(recipes);
                    if (recipes.isEmpty()) {
                        if (!ConnectionChecker.isNetworkAvailable(RecipesActivity.this)) {
                            showErrorView(R.string.error_no_connection);
                        } else {
                            showErrorView(R.string.error_recipe_wrong_data);
                        }
                    } else {
                        showList();
                    }
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private void showLoading() {
        errorLayer.setVisibility(View.GONE);
        loadingPb.setVisibility(View.VISIBLE);
        recipesList.setVisibility(View.INVISIBLE);
    }

    private void showList() {
        errorLayer.setVisibility(View.GONE);
        loadingPb.setVisibility(View.GONE);
        recipesList.setVisibility(View.VISIBLE);
    }

    private void showErrorView(int errorMessageId) {
        loadingPb.setVisibility(View.GONE);
        errorLayer.setVisibility(View.VISIBLE);
        errorMessage.setText(errorMessageId);
        recipesList.setVisibility(View.INVISIBLE);
    }

    protected void showErrorListErrorToast(boolean alsoExit) {
        if (alsoExit) {
            finish();
        }
        Toast.makeText(this, R.string.error_recipe_wrong_data, Toast.LENGTH_SHORT).show();
    }


    protected void showNoConnectionErrorToast(boolean alsoExit) {
        if (alsoExit) {
            finish();
        }
        Toast.makeText(this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
    }
}
