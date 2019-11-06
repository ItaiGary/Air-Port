package com.example.landings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FlightsDataAdapter extends ArrayAdapter<LandingData> {

    Context _context;
    int _layout;
    List<LandingData> _objects;

    public FlightsDataAdapter(Context context, int layout, List<LandingData> objects) {
        super(context, layout, objects);
        _context = context;
        _layout = layout;
        _objects = objects;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("=============", "Get view for " + position);

        View result = convertView;

        if (result == null) {
            Log.d("=======", "Drawing " + position);
            result = LayoutInflater.from(_context).inflate(R.layout.landing_list_item,
                    parent, false);
        }

        TextView tvCity = result.findViewById(R.id.city_textView);
        TextView tvAirport = result.findViewById(R.id.airport_textView);
        TextView tvNumber = result.findViewById(R.id.number_textView);
        TextView tvAppTime = result.findViewById(R.id.apptime_textView);
        TextView txtDelayed = result.findViewById(R.id.txtDelayed);
        ImageView imgLogo = result.findViewById(R.id.imageView);

        tvAppTime.setText(_objects.get(position).apptime);
        tvAirport.setText(_objects.get(position).airport);
        tvCity.setText(_objects.get(position).city);
        tvNumber.setText(_objects.get(position).number);
        int logo = R.drawable.no_logo;
        int logoId = _objects.get(position).companyid;
        if (logoId == 1) logo = R.drawable.british;
        if (logoId == 2) logo = R.drawable.elal;
        if (logoId == 3) logo = R.drawable.turkish;
        if (logoId == 4) logo = R.drawable.swiss;
        imgLogo.setImageResource(logo);
        if (!_objects.get(position).at.equals("")) {
            TextView txtAt = result.findViewById(R.id.txtAt);
            TextView txtLanded = result.findViewById(R.id.txtLanded);
            txtAt.setVisibility(View.VISIBLE);
            txtLanded.setVisibility(View.VISIBLE);
            txtAt.setText(_objects.get(position).at);
        }
        if (_objects.get(position).delayed) txtDelayed.setVisibility(View.VISIBLE);

        return result;

    }
    public void preferenceUpdate()
    {
        for (LandingData data : _objects)
        {
            if(!preferenceCheck(data)) _objects.remove(data);
        }
    }
    public boolean preferenceCheck(LandingData landingData)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);

        boolean landedOnly = prefs.getBoolean("landedOnly", false);
        boolean ongoingOnly = prefs.getBoolean("ongoingOnly", false);
        String arrivedBy = prefs.getString("arrivedBy", "0");
        int arriveByNum = 0;
        try { arriveByNum = Integer.parseInt(arrivedBy); }
        catch (Exception e)
        {

        }

        if(landingData.at.equals("") && landedOnly) return  false;
        if(!landingData.at.equals("") && ongoingOnly) return false;
        if(!timeWindowCheck(landingData.at, arriveByNum))
            return false;

        return true;
    }
    public boolean timeWindowCheck(String arrivalTime, int timeWindow)
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
}