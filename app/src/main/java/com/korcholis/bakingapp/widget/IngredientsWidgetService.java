package com.korcholis.bakingapp.widget;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.models.Ingredient;
import com.korcholis.bakingapp.models.Recipe;
import com.korcholis.bakingapp.provider.RecipesDBContract;
import com.korcholis.bakingapp.provider.RecipesDBHelper;

import java.util.ArrayList;
import java.util.List;

public class IngredientsWidgetService extends RemoteViewsService {
    List<Ingredient> ingredients = new ArrayList<>();
    int recipeId;
    Recipe recipe;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new IngredientsRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    public class IngredientsRemoteViewsFactory implements RemoteViewsFactory {
        private Context context;

        public IngredientsRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            recipeId = intent.getExtras().getInt(CakeWidgetProvider.RECIPE_ID);
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            final long identityToken = Binder.clearCallingIdentity();

            ContentResolver resolver = context.getContentResolver();

            Uri recipeUri = ContentUris.withAppendedId(RecipesDBContract.RecipeEntry.CONTENT_URI, recipeId);
            Uri ingredientsUri = RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_INGREDIENTS).build();
            Uri stepsUri = RecipesDBContract.RecipeEntry.CONTENT_URI.buildUpon().appendPath(recipeId + "").appendPath(RecipesDBContract.PATH_STEPS).build();

            Cursor recipeCursor = resolver.query(recipeUri, null, null, null, null);
            Cursor ingredientsCursor = resolver.query(ingredientsUri, null, null, null, null);
            Cursor stepsCursor = resolver.query(stepsUri, null, null, null, null);

            recipe = RecipesDBHelper.cursorToRecipeWithExtras(recipeCursor, ingredientsCursor, stepsCursor);

            ingredients = recipe.getIngredients();

            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return ingredients.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
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
