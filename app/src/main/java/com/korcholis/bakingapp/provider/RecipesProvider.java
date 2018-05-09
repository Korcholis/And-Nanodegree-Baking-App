package com.korcholis.bakingapp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RecipesProvider extends ContentProvider {
    private RecipesDBHelper dbHelper;

    public static final int RECIPES = 100;
    public static final int RECIPE_WITH_ID = 101;

    public static final int INGREDIENTS = 200;

    public static final int RECIPE_STEPS = 300;
    public static final int RECIPE_STEP_WITH_ID = 301;

    public static final UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(RecipesDBContract.AUTHORITY, RecipesDBContract.PATH_RECIPES, RECIPES);
        uriMatcher.addURI(RecipesDBContract.AUTHORITY, RecipesDBContract.PATH_RECIPES + "/#/" + RecipesDBContract.PATH_INGREDIENTS, INGREDIENTS);
        uriMatcher.addURI(RecipesDBContract.AUTHORITY, RecipesDBContract.PATH_RECIPES + "/#/" + RecipesDBContract.PATH_STEPS, RECIPE_STEPS);

        uriMatcher.addURI(RecipesDBContract.AUTHORITY, RecipesDBContract.PATH_RECIPES + "/#", RECIPE_WITH_ID);
        uriMatcher.addURI(RecipesDBContract.AUTHORITY, RecipesDBContract.PATH_STEPS + "/#", RECIPE_STEP_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new RecipesDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        switch (match) {
            case RECIPES:
                return db.query(
                        RecipesDBContract.RecipeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case RECIPE_WITH_ID:
                String recipeId = uri.getPathSegments().get(1);
                return db.query(
                        RecipesDBContract.RecipeEntry.TABLE_NAME,
                        projection,
                        RecipesDBContract.RecipeEntry._ID + "=" + recipeId,
                        null,
                        null,
                        null,
                        sortOrder);
            case INGREDIENTS:
                String recipeIdForIngredients = uri.getPathSegments().get(1);
                return db.query(
                        RecipesDBContract.IngredientEntry.TABLE_NAME,
                        projection,
                        RecipesDBContract.IngredientEntry.COLUMN_RECIPE_ID + "=" + recipeIdForIngredients,
                        null,
                        null,
                        null,
                        sortOrder);
            case RECIPE_STEPS:
                String recipeIdForSteps = uri.getPathSegments().get(1);
                return db.query(
                        RecipesDBContract.RecipeStepEntry.TABLE_NAME,
                        projection,
                        RecipesDBContract.RecipeStepEntry.COLUMN_RECIPE_ID + "=" + recipeIdForSteps,
                        null,
                        null,
                        null,
                        sortOrder);
            case RECIPE_STEP_WITH_ID:
                String stepId = uri.getPathSegments().get(1);
                return db.query(
                        RecipesDBContract.RecipeStepEntry.TABLE_NAME,
                        projection,
                        RecipesDBContract.RecipeStepEntry._ID + "=" + stepId,
                        null,
                        null,
                        null,
                        sortOrder);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (match) {
            case RECIPES:
                try {
                    long newId = db.insertOrThrow(RecipesDBContract.RecipeEntry.TABLE_NAME, null, contentValues);
                    if (newId > 0) {
                        return ContentUris.withAppendedId(RecipesDBContract.RecipeEntry.CONTENT_URI, newId);
                    } else {
                        return ContentUris.withAppendedId(RecipesDBContract.RecipeEntry.CONTENT_URI, 0);
                    }
                } catch(SQLiteConstraintException exception) {
                    return ContentUris.withAppendedId(RecipesDBContract.RecipeEntry.CONTENT_URI, 0);
                }
            case INGREDIENTS:
                String recipeIdForIngredients = uri.getPathSegments().get(1);

                contentValues.put(RecipesDBContract.IngredientEntry.COLUMN_RECIPE_ID, recipeIdForIngredients);

                try {
                    long newId = db.insertOrThrow(RecipesDBContract.IngredientEntry.TABLE_NAME, null, contentValues);
                    if (newId > 0) {
                        return ContentUris.withAppendedId(RecipesDBContract.IngredientEntry.CONTENT_URI, newId);
                    } else {
                        return ContentUris.withAppendedId(RecipesDBContract.IngredientEntry.CONTENT_URI, 0);
                    }
                } catch(SQLiteConstraintException exception) {
                    return ContentUris.withAppendedId(RecipesDBContract.IngredientEntry.CONTENT_URI, 0);
                }
            case RECIPE_STEPS:
                String recipeIdForSteps = uri.getPathSegments().get(1);

                contentValues.put(RecipesDBContract.RecipeStepEntry.COLUMN_RECIPE_ID, recipeIdForSteps);

                try {
                    long newId = db.insertOrThrow(RecipesDBContract.RecipeStepEntry.TABLE_NAME, null, contentValues);
                    if (newId > 0) {
                        return ContentUris.withAppendedId(RecipesDBContract.RecipeStepEntry.CONTENT_URI, newId);
                    } else {
                        return ContentUris.withAppendedId(RecipesDBContract.RecipeStepEntry.CONTENT_URI, 0);
                    }
                } catch(SQLiteConstraintException exception) {
                    return ContentUris.withAppendedId(RecipesDBContract.RecipeStepEntry.CONTENT_URI, 0);
                }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (match) {
            case RECIPES:

                return 0;
            case RECIPE_WITH_ID:
                String recipeId = uri.getPathSegments().get(1);

                return 0;
            case INGREDIENTS:
                String recipeIdForIngredients = uri.getPathSegments().get(1);

                return 0;
            case RECIPE_STEPS:
                String recipeIdForSteps = uri.getPathSegments().get(1);

                return 0;
            case RECIPE_STEP_WITH_ID:
                String stepId = uri.getPathSegments().get(1);

                return 0;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
