package com.west.earthquake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by usr1 on 8/1/14.
 */
public class EarthquakeAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_REFRESH_EARTHQUAKE_ALARM =
            "com.west.earthquake.ACTION_REFRESH_EARTHQUAKE_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent startIntent = new Intent(context, EarthquakeUpdateServices.class);
        context.startService(startIntent);

    }
}
