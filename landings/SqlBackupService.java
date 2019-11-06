package com.example.landings;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

public class SqlBackupService extends Service {
    private SQLiteDatabase database;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        LandingDataList list = (LandingDataList) intent.getSerializableExtra("DATA");
        helper.insertData(list.listOfLandingData);
        return Service.START_STICKY;
    }
}