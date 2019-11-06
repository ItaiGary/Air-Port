package com.example.landings;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.ListView;
import android.widget.Toast;

public class ConnectivityReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context,final Intent intent) {

        if(!airplaneModeOn(context))
        {
            ListView lv = (ListView)((Activity)context).findViewById(R.id.flightLV);
            lv.setAlpha(1f);
            lv.setEnabled(true);
            lv.setBackgroundColor(Color.WHITE);
        }
        else
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if(!prefs.getBoolean("offlineMode", false)) {
                ListView lv = (ListView) ((Activity) context).findViewById(R.id.flightLV);
                lv.setAlpha(0.75f);
                lv.setEnabled(false);
                lv.setBackgroundColor(Color.GRAY);
            }
            else Toast.makeText(context,"No Real-Time Connection", Toast.LENGTH_LONG).show();
        }
    }

    private static boolean airplaneModeOn(Context context) {

        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }
}