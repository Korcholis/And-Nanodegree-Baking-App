package com.korcholis.bakingapp.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.korcholis.bakingapp.models.Ingredient;
import com.korcholis.bakingapp.models.Recipe;
import com.korcholis.bakingapp.models.RecipeStep;

import java.util.ArrayList;
import java.util.List;

public class RecipesDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "baking.db";
    public static final int DB_VERSION = 1;

    public RecipesDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_RECIPES_TABLE = "CREATE TABLE " +
                RecipesDBContract.RecipeEntry.TABLE_NAME + " (" +
                RecipesDBContract.RecipeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RecipesDBContract.RecipeEntry.COLUMN_REMOTE_ID + " INTEGER UNIQUE NOT NULL, " +
                RecipesDBContract.RecipeEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                RecipesDBContract.RecipeEntry.COLUMN_IMAGE + " TEXT, " +
                RecipesDBContract.RecipeEntry.COLUMN_SERVINGS + " INTEGER DEFAULT 0" +
                ");";

        final String SQL_CREATE_INGREDIENTS_TABLE = "CREATE TABLE " +
                RecipesDBContract.IngredientEntry.TABLE_NAME + " (" +
                RecipesDBContract.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RecipesDBContract.IngredientEntry.COLUMN_INGREDIENT + " TEXT NOT NULL, " +
                RecipesDBContract.IngredientEntry.COLUMN_MEASURE + " TEXT NOT NULL, " +
                RecipesDBContract.IngredientEntry.COLUMN_QUANTITY + " REAL NOT NULL, " +
                RecipesDBContract.IngredientEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + RecipesDBContract.IngredientEntry.COLUMN_RECIPE_ID + ") " +
                "REFERENCES " + RecipesDBContract.RecipeEntry.TABLE_NAME + "(" + RecipesDBContract.RecipeEntry._ID + ")" +
                ");";

        final String SQL_CREATE_RECIPE_STEPS_TABLE = "CREATE TABLE " +
                RecipesDBContract.RecipeStepEntry.TABLE_NAME + " (" +
                RecipesDBContract.RecipeStepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RecipesDBContract.RecipeStepEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                RecipesDBContract.RecipeStepEntry.COLUMN_SHORT_DESCRIPTION + " TEXT NOT NULL, " +
                RecipesDBContract.RecipeStepEntry.COLUMN_STEP + " INTEGER NOT NULL, " +
                RecipesDBContract.RecipeStepEntry.COLUMN_THUMBNAIL_URL + " TEXT, " +
                RecipesDBContract.RecipeStepEntry.COLUMN_VIDEO_URL + " TEXT, " +
                RecipesDBContract.RecipeStepEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + RecipesDBContract.RecipeStepEntry.COLUMN_RECIPE_ID + ") " +
                "REFERENCES " + RecipesDBContract.RecipeEntry.TABLE_NAME + "(" + RecipesDBContract.RecipeEntry._ID + ")" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_RECIPES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_INGREDIENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RECIPE_STEPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        final String SQL_DROP_RECIPES_TABLE = "DROP TABLE IF EXISTS " + RecipesDBContract.RecipeEntry.TABLE_NAME;
        final String SQL_DROP_INGREDIENTS_TABLE = "DROP TABLE IF EXISTS " + RecipesDBContract.IngredientEntry.TABLE_NAME;
        final String SQL_DROP_RECIPE_STEPS_TABLE = "DROP TABLE IF EXISTS " + RecipesDBContract.RecipeStepEntry.TABLE_NAME;
        sqLiteDatabase.execSQL(SQL_DROP_RECIPES_TABLE);
        sqLiteDatabase.execSQL(SQL_DROP_INGREDIENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_DROP_RECIPE_STEPS_TABLE);
        onCreate(sqLiteDatabase);
    }

    public static List<Recipe> cursorToRecipes(Cursor cursor) {
        List<Recipe> recipes = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                Recipe recipe = new Recipe(
                    cursor.getInt(cursor.getColumnIndex(RecipesDBContract.RecipeEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(RecipesDBContract.RecipeEntry.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(RecipesDBContract.RecipeEntry.COLUMN_IMAGE)),
                    cursor.getInt(cursor.getColumnIndex(RecipesDBContract.RecipeEntry.COLUMN_SERVINGS)),
                    new ArrayList<Ingredient>(),
                    new ArrayList<RecipeStep>()
                );
                recipes.add(recipe);
                cursor.moveToNext();
            }
        }
        return recipes;
    }

    public static Recipe cursorToRecipeWithExtras(Cursor recipeCursor, Cursor ingredientsCursor, Cursor stepsCursor) {
        List<Ingredient> ingredients = new ArrayList<>();
        List<RecipeStep> steps = new ArrayList<>();
        if (ingredientsCursor.moveToFirst()) {
            while(!ingredientsCursor.isAfterLast()) {
                ingredients.add(new Ingredient(
                        ingredientsCursor.getString(ingredientsCursor.getColumnIndex(RecipesDBContract.IngredientEntry.COLUMN_INGREDIENT)),
                        ingredientsCursor.getString(ingredientsCursor.getColumnIndex(RecipesDBContract.IngredientEntry.COLUMN_MEASURE)),
                        ingredientsCursor.getFloat(ingredientsCursor.getColumnIndex(RecipesDBContract.IngredientEntry.COLUMN_QUANTITY))
                ));
                ingredientsCursor.moveToNext();
            }
        }
        if (stepsCursor.moveToFirst()) {
            while(!stepsCursor.isAfterLast()) {
                steps.add(new RecipeStep(
                        stepsCursor.getInt(stepsCursor.getColumnIndex(RecipesDBContract.RecipeStepEntry.COLUMN_STEP)),
                        stepsCursor.getString(stepsCursor.getColumnIndex(RecipesDBContract.RecipeStepEntry.COLUMN_DESCRIPTION)),
                        stepsCursor.getString(stepsCursor.getColumnIndex(RecipesDBContract.RecipeStepEntry.COLUMN_SHORT_DESCRIPTION)),
                        stepsCursor.getString(stepsCursor.getColumnIndex(RecipesDBContract.RecipeStepEntry.COLUMN_THUMBNAIL_URL)),
                        stepsCursor.getString(stepsCursor.getColumnIndex(RecipesDBContract.RecipeStepEntry.COLUMN_VIDEO_URL)),
                        stepsCursor.getInt(stepsCursor.getColumnIndex(RecipesDBContract.RecipeStepEntry.COLUMN_RECIPE_ID))
                ));
                stepsCursor.moveToNext();
            }
        }
        if (recipeCursor.moveToFirst()) {
            Recipe recipe = new Recipe(
                    recipeCursor.getInt(recipeCursor.getColumnIndex(RecipesDBContract.RecipeEntry._ID)),
                    recipeCursor.getString(recipeCursor.getColumnIndex(RecipesDBContract.RecipeEntry.COLUMN_NAME)),
                    recipeCursor.getString(recipeCursor.getColumnIndex(RecipesDBContract.RecipeEntry.COLUMN_IMAGE)),
                    recipeCursor.getInt(recipeCursor.getColumnIndex(RecipesDBContract.RecipeEntry.COLUMN_SERVINGS)),
                    ingredients,
                    steps
            );
            return recipe;
        }

        throw new SQLiteException();
    }
}
