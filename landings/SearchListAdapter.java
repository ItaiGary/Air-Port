package com.example.landings;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SearchListAdapter extends ArrayAdapter {
    Context _context;
    int _layout;
    List<String> _objects;
    String _searchBy;

    public SearchListAdapter(Context context, int layout, List objects, String searchBy) {
        super(context, layout, objects);

        _context = context;
        _layout = layout;
        _objects = objects;
        _searchBy = searchBy;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.d("=============", "Get view for " + position);

        View result = convertView;

        if (result == null) {
            Log.d("=======", "Drawing " + position);
            result = LayoutInflater.from(_context).inflate(R.layout.search_list_item_layout,
                    parent, false);
        }
        TextView tvSearchItem = result.findViewById(R.id.txtSearchItem);
        tvSearchItem.setText(_objects.get(position));

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(_context, MainActivity.class);
                i.putExtra("search_type", _searchBy);
                i.putExtra("search_filter", _objects.get(position));
                _context.startActivity(i);
            }
        });

        return result;
    }
}
