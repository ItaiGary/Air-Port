package com.example.landings;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "flights_db.db";
    public static final String TABLE_NAME = "SUPER_FLIGHTS";
    public static final String COL_1_ID = "ID";
    public static final String COL_2_AIRPORT = "AIRPORT";
    public static final String COL_3_CITY = "CITY";
    public static final String COL_4_DELAYED = "DELAYED";
    public static final String COL_5_APPTIME = "apptime";
    public static final String COL_6_companyid = "COMPANYID";
    public static final String COL_7_NUMBER = "NUMBER";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // table with id, name, email, paypal
        sqLiteDatabase.execSQL("create table " + TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "AIRPORT TEXT, AT TEXT, CITY TEXT, DELAYED INT," +
                " EAT TEXT, LOGO INT, NUMBER TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // when updragin your software
        // and you need to run some sql queries
        // sql query to add paypal column
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(List<LandingData> data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        for (LandingData flight : data) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_2_AIRPORT, flight.airport);
            contentValues.put(COL_3_CITY, flight.city);
            contentValues.put(COL_4_DELAYED, (flight.delayed ? 1 : 0));
            contentValues.put(COL_5_APPTIME, flight.apptime);
            contentValues.put(COL_6_companyid, flight.companyid);
            contentValues.put(COL_7_NUMBER, flight.number);
            long result = db.insert(TABLE_NAME, null, contentValues);
            if (result == -1)
                return false;
        }
        return true;
    }

    public List<LandingData> getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<LandingData> data = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
        while(cursor.moveToNext()) {
            LandingData flight = new LandingData();
            flight.airport = cursor.getString(1);
            flight.at = cursor.getString(2);
            if(flight.at==null) flight.at = "";
            flight.city = cursor.getString(3);
            flight.delayed = (cursor.getInt(4)==1);
            flight.apptime = cursor.getString(5);
            flight.companyid = cursor.getInt(6);
            flight.number = cursor.getString(7);
            data.add(flight);
        }
        return data;
    }

}