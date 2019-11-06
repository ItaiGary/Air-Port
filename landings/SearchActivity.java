package com.example.landings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private String searchBy = "city";
    private ArrayList<String> items;
    public static final String allOptionsString = "All (No Search Filters)";
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    final String TAG = "SearchActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Button btnCity = findViewById(R.id.btnCity);
        Button btnNumber = findViewById(R.id.btnNumber);
        Button btnAirport = findViewById(R.id.btnAirport);

        btnCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                items = new ArrayList<>();
                searchBy = "city";
                addItemsFromFirebase();
            }
        });
        btnAirport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                items = new ArrayList<>();
                searchBy = "airport";
                addItemsFromFirebase();
            }
        });
        btnNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBy = "number";
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setTitle("Enter The Flight Number");

                final EditText input = new EditText(SearchActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        i.putExtra("search_type", searchBy);
                        i.putExtra("search_filter", input.getText().toString());
                        startActivity(i);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }
    private void addItemsFromFirebase()
    {
        mDatabase.getReference().child("landings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> itemsStrings = new HashSet<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    LandingData ld = child.getValue(LandingData.class);
                    if(searchBy.equals("city"))
                        itemsStrings.add(ld.city);
                    if(searchBy.equals("airport"))
                        itemsStrings.add(ld.airport);
                }
                items = new ArrayList<>(itemsStrings);
                items.add(allOptionsString);

                ListView lv = findViewById(R.id.lvSearch);
                SearchListAdapter adapter = new SearchListAdapter(getBaseContext(), R.layout.search_list_item_layout, items, searchBy);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}