package com.korcholis.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.RecipeDetailActivity;
import com.korcholis.bakingapp.utils.Constants;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CakeWidgetProviderConfigureActivity CakeWidgetProviderConfigureActivity}
 */
public class CakeWidgetProvider extends AppWidgetProvider {
    @SuppressWarnings("WeakerAccess")
    public static final String UPDATE_ACTION = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String RECIPE_ID = "recipe_id";

    static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        int recipeId = CakeWidgetProviderConfigureActivity.loadRecipeId(context, appWidgetId);
        String recipeName = CakeWidgetProviderConfigureActivity.loadRecipeName(context, appWidgetId);

        Intent intent = new Intent(context, IngredientsWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(RECIPE_ID, recipeId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cake_widget_provider);
        views.setEmptyView(R.id.ingredient_list, R.id.empty_tv);
        views.setTextViewText(R.id.ingredients_title, recipeName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            views.setRemoteAdapter(R.id.ingredient_list, intent);
        } else {
            views.setRemoteAdapter(appWidgetId, R.id.ingredient_list, intent);
        }

        if (recipeId != -1) {
            Intent clickIntent = new Intent(context, RecipeDetailActivity.class);
            clickIntent.putExtra(Constants.PARAM_RECIPE_ID, recipeId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);
            views.setOnClickPendingIntent(R.id.open_recipe_btn, pendingIntent);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent != null && intent.getAction() != null && intent.getAction().equals(UPDATE_ACTION)) {
            int appWidgetIds[] = mgr.getAppWidgetIds(new ComponentName(context, CakeWidgetProvider.class));
            mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.ingredient_list);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            CakeWidgetProviderConfigureActivity.deleteRecipeIdPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

