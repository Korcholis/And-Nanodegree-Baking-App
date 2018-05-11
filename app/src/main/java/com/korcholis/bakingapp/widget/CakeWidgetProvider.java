package com.korcholis.bakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.korcholis.bakingapp.BuildConfig;
import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.provider.IngredientsWidgetService;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CakeWidgetProviderConfigureActivity CakeWidgetProviderConfigureActivity}
 */
public class CakeWidgetProvider extends AppWidgetProvider {
    public static final String UPDATE_ACTION = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String RECIPE_ID = "recipe_id";

    private static final String TAG = "[CWP]";

    static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        int recipeId = CakeWidgetProviderConfigureActivity.loadRecipeIdPrefix(context, appWidgetId);

        Log.i(TAG, "updateAppWidget: " + recipeId);

        Intent intent = new Intent(context, IngredientsWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(RECIPE_ID, recipeId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cake_widget_provider);
        views.setEmptyView(R.id.ingredient_list, R.id.empty_tv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            views.setRemoteAdapter(R.id.ingredient_list, intent);
        } else {
            views.setRemoteAdapter(appWidgetId, R.id.ingredient_list, intent);
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
        if (intent.getAction().equals(UPDATE_ACTION)) {
            int appWidgetIds[] = mgr.getAppWidgetIds(new ComponentName(context, CakeWidgetProvider.class));
            mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.ingredient_lv);
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
    public void onEnabled(Context context) { }

    @Override
    public void onDisabled(Context context) { }
}

