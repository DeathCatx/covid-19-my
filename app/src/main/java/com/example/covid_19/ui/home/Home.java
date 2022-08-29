package com.example.covid_19.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Map;
import com.anychart.core.map.series.Choropleth;
import com.anychart.core.ui.ColorRange;
import com.anychart.enums.SelectionMode;
import com.anychart.enums.SidePosition;
import com.anychart.scales.LinearColor;
import com.example.covid_19.Database;
import com.example.covid_19.R;
import com.example.covid_19.Setting;
import com.example.covid_19.ui.charts.Chart;
import com.example.covid_19.ui.global.Global;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Home extends AppCompatActivity {
    public static final String TAG = "";
    public static int NightMode,PushNotification;
    public static SharedPreferences sharedPreferences;
    public static int restart = 0;
    private Query query;
    private AnyChartView mapView;
    private Choropleth series;
    private static ArrayList<Malaysia> data;
    private ValueEventListener valueEventListener;

    TextView state,date,infectionM,infectionTodayM,infectionChild,infectionAdolescent,infectionAdult,infectionElderly,
    recoveredM,recoveredTodayM,activeM,activeTodayM,deathM,deathTodayM,deathTodayBID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(restart == 1){
            restart = 0;
            restart();
        }

        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        NightMode = sharedPreferences.getInt("NightModeInt", 1);
        PushNotification = sharedPreferences.getInt("NotificationInt", 2);
        //autoUpdate = sharedPreferences.getInt("autoUpdate", 1);
        //1 is off, 2 is on
        if(PushNotification == 2){
            FirebaseMessaging.getInstance().subscribeToTopic("news");
        }else if(PushNotification == 1){
            FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
        }
        AppCompatDelegate.setDefaultNightMode(NightMode);
        Database.getDatabase().getReference();
        FCM();
        setContentView(R.layout.activity_home);

        state = findViewById(R.id.state);
        date = findViewById(R.id.date);
        infectionM = findViewById(R.id.infectionM);
        infectionTodayM = findViewById(R.id.infectionTodayM);
        infectionChild = findViewById(R.id.infectionChild);
        infectionAdolescent = findViewById(R.id.infectionAdolescent);
        infectionAdult = findViewById(R.id.infectionAdult);
        infectionElderly = findViewById(R.id.infectionElderly);
        recoveredM = findViewById(R.id.recoveredM);
        recoveredTodayM = findViewById(R.id.recoveredTodayM);
        activeM = findViewById(R.id.activeM);
        activeTodayM = findViewById(R.id.activeTodayM);
        deathM = findViewById(R.id.deathM);
        deathTodayM = findViewById(R.id.deathTodayM);
        deathTodayBID = findViewById(R.id.deathTodayBID);
        setUpNavigation();

        query = Database.getDatabase().getReference().limitToLast(1);
        series = null;
        final LinearLayout homeBackground = findViewById(R.id.homeBackground);
        mapView = findViewById(R.id.malaysia);
        if(NightMode == 2){
            homeBackground.setBackgroundResource(R.drawable.background_night);
            mapView.setBackgroundColor("#222831");
        }else if(NightMode == 1){
            homeBackground.setBackgroundResource(R.drawable.background);
            mapView.setBackgroundColor("#ADC8B8");
        }

        loadData();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(query != null){
            query.removeEventListener(valueEventListener);
        }
    }

    private void setUpNavigation(){
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.home);
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
            return false;
        });
    }

    public void setting(View view){
        //System.out.print("clicked");
        Intent settingsPage = new Intent(this, Setting.class);
        startActivity(settingsPage);
    }

    public static String formatNumber(String number){
        return NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(number));
    }

    private void FCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
                    String msg = getString(R.string.msg_token_fmt, token);
                    Log.d(TAG, msg);
                    //Toast.makeText(Home.this, msg, Toast.LENGTH_SHORT).show();
                });
    }

    public void restart(){
        Intent intent = new Intent(this, Home.class);
        this.startActivity(intent);
        this.finishAffinity();
    }

    private void loadData(){
        data = new ArrayList<>();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //data = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    //System.out.println(ds.getValue());
                    data.add(ds.getValue(Malaysia.class));
                }
                date.setText(getString(R.string.last_date, data.get(data.size()-1).Malaysia.date));
                infectionM.setText(formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.totalConfirmed)));
                infectionTodayM.setText(getString(R.string.today, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.cases_new))));
                infectionChild.setText(getString(R.string.child, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.cases_child))));
                infectionAdolescent.setText(getString(R.string.adolescent, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.cases_adolescent))));
                infectionAdult.setText(getString(R.string.adult, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.cases_adult))));
                infectionElderly.setText(getString(R.string.elderly, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.cases_elderly))));
                recoveredM.setText(formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.totalRecovered)));
                recoveredTodayM.setText(getString(R.string.today, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.cases_recovered))));
                activeM.setText(formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.cases_active)));
                activeTodayM.setText(calActiveDiff(data.get(data.size()-1).Malaysia.activeToday));
                deathM.setText(formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.totalDeath)));
                deathTodayM.setText(getString(R.string.today, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.deaths_new))));
                deathTodayBID.setText(getString(R.string.brought_in_death, formatNumber(String.valueOf(data.get(data.size()-1).Malaysia.deaths_bid))));
                //mapView.invalidate();
                APIlib.getInstance().setActiveAnyChartView(mapView);
                if(series != null){
                    series.data(getData(data));
                }else{
                    drawMap(data);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("LogFragment", "loadLog:onCancelled", error.toException());
            }
        };
        query.addValueEventListener(valueEventListener);
    }

    private String calActiveDiff(int active){
        if (active > 0) {
            return "(+" + Home.formatNumber(Integer.toString(active)) + " Today)";
        }
        return "(" + Home.formatNumber(Integer.toString(active)) + " Today)" ;
    }

    private List<DataEntry> getData(ArrayList<Malaysia> result) {
        List<DataEntry> data = new ArrayList<>();
        //data.add(new CustomDataEntry("MY.4783", "", 8.4));
        int lastIndex = result.size()-1;

        data.add(new CustomDataEntry("MY.PK","Perak", result.get(lastIndex).Perak.cases_new,result.get(lastIndex).Perak.totalConfirmed,
                result.get(lastIndex).Perak.cases_child, result.get(lastIndex).Perak.cases_adolescent,result.get(lastIndex).Perak.cases_adult,
                result.get(lastIndex).Perak.cases_elderly, result.get(lastIndex).Perak.cases_active, result.get(lastIndex).Perak.activeToday,
                result.get(lastIndex).Perak.cases_import, result.get(lastIndex).Perak.cases_recovered, result.get(lastIndex).Perak.totalRecovered,
                result.get(lastIndex).Perak.deaths_new,result.get(lastIndex).Perak.totalDeath,result.get(lastIndex).Perak.deaths_bid));
        data.add(new CustomDataEntry("MY.PG","Pulau Pinang",result.get(lastIndex).PulauPinang.cases_new,result.get(lastIndex).PulauPinang.totalConfirmed,
                result.get(lastIndex).PulauPinang.cases_child, result.get(lastIndex).PulauPinang.cases_adolescent,result.get(lastIndex).PulauPinang.cases_adult,
                result.get(lastIndex).PulauPinang.cases_elderly, result.get(lastIndex).PulauPinang.cases_active, result.get(lastIndex).PulauPinang.activeToday,
                result.get(lastIndex).PulauPinang.cases_import, result.get(lastIndex).PulauPinang.cases_recovered, result.get(lastIndex).PulauPinang.totalRecovered,
                result.get(lastIndex).PulauPinang.deaths_new,result.get(lastIndex).PulauPinang.totalDeath,result.get(lastIndex).PulauPinang.deaths_bid));
        data.add(new CustomDataEntry("MY.KH","Kedah",result.get(lastIndex).Kedah.cases_new,result.get(lastIndex).Kedah.totalConfirmed,
                result.get(lastIndex).Kedah.cases_child, result.get(lastIndex).Kedah.cases_adolescent,result.get(lastIndex).Kedah.cases_adult,
                result.get(lastIndex).Kedah.cases_elderly, result.get(lastIndex).Kedah.cases_active, result.get(lastIndex).Kedah.activeToday,
                result.get(lastIndex).Kedah.cases_import, result.get(lastIndex).Kedah.cases_recovered, result.get(lastIndex).Kedah.totalRecovered,
                result.get(lastIndex).Kedah.deaths_new,result.get(lastIndex).Kedah.totalDeath,result.get(lastIndex).Kedah.deaths_bid));
        data.add(new CustomDataEntry("MY.PL","Perlis",result.get(lastIndex).Perlis.cases_new,result.get(lastIndex).Perlis.totalConfirmed,
                result.get(lastIndex).Perlis.cases_child, result.get(lastIndex).Perlis.cases_adolescent,result.get(lastIndex).Perlis.cases_adult,
                result.get(lastIndex).Perlis.cases_elderly, result.get(lastIndex).Perlis.cases_active, result.get(lastIndex).Perlis.activeToday,
                result.get(lastIndex).Perlis.cases_import, result.get(lastIndex).Perlis.cases_recovered, result.get(lastIndex).Perlis.totalRecovered,
                result.get(lastIndex).Perlis.deaths_new,result.get(lastIndex).Perlis.totalDeath,result.get(lastIndex).Perlis.deaths_bid));
        data.add(new CustomDataEntry("MY.JH","Johor",result.get(lastIndex).Johor.cases_new,result.get(lastIndex).Johor.totalConfirmed,
                result.get(lastIndex).Johor.cases_child, result.get(lastIndex).Johor.cases_adolescent,result.get(lastIndex).Johor.cases_adult,
                result.get(lastIndex).Johor.cases_elderly, result.get(lastIndex).Johor.cases_active, result.get(lastIndex).Johor.activeToday,
                result.get(lastIndex).Johor.cases_import, result.get(lastIndex).Johor.cases_recovered, result.get(lastIndex).Johor.totalRecovered,
                result.get(lastIndex).Johor.deaths_new,result.get(lastIndex).Johor.totalDeath,result.get(lastIndex).Johor.deaths_bid));
        data.add(new CustomDataEntry("MY.KN","Kelantan",result.get(lastIndex).Kelantan.cases_new,result.get(lastIndex).Kelantan.totalConfirmed,
                result.get(lastIndex).Kelantan.cases_child, result.get(lastIndex).Kelantan.cases_adolescent,result.get(lastIndex).Kelantan.cases_adult,
                result.get(lastIndex).Kelantan.cases_elderly, result.get(lastIndex).Kelantan.cases_active, result.get(lastIndex).Kelantan.activeToday,
                result.get(lastIndex).Kelantan.cases_import, result.get(lastIndex).Kelantan.cases_recovered, result.get(lastIndex).Kelantan.totalRecovered,
                result.get(lastIndex).Kelantan.deaths_new,result.get(lastIndex).Kelantan.totalDeath,result.get(lastIndex).Kelantan.deaths_bid));
        data.add(new CustomDataEntry("MY.ME","Melaka",result.get(lastIndex).Melaka.cases_new,result.get(lastIndex).Melaka.totalConfirmed,
                result.get(lastIndex).Melaka.cases_child, result.get(lastIndex).Melaka.cases_adolescent,result.get(lastIndex).Melaka.cases_adult,
                result.get(lastIndex).Melaka.cases_elderly, result.get(lastIndex).Melaka.cases_active, result.get(lastIndex).Melaka.activeToday,
                result.get(lastIndex).Melaka.cases_import, result.get(lastIndex).Melaka.cases_recovered, result.get(lastIndex).Melaka.totalRecovered,
                result.get(lastIndex).Melaka.deaths_new,result.get(lastIndex).Melaka.totalDeath,result.get(lastIndex).Melaka.deaths_bid));
        data.add(new CustomDataEntry("MY.NS","Negeri Sembilan",result.get(lastIndex).NegeriSembilan.cases_new,result.get(lastIndex).NegeriSembilan.totalConfirmed,
                result.get(lastIndex).NegeriSembilan.cases_child, result.get(lastIndex).NegeriSembilan.cases_adolescent,result.get(lastIndex).NegeriSembilan.cases_adult,
                result.get(lastIndex).NegeriSembilan.cases_elderly, result.get(lastIndex).NegeriSembilan.cases_active, result.get(lastIndex).NegeriSembilan.activeToday,
                result.get(lastIndex).NegeriSembilan.cases_import, result.get(lastIndex).NegeriSembilan.cases_recovered, result.get(lastIndex).NegeriSembilan.totalRecovered,
                result.get(lastIndex).NegeriSembilan.deaths_new,result.get(lastIndex).NegeriSembilan.totalDeath,result.get(lastIndex).NegeriSembilan.deaths_bid));
        data.add(new CustomDataEntry("MY.PH","Pahang",result.get(lastIndex).Pahang.cases_new,result.get(lastIndex).Pahang.totalConfirmed,
                result.get(lastIndex).Pahang.cases_child, result.get(lastIndex).Pahang.cases_adolescent,result.get(lastIndex).Pahang.cases_adult,
                result.get(lastIndex).Pahang.cases_elderly, result.get(lastIndex).Pahang.cases_active, result.get(lastIndex).Pahang.activeToday,
                result.get(lastIndex).Pahang.cases_import, result.get(lastIndex).Pahang.cases_recovered, result.get(lastIndex).Pahang.totalRecovered,
                result.get(lastIndex).Pahang.deaths_new,result.get(lastIndex).Pahang.totalDeath,result.get(lastIndex).Pahang.deaths_bid));
        data.add(new CustomDataEntry("MY.SL","Selangor",result.get(lastIndex).Selangor.cases_new,result.get(lastIndex).Selangor.totalConfirmed,
                result.get(lastIndex).Selangor.cases_child, result.get(lastIndex).Selangor.cases_adolescent,result.get(lastIndex).Selangor.cases_adult,
                result.get(lastIndex).Selangor.cases_elderly, result.get(lastIndex).Selangor.cases_active, result.get(lastIndex).Selangor.activeToday,
                result.get(lastIndex).Selangor.cases_import, result.get(lastIndex).Selangor.cases_recovered, result.get(lastIndex).Selangor.totalRecovered,
                result.get(lastIndex).Selangor.deaths_new,result.get(lastIndex).Selangor.totalDeath,result.get(lastIndex).Selangor.deaths_bid));
        data.add(new CustomDataEntry("MY.TE","Terengganu",result.get(lastIndex).Terengganu.cases_new,result.get(lastIndex).Terengganu.totalConfirmed,
                result.get(lastIndex).Terengganu.cases_child, result.get(lastIndex).Terengganu.cases_adolescent,result.get(lastIndex).Terengganu.cases_adult,
                result.get(lastIndex).Terengganu.cases_elderly, result.get(lastIndex).Terengganu.cases_active, result.get(lastIndex).Terengganu.activeToday,
                result.get(lastIndex).Terengganu.cases_import, result.get(lastIndex).Terengganu.cases_recovered, result.get(lastIndex).Terengganu.totalRecovered,
                result.get(lastIndex).Terengganu.deaths_new,result.get(lastIndex).Terengganu.totalDeath,result.get(lastIndex).Terengganu.deaths_bid));
        data.add(new CustomDataEntry("MY.SA","Sabah",result.get(lastIndex).Sabah.cases_new,result.get(lastIndex).Sabah.totalConfirmed,
                result.get(lastIndex).Sabah.cases_child, result.get(lastIndex).Sabah.cases_adolescent,result.get(lastIndex).Sabah.cases_adult,
                result.get(lastIndex).Sabah.cases_elderly, result.get(lastIndex).Sabah.cases_active, result.get(lastIndex).Sabah.activeToday,
                result.get(lastIndex).Sabah.cases_import, result.get(lastIndex).Sabah.cases_recovered, result.get(lastIndex).Sabah.totalRecovered,
                result.get(lastIndex).Sabah.deaths_new,result.get(lastIndex).Sabah.totalDeath,result.get(lastIndex).Sabah.deaths_bid));
        data.add(new CustomDataEntry("MY.SK","Sarawak",result.get(lastIndex).Sarawak.cases_new,result.get(lastIndex).Sarawak.totalConfirmed,
                result.get(lastIndex).Sarawak.cases_child, result.get(lastIndex).Sarawak.cases_adolescent,result.get(lastIndex).Sarawak.cases_adult,
                result.get(lastIndex).Sarawak.cases_elderly, result.get(lastIndex).Sarawak.cases_active, result.get(lastIndex).Sarawak.activeToday,
                result.get(lastIndex).Sarawak.cases_import, result.get(lastIndex).Sarawak.cases_recovered, result.get(lastIndex).Sarawak.totalRecovered,
                result.get(lastIndex).Sarawak.deaths_new,result.get(lastIndex).Sarawak.totalDeath,result.get(lastIndex).Sarawak.deaths_bid));
        data.add(new CustomDataEntry("MY.KL","Kuala Lumpur",result.get(lastIndex).KualaLumpur.cases_new,result.get(lastIndex).KualaLumpur.totalConfirmed,
                result.get(lastIndex).KualaLumpur.cases_child, result.get(lastIndex).KualaLumpur.cases_adolescent,result.get(lastIndex).KualaLumpur.cases_adult,
                result.get(lastIndex).KualaLumpur.cases_elderly, result.get(lastIndex).KualaLumpur.cases_active, result.get(lastIndex).KualaLumpur.activeToday,
                result.get(lastIndex).KualaLumpur.cases_import, result.get(lastIndex).KualaLumpur.cases_recovered, result.get(lastIndex).KualaLumpur.totalRecovered,
                result.get(lastIndex).KualaLumpur.deaths_new,result.get(lastIndex).KualaLumpur.totalDeath,result.get(lastIndex).KualaLumpur.deaths_bid));
        data.add(new CustomDataEntry("MY.PJ","Putrajaya",result.get(lastIndex).Putrajaya.cases_new,result.get(lastIndex).Putrajaya.totalConfirmed,
                result.get(lastIndex).Putrajaya.cases_child, result.get(lastIndex).Putrajaya.cases_adolescent,result.get(lastIndex).Putrajaya.cases_adult,
                result.get(lastIndex).Putrajaya.cases_elderly, result.get(lastIndex).Putrajaya.cases_active, result.get(lastIndex).Putrajaya.activeToday,
                result.get(lastIndex).Putrajaya.cases_import, result.get(lastIndex).Putrajaya.cases_recovered, result.get(lastIndex).Putrajaya.totalRecovered,
                result.get(lastIndex).Putrajaya.deaths_new,result.get(lastIndex).Putrajaya.totalDeath,result.get(lastIndex).Putrajaya.deaths_bid));
        data.add(new CustomDataEntry("MY.LA","Labuan",result.get(lastIndex).Labuan.cases_new,result.get(lastIndex).Labuan.totalConfirmed,
                result.get(lastIndex).Labuan.cases_child, result.get(lastIndex).Labuan.cases_adolescent,result.get(lastIndex).Labuan.cases_adult,
                result.get(lastIndex).Labuan.cases_elderly, result.get(lastIndex).Labuan.cases_active, result.get(lastIndex).Labuan.activeToday,
                result.get(lastIndex).Labuan.cases_import, result.get(lastIndex).Labuan.cases_recovered, result.get(lastIndex).Labuan.totalRecovered,
                result.get(lastIndex).Labuan.deaths_new,result.get(lastIndex).Labuan.totalDeath,result.get(lastIndex).Labuan.deaths_bid));
        return data;
    }

    static class CustomDataEntry extends DataEntry {
        public CustomDataEntry(String id, String name,Number value, Number totalConfirm, Number child,
                               Number adolescent, Number adult, Number elderly, Number active,Number activeToday,
                               Number imported, Number recovered, Number totalRecovered, Number deaths,
                               Number totalDeath, Number deathBID) {
            setValue("id", id);
            setValue("name", name);
            setValue("value", value);
            setValue("totalConfirm", totalConfirm);
            setValue("child", child);
            setValue("adolescent", adolescent);
            setValue("adult", adult);
            setValue("elderly", elderly);
            setValue("active", active);
            setValue("activeToday", activeToday);
            setValue("imported", imported);
            setValue("recovered", recovered);
            setValue("totalRecovered", totalRecovered);
            setValue("deaths", deaths);
            setValue("totalDeath", totalDeath);
            setValue("deathBID", deathBID);

        }
    }

    private void drawMap(ArrayList<Malaysia> test){
        APIlib.getInstance().setActiveAnyChartView(mapView);
        Map map = AnyChart.map();

        //mapView = binding.malaysia;
        mapView.setProgressBar(findViewById(R.id.progressBar));

        map.geoData("anychart.maps.malaysia");

        ColorRange colorRange = map.colorRange();
        colorRange.enabled(true)
                .colorLineSize(10)
                .stroke("#B9B9B9")
                .labels("{ 'padding': 3 }")
                .labels("{ 'size': 7 }");
        colorRange.ticks()
                .enabled(true)
                .stroke("#B9B9B9")
                .position(SidePosition.OUTSIDE)
                .length(10);
        colorRange.minorTicks()
                .enabled(true)
                .stroke("#B9B9B9")
                .position("outside")
                .length(5);

        map.interactivity().selectionMode(SelectionMode.NONE);

        //map.zoom(2,2,1.5,0);
        map.padding(0, 0, 0, 0);

        map.background().fill("#000000",0);

        series = map.choropleth(getData(test));
        LinearColor linearColor = LinearColor.instantiate();
        linearColor.colors(new String[]{ "#c2e9fb", "#81d4fa", "#01579b", "#002746"});
        series.colorScale(linearColor);
        series.hovered()
                .fill("#f48fb1")
                .stroke("#f99fb9");
        series.selected()
                .fill("#c2185b")
                .stroke("#c2185b");

        series.labels().enabled(true);
        series.labels().fontSize(10);
        if(NightMode == 2){
            series.labels().fontColor("#747474");
        }else{
            series.labels().fontColor("#121212");
        }
        series.labels().fontWeight(800);
        series.labels().format("{%Value}");

        series.tooltip(false);

        map.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"id", "name", "value",
                "totalConfirm", "child","adolescent","adult","elderly","active","activeToday","imported",
                "recovered","totalRecovered","deaths","totalDeath","deathBID" }) {
            @Override
            public void onClick(Event event) {
                runOnUiThread(() -> {
                    // This code will always run on the UI thread, therefore is safe to modify UI elements.
                    state.setText(event.getData().get("name"));
                    infectionM.setText(Home.formatNumber(String.valueOf(event.getData().get("totalConfirm"))));
                    infectionTodayM.setText(getString(R.string.today, Home.formatNumber(event.getData().get("value"))));
                    infectionChild.setText(getString(R.string.child, Home.formatNumber(event.getData().get("child"))));
                    infectionAdolescent.setText(getString(R.string.adolescent, Home.formatNumber(event.getData().get("adolescent"))));
                    infectionAdult.setText(getString(R.string.adult, Home.formatNumber(String.valueOf(event.getData().get("adult")))));
                    infectionElderly.setText(getString(R.string.elderly, Home.formatNumber(event.getData().get("elderly"))));
                    recoveredM.setText(Home.formatNumber(String.valueOf(event.getData().get("totalRecovered"))));
                    recoveredTodayM.setText(getString(R.string.today, Home.formatNumber(event.getData().get("recovered"))));
                    activeM.setText(Home.formatNumber(String.valueOf(event.getData().get("active"))));
                    activeTodayM.setText(calActiveDiff(Integer.parseInt(Objects.requireNonNull(event.getData().get("activeToday")))));
                    deathM.setText(Home.formatNumber(String.valueOf(event.getData().get("totalDeath"))));
                    deathTodayM.setText(getString(R.string.today, Home.formatNumber(event.getData().get("deaths"))));
                    deathTodayBID.setText(getString(R.string.brought_in_death, Home.formatNumber(event.getData().get("deathBID"))));
                });
            }
        });
        mapView.addScript("https://cdn.anychart.com/releases/8.11.0/geodata/countries/malaysia/malaysia.js");
        mapView.addScript("https://cdnjs.cloudflare.com/ajax/libs/proj4js/2.7.5/proj4.js");
        mapView.setChart(map);
    }
}