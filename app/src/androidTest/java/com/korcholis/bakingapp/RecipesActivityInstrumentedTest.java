package com.korcholis.bakingapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;

import com.korcholis.bakingapp.utils.Constants;
import com.korcholis.bakingapp.utils.TestConstants;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class RecipesActivityInstrumentedTest {
    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with "+childPosition+" child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }

                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent()) && group.getChildAt(childPosition).equals(view);
            }
        };
    }


    @Rule
    public ActivityTestRule<RecipesActivity> activityTestRule = new ActivityTestRule<>(RecipesActivity.class);

    @Rule
    public ActivityTestRule<RecipeDetailActivity> detailActivityRule
            = new ActivityTestRule<>(
            RecipeDetailActivity.class,
            true,
            false);

    @Rule
    public ActivityTestRule<InstructionsActivity> instructionsActivityRule
            = new ActivityTestRule<>(
            InstructionsActivity.class,
            true,
            false);

    @Before
    public void prepare() {
        Intents.init();
    }

    @After
    public void cleanup() {
        Intents.release();
    }

    @Test
    public void clickRecipeItem_MovesToRecipeDetailActivity() {
        onView(withText(TestConstants.RECIPE_1_NAME)).perform(click());
        intended(hasComponent(RecipeDetailActivity.class.getName()));
        onView(withId(R.id.ingredient_list)).check(matches(hasChildCount(TestConstants.RECIPE_1_INGREDIENT_COUNT)));
        onView(withId(R.id.step_list)).check(matches(hasChildCount(TestConstants.RECIPE_1_STEP_COUNT)));
    }

    @Test
    public void clickRecipeStep_MovesToInstructionsActivity() {
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_RECIPE_ID, TestConstants.RECIPE_1_ID);
        detailActivityRule.launchActivity(intent);

        onView(withText(TestConstants.RECIPE_1_STEP_0_NAME)).perform(click());
        intended(hasComponent(InstructionsActivity.class.getName()));
    }

    @Test
    public void rotateScreen_ShowsVideoOnRotationIsFullscreen() {
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_RECIPE_ID, TestConstants.RECIPE_1_ID);
        intent.putExtra(Constants.PARAM_STEP_ID, TestConstants.RECIPE_1_STEP_0_ID);
        instructionsActivityRule.launchActivity(intent);

        instructionsActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        onView(allOf(withId(R.id.video_player), isDescendantOfA(nthChildOf(withId(R.id.pager_container), 0)))).check(matches(isDisplayed()));
    }

    @Test
    public void swipeInstruction_MovesToTheNextStep() {
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_RECIPE_ID, TestConstants.RECIPE_1_ID);
        intent.putExtra(Constants.PARAM_STEP_ID, TestConstants.RECIPE_1_STEP_0_ID);
        instructionsActivityRule.launchActivity(intent);

        onView(withId(R.id.root)).perform(swipeUp());
        onView(withId(R.id.step_detail_container)).perform(swipeLeft());
    }
}
