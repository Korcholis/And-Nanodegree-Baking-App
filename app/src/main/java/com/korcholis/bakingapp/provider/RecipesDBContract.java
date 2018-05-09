package com.korcholis.bakingapp.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class RecipesDBContract {
    public static final String AUTHORITY = "com.korcholis.bakingapp";
    public static final String PATH_RECIPES = "recipes";
    public static final String PATH_INGREDIENTS = "ingredients";
    public static final String PATH_STEPS = "steps";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private RecipesDBContract() {}

    public static class RecipeEntry implements BaseColumns {
        public static final String TABLE_NAME = "recipes";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();

        public static final String COLUMN_REMOTE_ID = "remote_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_SERVINGS = "servings";
    }

    public static class IngredientEntry implements BaseColumns {
        public static final String TABLE_NAME = "ingredients";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_INGREDIENTS).build();

        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_MEASURE = "measure";
        public static final String COLUMN_INGREDIENT = "ingredient";
        public static final String COLUMN_QUANTITY = "quantity";
    }

    public static class RecipeStepEntry implements BaseColumns {
        public static final String TABLE_NAME = "recipe_steps";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STEPS).build();

        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_STEP = "step";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_SHORT_DESCRIPTION = "short_description";
        public static final String COLUMN_THUMBNAIL_URL = "thumbnail_url";
        public static final String COLUMN_VIDEO_URL = "video_url";
    }
}
