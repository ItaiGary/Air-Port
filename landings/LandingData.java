package com.example.landings;


import java.io.Serializable;

public class LandingData implements Serializable {

    public String airport;
    public String apptime;
    public String city;
    public int companyid;
    public String number;
    public String at;
    public boolean delayed;


    public LandingData()
    {

    }

    @Override
    public String toString() {
        return "number : " + number + " approximate time : " + apptime +
                " city : " + city + " company id : " + companyid +
                " airport : " + airport+ " exact arrival time: "
                + at + " delayed?: " + delayed;
    }


}