package com.example.landings;

import java.util.Calendar;
import java.util.Date;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;


import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FlightsDataAdapter adapter;
    private ListView lv;
    public List<LandingData> listOfLandingData = new ArrayList<>();
    private String filter;
    private String searchBy;
    final String Tag = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(),
                android.R.drawable.ic_menu_myplaces));

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(i);
            }
            });

        filter = getIntent().getStringExtra("search_filter");
        searchBy = getIntent().getStringExtra("search_type");


        lv = findViewById(R.id.flightLV);
        adapter = new FlightsDataAdapter(
                this, R.layout.landing_list_item, listOfLandingData );
        lv.setAdapter(adapter);
        bringData();
    }
    private void bringData()
    {
        if(Settings.System.getInt(this.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0)
            bringDataFromSQLite();
        else bringDataFromFirebase();
    }
    private void bringDataFromSQLite() {
        listOfLandingData.clear();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        listOfLandingData = dbHelper.getData();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (!prefs.getBoolean("offlineMode", false)) {
            ListView lv = findViewById(R.id.flightLV);
            lv.setAlpha(0.75f);
            lv.setEnabled(false);
            lv.setBackgroundColor(Color.GRAY);
        }
    }

    private void bringDataFromFirebase()
    {
        mDatabase.getReference().child("landings").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        listOfLandingData.clear();

                        for (DataSnapshot child : dataSnapshot.getChildren())
                        {
                            LandingData ld = child.getValue(LandingData.class);
                            if(!checkDisplay(ld))
                                listOfLandingData.add(ld);
                        }
                        adapter.notifyDataSetChanged();
                        startBackupService();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getBaseContext(), "Firebase error " + databaseError,
                                Toast.LENGTH_SHORT).show();
                        Log.e(Tag, "Firebase error " + databaseError.getMessage());
                    }
                }
        );
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.AIRPLANE_MODE"
        );
        getBaseContext().registerReceiver(new ConnectivityReceiver(),intentFilter);

    if(searchBy != null && !filter.equals(SearchActivity.allOptionsString))
    {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, "Only showing flights where the " +
                searchBy.toLowerCase() + " is: " + filter, Snackbar.LENGTH_INDEFINITE)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filter = SearchActivity.allOptionsString;
                        bringData();
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                .show();
    }
    }



    private void startBackupService()
    {
        LandingDataList list = new LandingDataList();
        list.listOfLandingData = listOfLandingData;
        Intent i = new Intent(getBaseContext(), SqlBackupService.class);
        i.putExtra("DATA", list);
        startService(i);
    }

    private boolean checkDisplay(LandingData ld)
    {
        boolean displayingOkay = false;

        if(searchBy == null)
            displayingOkay = true;
        else {
            if(filter.equals(SearchActivity.allOptionsString))
                displayingOkay = true;
            if(searchBy.equals("city") && ld.city.equals(filter))
                displayingOkay = true;
            if(searchBy.equals("airport") && ld.airport.equals(filter))
                displayingOkay = true;
            if(searchBy.equals("number") && ld.number.equals(filter))
                displayingOkay = true;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean landedOnly = prefs.getBoolean("landedOnly", false);
        boolean ongoingOnly = prefs.getBoolean("ongoingOnly", false);
        String arrivedBy = prefs.getString("arrivedBy", "0");
        int arriveByNum = 0;
        try { arriveByNum = Integer.parseInt(arrivedBy); }
        catch (Exception e)
        {
            Log.e(Tag,"===Error" + e + "===");
        }

        if(ld.at.equals("") && landedOnly) displayingOkay =  false;
        if(!ld.at.equals("") && ongoingOnly) displayingOkay = false;
        if(!timeWindowCheck(ld.at, arriveByNum)) displayingOkay = false;

        return displayingOkay;
    }
    private boolean timeWindowCheck(String arrivalTime, int timeWindow)
    {
        if(timeWindow == 0) return true;
        if(arrivalTime.equals("")) return false;
        Date currentTime = Calendar.getInstance().getTime();
        int minutesCurrent = currentTime.getMinutes();
        int hoursCurrent = (currentTime.getHours() + 3) % 24;

        int hoursArrived = Integer.parseInt(arrivalTime.substring(0, arrivalTime.indexOf(":")));
        int minutesArrived = Integer.parseInt(arrivalTime.substring(arrivalTime.indexOf(":") + 1, arrivalTime.length()));

        int timeDifference = hoursCurrent - hoursArrived;
        if(timeDifference < 0)
        {
            timeDifference = 24 + timeDifference;
        }
        if(timeDifference < timeWindow)
            return true;
        if(timeDifference == timeWindow)
        {
            if(minutesCurrent < minutesArrived) return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, AppPreferencesActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        lv = findViewById(R.id.flightLV);
        adapter = new FlightsDataAdapter(
                this, R.layout.landing_list_item, listOfLandingData );
        lv.setAdapter(adapter);
        bringDataFromFirebase();
    }
}