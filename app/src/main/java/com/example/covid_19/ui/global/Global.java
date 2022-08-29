package com.example.covid_19.ui.global;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.covid_19.R;
import com.example.covid_19.Setting;
import com.example.covid_19.ui.charts.Chart;
import com.example.covid_19.ui.home.Home;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

import java.util.HashMap;

public class Global extends AppCompatActivity {
    HashMap<String,String> worldWide;
    String[] global = {"cases","todayCases","deaths","todayDeaths","recovered","todayRecovered","active"};
    TextView cases,todayCases,deaths,todayDeaths,recovered,todayRecovered,active,todayActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);
        cases = findViewById(R.id.cases);
        todayCases = findViewById(R.id.todayCases);
        deaths = findViewById(R.id.deaths);
        todayDeaths = findViewById(R.id.todayDeaths);
        recovered = findViewById(R.id.recovered);
        todayRecovered = findViewById(R.id.todayRecovered);
        active = findViewById(R.id.active);
        todayActive = findViewById(R.id.todayActive);
        setUpNavigation();

        final LinearLayout globalBackground = findViewById(R.id.globalBackground);
        if (Home.NightMode == 2) {
            globalBackground.setBackgroundResource(R.drawable.background_night);
        } else if (Home.NightMode == 1) {
            globalBackground.setBackgroundResource(R.drawable.background);
        }

        worldWide = new HashMap<>();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://disease.sh/v3/covid-19/all?yesterday=true&allowNull=true";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try{
                        for (String s : global) {
                            String temp = response.getString(s);
                            temp = Home.formatNumber(temp);
                            worldWide.put(s, temp);
                        }
                        cases.setText(worldWide.get(global[0]));
                        todayCases.setText(getString(R.string.today, worldWide.get(global[1])));
                        deaths.setText(worldWide.get(global[2]));
                        todayDeaths.setText(getString(R.string.today, worldWide.get(global[3])));
                        recovered.setText(worldWide.get(global[4]));
                        todayRecovered.setText(getString(R.string.today, worldWide.get(global[5])));
                        active.setText(worldWide.get(global[6]));
                        todayActive.setText(calActive(Integer.parseInt(response.getString("todayCases")),
                                Integer.parseInt(response.getString("todayRecovered")),
                                Integer.parseInt(response.getString("todayDeaths"))));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    //Handle error
                    cases.setText(R.string.error);
                    todayCases.setText(R.string.blank);
                    deaths.setText(R.string.error);
                    todayDeaths.setText(R.string.blank);
                    recovered.setText(R.string.error);
                    todayRecovered.setText(R.string.blank);
                    active.setText(R.string.error);
                    todayActive.setText(R.string.blank);
                }
                );
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

        private void setUpNavigation(){
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.global);
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

    public void setting(View view){
        Intent settingsPage = new Intent(this, Setting.class);
        startActivity(settingsPage);
    }

    public String calActive(Integer confirm,Integer recover,Integer death){
        int result = confirm - recover - death;
        if (result > 0) {
            return "(+" + Home.formatNumber(Integer.toString(result)) + " Today)";
        }
        return "(" + Home.formatNumber(Integer.toString(result)) + " Today)" ;
    }
}