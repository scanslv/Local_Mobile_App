package com.iivanovs.locals;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iivanovs.locals.entity.Local;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class WeatherData extends AsyncTask<Local, Void, String> {

    private Exception exception;
    private final String APIURL = "http://api.openweathermap.org/data/2.5/weather";
    private final String API_KEY = "&appid=66efe6cd305a3b36249ab9f2185f99ec";
    private final String UNITS = "&units=metric";
    Context context;
    Activity activity;

    public WeatherData(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    protected void onPreExecute() {
//        this.context =context;
//        progressBar.setVisibility(View.VISIBLE);
//        responseView.setText("");
    }

    protected String doInBackground(Local... local) {
        Local currentLocal = local[0];
        try {
            URL url = new URL(APIURL + "?lat=" + currentLocal.getLat() +
                    "&lon=" + currentLocal.getLon() + API_KEY + UNITS);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(String response) {
        LinearLayout weather_info_layout;
        TextView cityField, weather_deckField, tempField, min_maxField, windField, cloudField;
        ImageView weather_icon;
        weather_info_layout = (LinearLayout) activity.findViewById(R.id.weather_info_layout);
        cityField = (TextView) activity.findViewById(R.id.city);
        weather_deckField = (TextView) activity.findViewById(R.id.weather_deck);
        tempField = (TextView) activity.findViewById(R.id.temp);
        min_maxField = (TextView) activity.findViewById(R.id.min_max);
        windField = (TextView) activity.findViewById(R.id.wind);
        cloudField = (TextView) activity.findViewById(R.id.cloud);
        weather_icon = (ImageView) activity.findViewById(R.id.weather_icon);

        JSONObject obj;
        if (response == null) {
            response = "THERE WAS AN ERROR";
        }
        try {
            obj = new JSONObject(response);
            JSONArray weather = obj.getJSONArray("weather");

            JSONObject weather0 = weather.getJSONObject(0);
            String mainDesc = weather0.getString("main");
            String icon = "w" + weather0.getString("icon");

            JSONObject mainObj = obj.getJSONObject("main");
            String temp = mainObj.getString("temp") + "\u00b0" + "C";
            String temp_min = mainObj.getString("temp_min") + "\u00b0" + "C";
            String temp_max = mainObj.getString("temp_max") + "\u00b0" + "C";

            JSONObject wind = obj.getJSONObject("wind");
            String speed = wind.getString("speed") + " m/s";

            JSONObject clouds = obj.getJSONObject("clouds");
            String clouds_percentage = clouds.getString("all") + "%";

            String city = obj.getString("name");

            cityField.setText(city);
            weather_deckField.setText(mainDesc);
            tempField.setText(temp);
            min_maxField.setText("min: " + temp_min + ", max: " + temp_max);
            windField.setText("Wind: " + speed);
            cloudField.setText("Cloud coverage: " + clouds_percentage);

            int resId = activity.getResources().getIdentifier(icon, "drawable", activity.getPackageName());
            Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), resId);
            weather_icon.setImageBitmap(bm);

            weather_info_layout.setVisibility(View.VISIBLE);

            Log.i("INFO", response);


        } catch (JSONException e) {
            Log.i("INFO", e.getMessage());
        }
    }
}
