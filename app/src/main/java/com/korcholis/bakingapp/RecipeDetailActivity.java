package com.korcholis.bakingapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.korcholis.bakingapp.adapters.IngredientsAdapter;
import com.korcholis.bakingapp.adapters.RecipeStepsAdapter;
import com.korcholis.bakingapp.models.Ingredient;
import com.korcholis.bakingapp.models.Recipe;
import com.korcholis.bakingapp.models.RecipeStep;
import com.korcholis.bakingapp.provider.RecipesDBContract;
import com.korcholis.bakingapp.provider.RecipesDBHelper;
import com.korcholis.bakingapp.utils.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

/**
 * An activity representing a list of Steps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link InstructionsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class RecipeDetailActivity extends CakeActivity {
    private static final String LIST_SCROLL_POSITION = "list_scroll_pos";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    @BindView(R.id.parent_scroll_view)
    NestedScrollView parentScrollView;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.step_item_container)
    View stepPanel;
    @BindView(R.id.ingredient_list)
    RecyclerView ingredientsList;
    @BindView(R.id.step_list)
    RecyclerView stepsList;
    private int savedScrollPos = 0;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private IngredientsAdapter ingredientsAdapter;
    private RecipeStepsAdapter stepsAdapter;
    private int recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recipeId = getIntent().getIntExtra(Constants.PARAM_RECIPE_ID, Constants.DEFAULT_RECIPE_ID);

        if (recipeId == Constants.DEFAULT_RECIPE_ID) {
            finish();
            return;
        }

        if (stepPanel != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        assert stepsList != null;
        assert ingredientsList != null;

        setupRecyclerView(ingredientsList, stepsList);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(LIST_SCROLL_POSITION, parentScrollView.getScrollY());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        savedScrollPos = savedInstanceState.getInt(LIST_SCROLL_POSITION, 0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView ingredientsList, @NonNull RecyclerView stepsList) {
        ingredientsAdapter = new IngredientsAdapter(new ArrayList<Ingredient>());
        stepsAdapter = new RecipeStepsAdapter(new ArrayList<RecipeStep>(), mTwoPane);

        stepsAdapter.setOnItemClickListener(new RecipeStepsAdapter.OnItemClickListener() {
            @Override
            public void onClick(RecipeStep recipeStep) {
                if (mTwoPane) {
                    changePageInTwoPane(recipeStep);
                } else {
                    Intent intent = new Intent(getBaseContext(), InstructionsActivity.class);

                    intent.putExtra(Constants.PARAM_RECIPE_ID, recipeStep.getRecipeId());
                    intent.putExtra(Constants.PARAM_STEP_ID, recipeStep.getStep());

                    startActivity(intent);
                }
            }
        });

        ingredientsList.setAdapter(ingredientsAdapter);
        stepsList.setAdapter(stepsAdapter);

        ContentResolver resolver = getContentResolver();

        Uri recipeUri = ContentUris.withAppendedId(RecipesDBContract.RecipeEntry.CONTENT_URI, recipeId);
        Uri ingredientsUri = RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_INGREDIENTS).build();
        Uri stepsUri = RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_STEPS).build();

        Cursor recipeCursor = resolver.query(recipeUri, null, null, null, null);
        Cursor ingredientsCursor = resolver.query(ingredientsUri, null, null, null, null);
        Cursor stepsCursor = resolver.query(stepsUri, null, null, null, null);

        Recipe recipe = RecipesDBHelper.cursorToRecipeWithExtras(recipeCursor, ingredientsCursor, stepsCursor);
        setTitle(recipe.getName());
        ingredientsAdapter.swapContent(recipe.getIngredients());
        stepsAdapter.swapContent(recipe.getSteps());
        initViewPagerInTwoPane();

        parentScrollView.scrollTo(0, savedScrollPos);
    }

    private void changePageInTwoPane(RecipeStep recipeStep) {
        InstructionsFragment fragment = (InstructionsFragment) getSupportFragmentManager().findFragmentById(R.id.step_item_container);
        fragment.goToStep(recipeStep);
    }

    private void initViewPagerInTwoPane() {
        if (!mTwoPane) {
            return;
        }

        Bundle arguments = new Bundle();
        arguments.putInt(Constants.PARAM_RECIPE_ID, recipeId);
        arguments.putInt(Constants.PARAM_STEP_ID, 0);
        InstructionsFragment fragment = new InstructionsFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.step_item_container, fragment)
                .commit();
    }
}
