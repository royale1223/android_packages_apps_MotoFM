package com.motorola.fmradio.appwidgets;

import com.motorola.fmradio.FMRadioMain;

import android.util.Log;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.motorola.fmradio.FMRadioPlayerService;
import com.motorola.fmradio.R;

public class FourByTwoLightWidget extends AppWidgetProvider {

    public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate4x2white";
    private static final String TAG = "FourByTwoWhiteWidget";
    private static FourByTwoLightWidget sInstance;

    public static synchronized FourByTwoLightWidget getInstance() {
        if (sInstance == null) {
            sInstance = new FourByTwoLightWidget();
        }
        return sInstance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(TAG, "onUpdate");
        defaultAppWidget(context, appWidgetIds);
        // Send broadcast intent to any running FMRadioPlayerService so it can
        // wrap around with an immediate update.
        Intent updateIntent = new Intent(FMRadioPlayerService.SERVICECMD);
        updateIntent.putExtra(FMRadioPlayerService.CMDNAME, CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);

    }
    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    private void defaultAppWidget(Context context, int[] appWidgetIds) {
        Log.v(TAG, "defaultAppWidget");
        final RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.fourbytwo_app_widget);

        linkButtons(context, views);
        pushUpdate(context, appWidgetIds, views);
    }

    private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
        Log.v(TAG, "pushUpdate");
        // Update specific list of appWidgetIds if given, otherwise default to
        // all
        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            awm.updateAppWidget(appWidgetIds, views);
        } else {
            awm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }

    /**
     * Check against {@link AppWidgetManager} if there are any instances of this
     * widget.
     */
    private boolean hasInstances(Context context) {
        Log.v(TAG, "hasInstances");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this
                .getClass()));
        return (appWidgetIds.length > 0);
    }

    /**
     * Handle a change notification coming over from {@link FMRadioPlayerService}
     */
    public void notifyChange(FMRadioPlayerService service, String what) {
        Log.v(TAG, "notifyChange");
        if (hasInstances(service)) {
            if (FMRadioPlayerService.RDS_CHANGED.equals(what)
                    || FMRadioPlayerService.PLAYSTATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(FMRadioPlayerService service, int[] appWidgetIds) {
        Log.v(TAG, "performUpdate");
        final RemoteViews views = new RemoteViews(service.getPackageName(),
                R.layout.fourbytwo_app_widget);
        CharSequence stationName = service.getCurrentStationName();
        CharSequence frequency = String.valueOf(service.getmCurFreq());
        CharSequence rdsText = service.getRdsText();
        Log.v(TAG, "performUpdate "+stationName + frequency + rdsText);
        views.setTextViewText(R.id.four_by_two_white_station_name, stationName);
        views.setTextViewText(R.id.four_by_two_white_frequency, frequency);
        views.setTextViewText(R.id.four_by_two_white_rds, rdsText);

        // Set correct drawable for pause state
        final boolean playing = service != null;
        if (playing) {
            views.setImageViewResource(R.id.four_by_two_white_control_mute,
                    R.drawable.fm_holo_light_stop);
        } else {
            views.setImageViewResource(R.id.four_by_two_white_control_mute,
                    R.drawable.fm_holo_light_play);
        }

        // Link actions buttons to intents
        linkButtons(service, views);

        pushUpdate(service, appWidgetIds, views);

    }

    /**
     * Link up various button actions using {@link PendingIntents}.
     */
    private void linkButtons(Context context, RemoteViews views) {
        Log.v(TAG, "linkButtons");
        // Connect up various buttons and touch events
        Intent intent;
        PendingIntent pendingIntent;

        intent = new Intent()
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(new ComponentName(context, FMRadioMain.class))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        pendingIntent = PendingIntent.getActivity(context, 1, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_white_info, pendingIntent);

        intent = new Intent()
                .setAction(FMRadioPlayerService.ACTION_FM_COMMAND)
                .setComponent((new ComponentName(context, FMRadioPlayerService.class)))
                .putExtra(FMRadioPlayerService.EXTRA_COMMAND, FMRadioPlayerService.COMMAND_TOGGLE_MUTE);
        pendingIntent = PendingIntent.getService(context.getApplicationContext(), 2, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_white_control_mute, pendingIntent);

        intent.putExtra(FMRadioPlayerService.EXTRA_COMMAND, FMRadioPlayerService.COMMAND_NEXT);
        pendingIntent = PendingIntent.getService(context, 3, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_white_control_next, pendingIntent);

        intent.putExtra(FMRadioPlayerService.EXTRA_COMMAND, FMRadioPlayerService.COMMAND_PREV);
        pendingIntent = PendingIntent.getService(context, 4, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_white_control_prev, pendingIntent);
    }
}
