package com.example.covid_19.ui.charts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Area;
import com.anychart.core.cartesian.series.Bar;
import com.anychart.core.cartesian.series.Line;
import com.anychart.core.ui.Crosshair;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.MarkerType;
import com.anychart.enums.ScaleStackMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.example.covid_19.Database;
import com.example.covid_19.R;
import com.example.covid_19.Setting;
import com.example.covid_19.ui.global.Global;
import com.example.covid_19.ui.home.Home;
import com.example.covid_19.ui.home.Malaysia;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Chart extends AppCompatActivity {
    private static ArrayList<Malaysia> data2;
    private Query queryC;
    private AnyChartView anyChartView,anyChartView2,anyChartView3;
    private Spinner areaSpinner,barSpinner,lineSpinner;
    private Set area,line,bar;
    private ValueEventListener valueEventListener;
    private static String barSel,lineSel,areaSel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        anyChartView = findViewById(R.id.areaChart);
        anyChartView2 = findViewById(R.id.lineChart);
        anyChartView3 = findViewById(R.id.barChart);
        areaSpinner = findViewById(R.id.spinnerArea);
        barSpinner = findViewById(R.id.spinnerBar);
        lineSpinner = findViewById(R.id.spinnerLine);
        LinearLayout chartsBackground = findViewById(R.id.chartsBackground);

        setUpNavigation();

        if(Home.NightMode == 2){
            chartsBackground.setBackgroundResource(R.drawable.background_night);
            anyChartView.setBackgroundColor("#222831");
            anyChartView2.setBackgroundColor("#222831");
            anyChartView3.setBackgroundColor("#222831");
        }else if(Home.NightMode == 1){
            chartsBackground.setBackgroundResource(R.drawable.background);
            anyChartView.setBackgroundColor("white");
            anyChartView2.setBackgroundColor("white");
            anyChartView3.setBackgroundColor("white");
        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.MalaysiaStates, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(adapter);
        //areaSpinner.setSelection(0);
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                areaSel = areaSpinner.getItemAtPosition(i).toString();
                if (area != null){
                    APIlib.getInstance().setActiveAnyChartView(anyChartView);
                    area.data(getDataArea(data2,areaSel));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.caseTypes, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineSpinner.setAdapter(adapter2);
        //lineSpinner.setSelection(0);
        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lineSel = lineSpinner.getItemAtPosition(i).toString();
                if (line != null) {
                    APIlib.getInstance().setActiveAnyChartView(anyChartView2);
                    line.data(getDataLine(data2, lineSel));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
                R.array.caseTypes, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        barSpinner.setAdapter(adapter3);

        barSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                barSel = barSpinner.getItemAtPosition(i).toString();
                if (bar != null && !data2.isEmpty()) {
                    APIlib.getInstance().setActiveAnyChartView(anyChartView3);
                    bar.data(getDataBar(data2, barSel));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(queryC != null){
            queryC.removeEventListener(valueEventListener);
        }
    }

    public void setting(View view){
        Intent settingsPage = new Intent(this, Setting.class);
        startActivity(settingsPage);
    }

    private void setUpNavigation(){
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.charts);
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.home){
                this.startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }else if(id == R.id.global){
                this.startActivity(new Intent(getApplicationContext(), Global.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }else if(id == R.id.charts){
                this.startActivity(new Intent(getApplicationContext(), Chart.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }
            return false; });
    }

    private void loadData(){
        queryC = Database.getDatabase().getReference().limitToLast(30);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                data2 = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    data2.add(ds.getValue(Malaysia.class));
                }
                APIlib.getInstance().setActiveAnyChartView(anyChartView3);
                if(bar != null){
                    bar.data(getDataBar(data2, barSel));
                }else{
                    drawBar(data2, barSel);
                }
                APIlib.getInstance().setActiveAnyChartView(anyChartView);
                if (area != null){
                    area.data(getDataArea(data2,areaSel));
                }else{
                    areaChart(data2, areaSel);
                }
                APIlib.getInstance().setActiveAnyChartView(anyChartView2);
                if (line != null){
                    line.data(getDataLine(data2, lineSel));
                }else{
                    drawLine(data2,lineSel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("LogFragment", "loadLog:onCancelled", error.toException());
            }
        };
        queryC.addValueEventListener(valueEventListener);
    }

    private List<DataEntry> getDataArea(ArrayList<Malaysia> result, String selection) {
        List<DataEntry> historicalData = new ArrayList<>();
        switch (selection){
            case "Malaysia":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.4783", "Malaysia",result.get(i).Malaysia.date, result.get(i).Malaysia.cases_active, result.get(i).Malaysia.cases_import,
                            result.get(i).Malaysia.cases_new, result.get(i).Malaysia.cases_recovered,
                            result.get(i).Malaysia.deaths_bid, result.get(i).Malaysia.deaths_new));
                }
                break;
            case "Johor":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.JH", "Johor",result.get(i).Johor.date, result.get(i).Johor.cases_active, result.get(i).Johor.cases_import,
                            result.get(i).Johor.cases_new, result.get(i).Johor.cases_recovered,
                            result.get(i).Johor.deaths_bid, result.get(i).Johor.deaths_new));
                }
                break;
            case "Kedah":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.KH", "Kedah",result.get(i).Kedah.date, result.get(i).Kedah.cases_active, result.get(i).Kedah.cases_import,
                            result.get(i).Kedah.cases_new, result.get(i).Kedah.cases_recovered,
                            result.get(i).Kedah.deaths_bid, result.get(i).Kedah.deaths_new));
                }
                break;
            case "Kelantan":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.KN", "Kelantan",result.get(i).Kelantan.date, result.get(i).Kelantan.cases_active, result.get(i).Kelantan.cases_import,
                            result.get(i).Kelantan.cases_new, result.get(i).Kelantan.cases_recovered,
                            result.get(i).Kelantan.deaths_bid, result.get(i).Kelantan.deaths_new));
                }
                break;
            case "Kuala Lumpur":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.KL", "Kuala Lumpur",result.get(i).KualaLumpur.date, result.get(i).KualaLumpur.cases_active, result.get(i).KualaLumpur.cases_import,
                            result.get(i).KualaLumpur.cases_new, result.get(i).KualaLumpur.cases_recovered,
                            result.get(i).KualaLumpur.deaths_bid, result.get(i).KualaLumpur.deaths_new));
                }
                break;
            case "Labuan":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.LA", "Labuan",result.get(i).Labuan.date, result.get(i).Labuan.cases_active, result.get(i).Labuan.cases_import,
                            result.get(i).Labuan.cases_new, result.get(i).Labuan.cases_recovered,
                            result.get(i).Labuan.deaths_bid, result.get(i).Labuan.deaths_new));
                }
                break;
            case "Melaka":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.ME", "Melaka",result.get(i).Melaka.date, result.get(i).Melaka.cases_active, result.get(i).Melaka.cases_import,
                            result.get(i).Melaka.cases_new, result.get(i).Melaka.cases_recovered,
                            result.get(i).Melaka.deaths_bid, result.get(i).Melaka.deaths_new));
                }
                break;
            case "Negeri Sembilan":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.NS", "Negeri Sembilan",result.get(i).NegeriSembilan.date, result.get(i).NegeriSembilan.cases_active, result.get(i).NegeriSembilan.cases_import,
                            result.get(i).NegeriSembilan.cases_new, result.get(i).NegeriSembilan.cases_recovered,
                            result.get(i).NegeriSembilan.deaths_bid, result.get(i).NegeriSembilan.deaths_new));
                }
                break;
            case "Pahang":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.PH", "Pahang",result.get(i).Pahang.date, result.get(i).Pahang.cases_active, result.get(i).Pahang.cases_import,
                            result.get(i).Pahang.cases_new, result.get(i).Pahang.cases_recovered,
                            result.get(i).Pahang.deaths_bid, result.get(i).Pahang.deaths_new));
                }
                break;
            case "Perak":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.PK", "Perak",result.get(i).Perak.date, result.get(i).Perak.cases_active, result.get(i).Perak.cases_import,
                            result.get(i).Perak.cases_new, result.get(i).Perak.cases_recovered,
                            result.get(i).Perak.deaths_bid, result.get(i).Perak.deaths_new));
                }
                break;
            case "Perlis":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.PL", "Perlis",result.get(i).Perlis.date, result.get(i).Perlis.cases_active, result.get(i).Perlis.cases_import,
                            result.get(i).Perlis.cases_new, result.get(i).Perlis.cases_recovered,
                            result.get(i).Perlis.deaths_bid, result.get(i).Perlis.deaths_new));
                }
                break;
            case "Pulau Pinang":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.PG", "Pulau Pinang",result.get(i).PulauPinang.date, result.get(i).PulauPinang.cases_active, result.get(i).PulauPinang.cases_import,
                            result.get(i).PulauPinang.cases_new, result.get(i).PulauPinang.cases_recovered,
                            result.get(i).PulauPinang.deaths_bid, result.get(i).PulauPinang.deaths_new));
                }
                break;
            case "Putrajaya":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.PJ", "Putrajaya",result.get(i).Putrajaya.date, result.get(i).Putrajaya.cases_active, result.get(i).Putrajaya.cases_import,
                            result.get(i).Putrajaya.cases_new, result.get(i).Putrajaya.cases_recovered,
                            result.get(i).Putrajaya.deaths_bid, result.get(i).Putrajaya.deaths_new));
                }
                break;
            case "Sabah":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.SA", "Sabah",result.get(i).Sabah.date, result.get(i).Sabah.cases_active, result.get(i).Sabah.cases_import,
                            result.get(i).Sabah.cases_new, result.get(i).Sabah.cases_recovered,
                            result.get(i).Sabah.deaths_bid, result.get(i).Sabah.deaths_new));
                }
                break;
            case "Sarawak":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.SK", "Sarawak",result.get(i).Sarawak.date, result.get(i).Sarawak.cases_active, result.get(i).Sarawak.cases_import,
                            result.get(i).Sarawak.cases_new, result.get(i).Sarawak.cases_recovered,
                            result.get(i).Sarawak.deaths_bid, result.get(i).Sarawak.deaths_new));
                }
                break;
            case "Selangor":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.SL", "Selangor",result.get(i).Selangor.date, result.get(i).Selangor.cases_active, result.get(i).Selangor.cases_import,
                            result.get(i).Selangor.cases_new, result.get(i).Selangor.cases_recovered,
                            result.get(i).Selangor.deaths_bid, result.get(i).Selangor.deaths_new));
                }
                break;
            case "Terengganu":
                for (int i = 0; i<result.size(); i++){
                    historicalData.add(new CustomDataEntry("MY.TE", "Terengganu",result.get(i).Terengganu.date, result.get(i).Terengganu.cases_active, result.get(i).Terengganu.cases_import,
                            result.get(i).Terengganu.cases_new, result.get(i).Terengganu.cases_recovered,
                            result.get(i).Terengganu.deaths_bid, result.get(i).Terengganu.deaths_new));
                }
                break;
            default:
                System.out.println("Error occurred.");
        }
        return historicalData;
    }

    private List<DataEntry> getDataLine(ArrayList<Malaysia> result, String selection) {
        List<DataEntry> historicalLineData = new ArrayList<>();
        switch (selection){
            case "Infections":
                for (int i = 0; i<result.size(); i++){
                    historicalLineData.add(new CustomDataEntry(result.get(i).Johor.date, result.get(i).Johor.cases_new,result.get(i).Kedah.cases_new,result.get(i).Kelantan.cases_new,
                            result.get(i).KualaLumpur.cases_new,result.get(i).Labuan.cases_new, result.get(i).Melaka.cases_new,result.get(i).NegeriSembilan.cases_new,
                            result.get(i).Pahang.cases_new,result.get(i).Perak.cases_new, result.get(i).Perlis.cases_new,result.get(i).PulauPinang.cases_new,
                            result.get(i).Putrajaya.cases_new,result.get(i).Sabah.cases_new,result.get(i).Sarawak.cases_new,result.get(i).Selangor.cases_new,
                            result.get(i).Terengganu.cases_new));
                }
                break;
            case "Actives":
                for (int i = 0; i<result.size(); i++){
                    historicalLineData.add(new CustomDataEntry(result.get(i).Johor.date, result.get(i).Johor.cases_active,result.get(i).Kedah.cases_active,result.get(i).Kelantan.cases_active,
                            result.get(i).KualaLumpur.cases_active,result.get(i).Labuan.cases_active, result.get(i).Melaka.cases_active,result.get(i).NegeriSembilan.cases_active,
                            result.get(i).Pahang.cases_active,result.get(i).Perak.cases_active, result.get(i).Perlis.cases_active,result.get(i).PulauPinang.cases_active,
                            result.get(i).Putrajaya.cases_active,result.get(i).Sabah.cases_active,result.get(i).Sarawak.cases_active,result.get(i).Selangor.cases_active,
                            result.get(i).Terengganu.cases_active));
                }
                break;
            case "Recovered":
                for (int i = 0; i<result.size(); i++){
                    historicalLineData.add(new CustomDataEntry(result.get(i).Johor.date, result.get(i).Johor.cases_recovered,result.get(i).Kedah.cases_recovered,result.get(i).Kelantan.cases_recovered,
                            result.get(i).KualaLumpur.cases_recovered,result.get(i).Labuan.cases_recovered, result.get(i).Melaka.cases_recovered,result.get(i).NegeriSembilan.cases_recovered,
                            result.get(i).Pahang.cases_recovered,result.get(i).Perak.cases_recovered, result.get(i).Perlis.cases_recovered,result.get(i).PulauPinang.cases_recovered,
                            result.get(i).Putrajaya.cases_recovered,result.get(i).Sabah.cases_recovered,result.get(i).Sarawak.cases_recovered,result.get(i).Selangor.cases_recovered,
                            result.get(i).Terengganu.cases_recovered));
                }
                break;
            case "Deaths":
                for (int i = 0; i<result.size(); i++){
                    historicalLineData.add(new CustomDataEntry(result.get(i).Johor.date, result.get(i).Johor.deaths_new,result.get(i).Kedah.deaths_new,result.get(i).Kelantan.deaths_new,
                            result.get(i).KualaLumpur.deaths_new,result.get(i).Labuan.deaths_new, result.get(i).Melaka.deaths_new,result.get(i).NegeriSembilan.deaths_new,
                            result.get(i).Pahang.deaths_new,result.get(i).Perak.deaths_new, result.get(i).Perlis.deaths_new,result.get(i).PulauPinang.deaths_new,
                            result.get(i).Putrajaya.deaths_new,result.get(i).Sabah.deaths_new,result.get(i).Sarawak.deaths_new,result.get(i).Selangor.deaths_new,
                            result.get(i).Terengganu.deaths_new));
                }
                break;
            default:
                System.out.println("Error occurred.");
        }
        return historicalLineData;
    }

    private List<DataEntry> getDataBar(ArrayList<Malaysia> result, String selection){
        List<DataEntry> historicalBarData = new ArrayList<>();
        int lastIndex = result.size()-1;
        switch (selection){
            case "Infections":
                historicalBarData.add(new CustomDataEntry("Johor",result.get(lastIndex).Johor.cases_new));
                historicalBarData.add(new CustomDataEntry("Kedah",result.get(lastIndex).Kedah.cases_new));
                historicalBarData.add(new CustomDataEntry("Kelantan",result.get(lastIndex).Kelantan.cases_new));
                historicalBarData.add(new CustomDataEntry("KualaLumpur",result.get(lastIndex).KualaLumpur.cases_new));
                historicalBarData.add(new CustomDataEntry("Melaka",result.get(lastIndex).Melaka.cases_new));
                historicalBarData.add(new CustomDataEntry("NegeriSembilan",result.get(lastIndex).NegeriSembilan.cases_new));
                historicalBarData.add(new CustomDataEntry("Pahang",result.get(lastIndex).Pahang.cases_new));
                historicalBarData.add(new CustomDataEntry("Perak",result.get(lastIndex).Perak.cases_new));
                historicalBarData.add(new CustomDataEntry("Perlis",result.get(lastIndex).Perlis.cases_new));
                historicalBarData.add(new CustomDataEntry("PulauPinang",result.get(lastIndex).PulauPinang.cases_new));
                historicalBarData.add(new CustomDataEntry("Putrajaya",result.get(lastIndex).Putrajaya.cases_new));
                historicalBarData.add(new CustomDataEntry("Sabah",result.get(lastIndex).Sabah.cases_new));
                historicalBarData.add(new CustomDataEntry("Sarawak",result.get(lastIndex).Sarawak.cases_new));
                historicalBarData.add(new CustomDataEntry("Selangor",result.get(lastIndex).Selangor.cases_new));
                historicalBarData.add(new CustomDataEntry("Terengganu",result.get(lastIndex).Terengganu.cases_new));
                break;
            case "Actives":
                historicalBarData.add(new CustomDataEntry("Johor",result.get(lastIndex).Johor.cases_active));
                historicalBarData.add(new CustomDataEntry("Kedah",result.get(lastIndex).Kedah.cases_active));
                historicalBarData.add(new CustomDataEntry("Kelantan",result.get(lastIndex).Kelantan.cases_active));
                historicalBarData.add(new CustomDataEntry("KualaLumpur",result.get(lastIndex).KualaLumpur.cases_active));
                historicalBarData.add(new CustomDataEntry("Melaka",result.get(lastIndex).Melaka.cases_active));
                historicalBarData.add(new CustomDataEntry("NegeriSembilan",result.get(lastIndex).NegeriSembilan.cases_active));
                historicalBarData.add(new CustomDataEntry("Pahang",result.get(lastIndex).Pahang.cases_active));
                historicalBarData.add(new CustomDataEntry("Perak",result.get(lastIndex).Perak.cases_active));
                historicalBarData.add(new CustomDataEntry("Perlis",result.get(lastIndex).Perlis.cases_active));
                historicalBarData.add(new CustomDataEntry("PulauPinang",result.get(lastIndex).PulauPinang.cases_active));
                historicalBarData.add(new CustomDataEntry("Putrajaya",result.get(lastIndex).Putrajaya.cases_active));
                historicalBarData.add(new CustomDataEntry("Sabah",result.get(lastIndex).Sabah.cases_active));
                historicalBarData.add(new CustomDataEntry("Sarawak",result.get(lastIndex).Sarawak.cases_active));
                historicalBarData.add(new CustomDataEntry("Selangor",result.get(lastIndex).Selangor.cases_active));
                historicalBarData.add(new CustomDataEntry("Terengganu",result.get(lastIndex).Terengganu.cases_active));
                break;
            case "Recovered":
                historicalBarData.add(new CustomDataEntry("Johor",result.get(lastIndex).Johor.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Kedah",result.get(lastIndex).Kedah.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Kelantan",result.get(lastIndex).Kelantan.cases_recovered));
                historicalBarData.add(new CustomDataEntry("KualaLumpur",result.get(lastIndex).KualaLumpur.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Melaka",result.get(lastIndex).Melaka.cases_recovered));
                historicalBarData.add(new CustomDataEntry("NegeriSembilan",result.get(lastIndex).NegeriSembilan.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Pahang",result.get(lastIndex).Pahang.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Perak",result.get(lastIndex).Perak.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Perlis",result.get(lastIndex).Perlis.cases_recovered));
                historicalBarData.add(new CustomDataEntry("PulauPinang",result.get(lastIndex).PulauPinang.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Putrajaya",result.get(lastIndex).Putrajaya.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Sabah",result.get(lastIndex).Sabah.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Sarawak",result.get(lastIndex).Sarawak.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Selangor",result.get(lastIndex).Selangor.cases_recovered));
                historicalBarData.add(new CustomDataEntry("Terengganu",result.get(lastIndex).Terengganu.cases_recovered));
                break;
            case "Deaths":
                historicalBarData.add(new CustomDataEntry("Johor",result.get(lastIndex).Johor.deaths_new));
                historicalBarData.add(new CustomDataEntry("Kedah",result.get(lastIndex).Kedah.deaths_new));
                historicalBarData.add(new CustomDataEntry("Kelantan",result.get(lastIndex).Kelantan.deaths_new));
                historicalBarData.add(new CustomDataEntry("KualaLumpur",result.get(lastIndex).KualaLumpur.deaths_new));
                historicalBarData.add(new CustomDataEntry("Melaka",result.get(lastIndex).Melaka.deaths_new));
                historicalBarData.add(new CustomDataEntry("NegeriSembilan",result.get(lastIndex).NegeriSembilan.deaths_new));
                historicalBarData.add(new CustomDataEntry("Pahang",result.get(lastIndex).Pahang.deaths_new));
                historicalBarData.add(new CustomDataEntry("Perak",result.get(lastIndex).Perak.deaths_new));
                historicalBarData.add(new CustomDataEntry("Perlis",result.get(lastIndex).Perlis.deaths_new));
                historicalBarData.add(new CustomDataEntry("PulauPinang",result.get(lastIndex).PulauPinang.deaths_new));
                historicalBarData.add(new CustomDataEntry("Putrajaya",result.get(lastIndex).Putrajaya.deaths_new));
                historicalBarData.add(new CustomDataEntry("Sabah",result.get(lastIndex).Sabah.deaths_new));
                historicalBarData.add(new CustomDataEntry("Sarawak",result.get(lastIndex).Sarawak.deaths_new));
                historicalBarData.add(new CustomDataEntry("Selangor",result.get(lastIndex).Selangor.deaths_new));
                historicalBarData.add(new CustomDataEntry("Terengganu",result.get(lastIndex).Terengganu.deaths_new));
                break;
            default:
                System.out.println("Error occurred.");
        }
        return historicalBarData;
    }

    public void areaChart(ArrayList<Malaysia> input, String selected){
        try {
            APIlib.getInstance().setActiveAnyChartView(anyChartView);
            anyChartView.setProgressBar(findViewById(R.id.progressBar2));

            Cartesian areaChart = AnyChart.area();

            areaChart.animation(false);

            Crosshair crosshair = areaChart.crosshair();
            crosshair.enabled(true);

            crosshair.yStroke((Stroke) null, null, null, (String) null, (String) null)
                    .xStroke("#fff", 1d, null, (String) null, (String) null)
                    .zIndex(39d);
            crosshair.yLabel(0).enabled(true);

            areaChart.yScale().stackMode(ScaleStackMode.VALUE);

            area = Set.instantiate();
            area.data(getDataArea(input,selected));

            areaChart.background().fill("#000000",0);

            Mapping confirmed = area.mapAs("{ x: 'date', value: 'value' }");
            Mapping recover = area.mapAs("{ x: 'date', value: 'recovered' }");
            Mapping dead = area.mapAs("{ x: 'date', value: 'deaths' }");
            Mapping active = area.mapAs("{ x: 'date', value: 'active' }");

            Area series1 = areaChart.area(confirmed);
            series1.name("Confirmed");
            series1.stroke("1 #fff");
            series1.color("#369ED6");
            series1.hovered().stroke("3 #fff");
            series1.hovered().markers().enabled(true);
            series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d)
                    .stroke("1.5 #fff");
            series1.markers().zIndex(100d);

            Area series2 = areaChart.area(recover);
            series2.name("Recovered");
            series2.stroke("1 #fff");
            series2.color("#36D656");
            series2.hovered().stroke("3 #fff");
            series2.hovered().markers().enabled(true);
            series2.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d)
                    .stroke("1.5 #fff");
            series2.markers().zIndex(100d);

            Area series3 = areaChart.area(dead);
            series3.name("Deaths");
            series3.stroke("1 #fff");
            series3.color("#FF0000");
            series3.hovered().stroke("3 #fff");
            series3.hovered().markers().enabled(true);
            series3.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d)
                    .stroke("1.5 #fff");
            series3.markers().zIndex(100d);

            Area series4 = areaChart.area(active);
            series4.name("Actives");
            series4.stroke("1 #fff");
            series4.color("#FFA91C");
            series4.hovered().stroke("3 #fff");
            series4.hovered().markers().enabled(true);
            series4.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d)
                    .stroke("1.5 #fff");
            series4.markers().zIndex(100d);

            areaChart.legend().enabled(true);
            areaChart.legend().fontSize(13d);
            areaChart.legend().padding(0d, 0d, 20d, 0d);

            areaChart.xAxis(0).title(false);
            areaChart.yAxis(0).title(false);

            areaChart.interactivity().hoverMode(HoverMode.BY_X);
            areaChart.tooltip()
                    //.valuePrefix("$")
                    //.valuePostfix(" Cases")
                    .displayMode(TooltipDisplayMode.UNION);


            anyChartView.setChart(areaChart);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void drawLine(ArrayList<Malaysia> input, String selected){
        try {
            APIlib.getInstance().setActiveAnyChartView(anyChartView2);
            anyChartView2.setProgressBar(findViewById(R.id.progressBar3));

            Cartesian cartesian = AnyChart.line();

            cartesian.animation(false);

            //cartesian.padding(10d, 20d, 5d, 20d);

            cartesian.crosshair().enabled(true);
            cartesian.crosshair()
                    .yLabel(true)
                    .yStroke((Stroke) null, null, null, (String) null, (String) null);

            cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

            cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

            line = Set.instantiate();
            line.data(getDataLine(input,selected));

            cartesian.background().fill("#000000",0);

            Mapping Johor = line.mapAs("{ x: 'date', value: 'Johor' }");
            Mapping Kedah = line.mapAs("{ x: 'date', value: 'Kedah' }");
            Mapping Kelantan = line.mapAs("{ x: 'date', value: 'Kelantan' }");
            Mapping KualaLumpur = line.mapAs("{ x: 'date', value: 'KualaLumpur' }");
            Mapping Labuan = line.mapAs("{ x: 'date', value: 'Labuan' }");
            Mapping Melaka = line.mapAs("{ x: 'date', value: 'Melaka' }");
            Mapping NegeriSembilan = line.mapAs("{ x: 'date', value: 'NegeriSembilan' }");
            Mapping Pahang = line.mapAs("{ x: 'date', value: 'Pahang' }");
            Mapping Perak = line.mapAs("{ x: 'date', value: 'Perak' }");
            Mapping Perlis = line.mapAs("{ x: 'date', value: 'Perlis' }");
            Mapping PulauPinang = line.mapAs("{ x: 'date', value: 'PulauPinang' }");
            Mapping Putrajaya = line.mapAs("{ x: 'date', value: 'Putrajaya' }");
            Mapping Sabah = line.mapAs("{ x: 'date', value: 'Sabah' }");
            Mapping Sarawak = line.mapAs("{ x: 'date', value: 'Sarawak' }");
            Mapping Selangor = line.mapAs("{ x: 'date', value: 'Selangor' }");
            Mapping Terengganu = line.mapAs("{ x: 'date', value: 'Terengganu' }");

            Line series1 = cartesian.line(Johor);
            series1.name("Johor");
            series1.hovered().markers().enabled(true);
            series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series1.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series2 = cartesian.line(Kedah);
            series2.name("Kedah");
            series2.hovered().markers().enabled(true);
            series2.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series2.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series3 = cartesian.line(Kelantan);
            series3.name("Kelantan");
            series3.hovered().markers().enabled(true);
            series3.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series3.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series4 = cartesian.line(KualaLumpur);
            series4.name("KualaLumpur");
            series4.hovered().markers().enabled(true);
            series4.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series4.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series5 = cartesian.line(Labuan);
            series5.name("Labuan");
            series5.hovered().markers().enabled(true);
            series5.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series5.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series6 = cartesian.line(Melaka);
            series6.name("Melaka");
            series6.hovered().markers().enabled(true);
            series6.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series6.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series7 = cartesian.line(NegeriSembilan);
            series7.name("NegeriSembilan");
            series7.hovered().markers().enabled(true);
            series7.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series7.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series8 = cartesian.line(Pahang);
            series8.name("Pahang");
            series8.hovered().markers().enabled(true);
            series8.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series8.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series9 = cartesian.line(Perak);
            series9.name("Perak");
            series9.hovered().markers().enabled(true);
            series9.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series9.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series10 = cartesian.line(Perlis);
            series10.name("Perlis");
            series10.hovered().markers().enabled(true);
            series10.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series10.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series11 = cartesian.line(PulauPinang);
            series11.name("PulauPinang");
            series11.hovered().markers().enabled(true);
            series11.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series11.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series12 = cartesian.line(Putrajaya);
            series12.name("Putrajaya");
            series12.hovered().markers().enabled(true);
            series12.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series12.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series13 = cartesian.line(Sabah);
            series13.name("Sabah");
            series13.hovered().markers().enabled(true);
            series13.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series13.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series14 = cartesian.line(Sarawak);
            series14.name("Sarawak");
            series14.hovered().markers().enabled(true);
            series14.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series14.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series15 = cartesian.line(Selangor);
            series15.name("Selangor");
            series15.hovered().markers().enabled(true);
            series15.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series15.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series16 = cartesian.line(Terengganu);
            series16.name("Terengganu");
            series16.hovered().markers().enabled(true);
            series16.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series16.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            cartesian.legend().enabled(true);
            cartesian.legend().fontSize(13d);
            cartesian.legend().padding(0d, 0d, 10d, 0d);

            anyChartView2.setChart(cartesian);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void drawBar(ArrayList<Malaysia> input, String selected){
        try {
            APIlib.getInstance().setActiveAnyChartView(anyChartView3);
            anyChartView3.setProgressBar(findViewById(R.id.progressBar1));

            Cartesian vertical = AnyChart.vertical();

            bar = Set.instantiate();
            bar.data(getDataBar(input,selected));

            vertical.background().fill("#000000",0);

            Mapping barData = bar.mapAs("{ x: 'name', value: 'value' }");

            Bar bar = vertical.bar(barData);
            bar.labels().format("{%Value}");

            vertical.yScale().minimum(0d);

            vertical.labels(true);
            vertical.tooltip(false);
            vertical.interactivity().hoverMode(HoverMode.BY_X);

            vertical.xAxis(true);
            vertical.yAxis(true);
            vertical.yAxis(0).labels().format("{%Value}");

            anyChartView3.setChart(vertical);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String name, Number value) {
            super(name, value);
            setValue("name", name);
            setValue("value", value);
        }

        CustomDataEntry(String id, String name,String date,Number active,Number imported,Number value,
                        Number recovered,Number deathBID,Number deaths) {
            super(date, value);
            setValue("id", id);
            setValue("name", name);
            setValue("date", date);
            setValue("active", active);
            setValue("imported", imported);
            setValue("value", value);
            setValue("recovered", recovered);
            setValue("deathBID", deathBID);
            setValue("deaths", deaths);
        }

        CustomDataEntry(String date, Number Johor,Number Kedah,Number Kelantan,Number KualaLumpur,Number Labuan,
                        Number Melaka,Number NegeriSembilan,Number Pahang,Number Perak,Number Perlis,Number PulauPinang,
                        Number Putrajaya,Number Sabah,Number Sarawak,Number Selangor,Number Terengganu) {
            super(date, Johor);
            setValue("Johor", Johor);
            setValue("Kedah", Kedah);
            setValue("Kelantan", Kelantan);
            setValue("KualaLumpur", KualaLumpur);
            setValue("Labuan", Labuan);
            setValue("Melaka", Melaka);
            setValue("NegeriSembilan", NegeriSembilan);
            setValue("Pahang", Pahang);
            setValue("Perak", Perak);
            setValue("Perlis", Perlis);
            setValue("PulauPinang", PulauPinang);
            setValue("Putrajaya", Putrajaya);
            setValue("Sabah", Sabah);
            setValue("Sarawak", Sarawak);
            setValue("Selangor", Selangor);
            setValue("Terengganu", Terengganu);
        }
    }
}