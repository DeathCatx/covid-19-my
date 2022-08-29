package com.example.covid_19.ui.home;

public class Data {
    public int cases_active,cases_adolescent,cases_adult,cases_child,
            activeToday,cases_elderly,totalConfirmed,cases_import,cases_new,
            totalDeath,cases_recovered,totalRecovered,deaths_bid,deaths_new;
    public String date;

    public Data(){

    }

    public Data(int cases_active, int cases_adolescent, int cases_adult,int cases_child,
                int activeToday, int cases_elderly, int totalConfirmed, int cases_import,
                int cases_new, int totalDeath,int cases_recovered, int totalRecovered,int deaths_bid, int deaths_new, String date){
        this.cases_active = cases_active;
        this.cases_adolescent = cases_adolescent;
        this.cases_adult = cases_adult;
        this.cases_child = cases_child;
        this.activeToday = activeToday;
        this.cases_elderly = cases_elderly;
        this.totalConfirmed = totalConfirmed;
        this.cases_import = cases_import;
        this.cases_new = cases_new;
        this.totalDeath = totalDeath;
        this.cases_recovered = cases_recovered;
        this.totalRecovered = totalRecovered;
        this.date = date;
        this.deaths_bid = deaths_bid;
        this.deaths_new = deaths_new;
    }
}
