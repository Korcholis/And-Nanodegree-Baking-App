package com.korcholis.bakingapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.korcholis.bakingapp.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single Step detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link RecipeDetailActivity}.
 */
public class InstructionsActivity extends CakeActivity {

    @BindView(R.id.detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @Nullable
    @BindView(R.id.fragment_scroll_wrapper)
    NestedScrollView fragmentScrollWrapper;
    private int previousAppBarSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_detail);
        ButterKnife.bind(this);

        prepareUI();

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(Constants.PARAM_RECIPE_ID,
                    getIntent().getIntExtra(Constants.PARAM_RECIPE_ID, Constants.DEFAULT_RECIPE_ID));
            arguments.putInt(Constants.PARAM_STEP_ID,
                    getIntent().getIntExtra(Constants.PARAM_STEP_ID, Constants.DEFAULT_STEP_ID));
            InstructionsFragment fragment = new InstructionsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.step_detail_container, fragment)
                    .commit();
        }
    }

    private void prepareUI() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (fragmentScrollWrapper != null) {
            fragmentScrollWrapper.setFillViewport(true);
        }

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void switchToFullscreenOrNot() {
        if (((InstructionsFragment)getSupportFragmentManager().findFragmentById(R.id.step_detail_container)).shouldSetFullscreen()) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                fab.setVisibility(View.GONE);
                appBar.animate().translationY(-appBar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                ViewGroup.LayoutParams params = appBar.getLayoutParams();
                previousAppBarSize = params.height;
                params.height = 0;
                appBar.setLayoutParams(params);
                ((InstructionsFragment)getSupportFragmentManager().findFragmentById(R.id.step_detail_container)).enableFullscreen();
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                appBar.setExpanded(true, false);
                fab.setVisibility(View.VISIBLE);
                appBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                ViewGroup.LayoutParams params = appBar.getLayoutParams();
                params.height = previousAppBarSize;
                previousAppBarSize = 0;
                appBar.setLayoutParams(params);
                ((InstructionsFragment)getSupportFragmentManager().findFragmentById(R.id.step_detail_container)).disableFullscreen();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, RecipeDetailActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switchToFullscreenOrNot();
    }
}
