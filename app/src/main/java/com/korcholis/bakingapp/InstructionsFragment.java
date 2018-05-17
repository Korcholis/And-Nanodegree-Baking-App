package com.korcholis.bakingapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.korcholis.bakingapp.models.Recipe;
import com.korcholis.bakingapp.models.RecipeStep;
import com.korcholis.bakingapp.provider.RecipesDBContract;
import com.korcholis.bakingapp.provider.RecipesDBHelper;
import com.korcholis.bakingapp.utils.CakeViewPager;
import com.korcholis.bakingapp.utils.Constants;

import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A fragment representing a single Step detail screen.
 * This fragment is either contained in a {@link RecipeDetailActivity}
 * in two-pane mode (on tablets) or a {@link InstructionsActivity}
 * on handsets.
 */
public class InstructionsFragment extends Fragment {

    @BindView(R.id.pager_container)
    CakeViewPager viewPager;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter pagerAdapter;
    /**
     * The dummy content this fragment is presenting.
     */
    private Recipe recipe;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int recipeId;
    private int stepId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InstructionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(Constants.PARAM_RECIPE_ID) && getArguments().containsKey(Constants.PARAM_STEP_ID)) {
            final AppCompatActivity activity = (AppCompatActivity) this.getActivity();

            recipeId = getArguments().getInt(Constants.PARAM_RECIPE_ID);
            stepId = getArguments().getInt(Constants.PARAM_STEP_ID);

            compositeDisposable.add(
                    Observable.fromCallable(new Callable<Recipe>() {
                        @Override
                        public Recipe call() {
                            ContentResolver resolver = activity.getContentResolver();

                            Uri recipeUri = ContentUris.withAppendedId(RecipesDBContract.RecipeEntry.CONTENT_URI, recipeId);
                            Uri ingredientsUri = RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_INGREDIENTS).build();
                            Uri stepsUri = RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_STEPS).build();

                            Cursor recipeCursor = resolver.query(recipeUri, null, null, null, null);
                            Cursor ingredientsCursor = resolver.query(ingredientsUri, null, null, null, null);
                            Cursor stepsCursor = resolver.query(stepsUri, null, null, null, null);

                            recipe = RecipesDBHelper.cursorToRecipeWithExtras(recipeCursor, ingredientsCursor, stepsCursor);

                            return recipe;
                        }
                    })
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Recipe>() {
                                @Override
                                public void accept(Recipe recipe) {
                                    pagerAdapter.swapRecipe(recipe);
                                    viewPager.setCurrentItem(stepId);
                                }
                            })
            );

            pagerAdapter = new SectionsPagerAdapter(activity.getSupportFragmentManager(), null);

        }
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    public void disableFullscreen() {
        if (viewPager != null) {
            viewPager.setPagingEnabled(true);
            getCurrentPageFragment(viewPager.getCurrentItem()).restoreVideo();
        }
    }

    public void enableFullscreen() {
        if (viewPager != null) {
            viewPager.setPagingEnabled(false);
            getCurrentPageFragment(viewPager.getCurrentItem()).maximiseVideo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.step_detail, container, false);
        ButterKnife.bind(this, rootView);

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                getCurrentPageFragment(position).playVideo();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    getCurrentPageFragment(viewPager.getCurrentItem()).pauseVideo();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewPager.getChildCount() > 0) {
            getCurrentPageFragment(viewPager.getCurrentItem()).playVideo();
        }
    }

    @Override
    public void onStop() {
        if (viewPager.getChildCount() > 0) {
            getCurrentPageFragment(viewPager.getCurrentItem()).pauseVideo();
        }
        super.onStop();
    }

    private StepPageFragment getCurrentPageFragment(int position) {
        return ((StepPageFragment) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager_container + ":" + position));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recipe_instructions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean shouldSetFullscreen() {
        return getCurrentPageFragment(viewPager.getCurrentItem()).hasVideo;
    }

    public void goToStep(RecipeStep recipeStep) {
        viewPager.setCurrentItem(recipeStep.getStep());
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    @SuppressWarnings("deprecation")
    public static class StepPageFragment extends Fragment {
        @BindView(R.id.step_description)
        TextView stepDescription;
        @BindView(R.id.video_player)
        SimpleExoPlayerView playerView;
        private SimpleExoPlayer exoPlayer;
        private RecipeStep recipeStep;
        private boolean hasVideo = false;

        public StepPageFragment() {
        }

        public static StepPageFragment newInstance(RecipeStep step, Recipe recipe) {
            StepPageFragment fragment = new StepPageFragment();
            Bundle args = new Bundle();
            args.putParcelable(Constants.PARAM_STEP, step);
            args.putParcelable(Constants.PARAM_RECIPE, recipe);
            fragment.setArguments(args);
            return fragment;
        }

        public void playVideo() {
            if (hasVideo && exoPlayer != null) {
                exoPlayer.setPlayWhenReady(true);
            }
        }

        public void pauseVideo() {
            if (hasVideo && exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
            }
        }

        public void maximiseVideo() {
            ViewGroup.LayoutParams params = playerView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.setLayoutParams(params);
        }

        public void restoreVideo() {
            ViewGroup.LayoutParams params = playerView.getLayoutParams();
            params.height = getActivity().getResources().getDimensionPixelSize(R.dimen.video_height);
            playerView.setLayoutParams(params);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            recipeStep = getArguments().getParcelable(Constants.PARAM_STEP);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_recipe_instructions, container, false);
            ButterKnife.bind(this, rootView);

            stepDescription.setText(recipeStep.getDescription());

            if (!recipeStep.getVideoURL().isEmpty()) {
                playerView.setVisibility(View.VISIBLE);
                initializePlayer(recipeStep.getVideoURL());
                hasVideo = true;
            } else {
                playerView.setVisibility(View.GONE);
                hasVideo = false;
            }
            return rootView;
        }

        @Override
        public void onDestroyView() {
            if (exoPlayer != null) {
                exoPlayer.stop();
                exoPlayer.release();
                exoPlayer = null;
            }
            super.onDestroyView();
        }

        private void initializePlayer(String videoURL) {
            if (exoPlayer == null) {
                TrackSelector trackSelector = new DefaultTrackSelector();
                LoadControl loadControl = new DefaultLoadControl();
                exoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
                playerView.setPlayer(exoPlayer);
            }

            String userAgent = Util.getUserAgent(getContext(), "CakeliciousUdacityExercise");
            Uri videoUri = Uri.parse(videoURL);
            MediaSource mediaSource = new ExtractorMediaSource(videoUri, new DefaultDataSourceFactory(getContext(), userAgent), new DefaultExtractorsFactory(), null, null);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);
            if (recipeStep.getStep() > 0) {
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            } else {
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Recipe recipe;

        public SectionsPagerAdapter(FragmentManager fm, Recipe recipe) {
            super(fm);
            this.recipe = recipe;
        }

        public void swapRecipe(Recipe recipe) {
            this.recipe = recipe;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            return StepPageFragment.newInstance(recipe.getSteps().get(position), recipe);
        }

        @Override
        public int getCount() {
            if (recipe == null) {
                return 0;
            }
            return recipe.getSteps().size();
        }
    }
}
