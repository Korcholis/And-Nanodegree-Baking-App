package com.korcholis.bakingapp.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.models.Ingredient;
import com.korcholis.bakingapp.models.Recipe;
import com.korcholis.bakingapp.widget.CakeWidgetProvider;

import java.util.ArrayList;
import java.util.List;

public class IngredientsWidgetService extends RemoteViewsService {
    public static final String TAG = "[CWP][IWS]";
    List<Ingredient> ingredients = new ArrayList<>();
    int recipeId;
    Recipe recipe;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i(TAG, "onGetViewFactory");
        return new IngredientsRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    public class IngredientsRemoteViewsFactory implements RemoteViewsFactory {
        private Context context;

        public IngredientsRemoteViewsFactory(Context context, Intent intent) {
            Log.i(TAG, "Factory");
            this.context = context;
            recipeId = intent.getExtras().getInt(CakeWidgetProvider.RECIPE_ID);
        }

        @Override
        public void onCreate() {
            Log.i(TAG, "onCreate");
        }

        @Override
        public void onDataSetChanged() {
            Log.i(TAG, "onDataSetChanged");
            ContentResolver resolver = context.getContentResolver();

            Cursor recipeCursor = resolver.query(ContentUris.withAppendedId(RecipesDBContract.RecipeEntry.CONTENT_URI, recipeId), null, null, null, null);
            Cursor ingredientsCursor = resolver.query(RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_INGREDIENTS).build(), null, null, null, null);
            Cursor stepsCursor = resolver.query(RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_STEPS).build(), null, null, null, null);

            recipe = RecipesDBHelper.cursorToRecipeWithExtras(recipeCursor, ingredientsCursor, stepsCursor);

            ingredients = recipe.getIngredients();
            Log.i(TAG, "accept: " + ingredients.size());
        }

        @Override
        public void onDestroy() {
            Log.i(TAG, "onDestroy");
        }

        @Override
        public int getCount() {
            return ingredients.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            Log.i(TAG, "getViewAt");
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

            String ingredient = ingredients.get(i).getName();
            rv.setTextViewText(R.id.name_tv, ingredient);

            Bundle extras = new Bundle();
            extras.putInt(CakeWidgetProvider.RECIPE_ID, i);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("ingredient", ingredient);
            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.item_layout, fillInIntent);
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
