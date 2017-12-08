package com.iivanovs.locals;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.iivanovs.locals.entity.Local;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String LOCATION_ID = "LOCATION_ID";
    private SearchView searchBar;
    private RelativeLayout save_current_location_btn;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private ActionBar actionBar;
    LinearLayout profile_info_layout, weather_info_layout;
    RelativeLayout profile_info_btn, weather_btn, all_locationds_btn;
    TextView locations_saved, pictures_taken;
    LinearLayout location_layout;
    DBManager db;
    ArrayList<Local> locationList;

    @Override
    protected void onRestart() {
        super.onRestart();
        locationList = (ArrayList<Local>) db.getAllLocals();
        displayLocationList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBManager(this);
        locationList = (ArrayList<Local>) db.getAllLocals();
        location_layout = (LinearLayout) findViewById(R.id.location_layout);
        locations_saved = (TextView) findViewById(R.id.locations_saved);
        pictures_taken = (TextView) findViewById(R.id.pictures_taken);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerTitle = getString(R.string.drawer_open);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        profile_info_layout = (LinearLayout) findViewById(R.id.profile_info_layout);
        profile_info_layout.setVisibility(View.INVISIBLE);
        weather_info_layout = (LinearLayout) findViewById(R.id.weather_info_layout);
        weather_info_layout.setVisibility(View.INVISIBLE);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.app_name
        ) {
            public void onDrawerClosed(View view) {
                actionBar.setTitle(getTitle());
                profile_info_layout.setVisibility(View.INVISIBLE);
                weather_info_layout.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(mDrawerTitle);
                profile_info_layout.setVisibility(View.INVISIBLE);
                weather_info_layout.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        searchBar = (SearchView) findViewById(R.id.search_bar);
        searchBar.setBackgroundResource(R.drawable.text_field_style);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                locationList = (ArrayList<Local>) db.searchLocations(newText);
                displayLocationList();
                return false;
            }
        });

        findViewById(R.id.mainLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return hideKeyboard();
            }
        });

        setButtonListeners();
        displayLocationList();
    }

    private void setButtonListeners() {

        profile_info_btn = (RelativeLayout) findViewById(R.id.profile_info_btn);
        weather_btn = (RelativeLayout) findViewById(R.id.weather_btn);
        all_locationds_btn = (RelativeLayout) findViewById(R.id.all_locationds_btn);
        save_current_location_btn = (RelativeLayout) findViewById(R.id.save_current_location_btn);

        profile_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weather_info_layout.setVisibility(View.INVISIBLE);
                getProfileInfo();
            }
        });

        all_locationds_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_info_layout.setVisibility(View.INVISIBLE);
                weather_info_layout.setVisibility(View.INVISIBLE);
            }
        });

        weather_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_info_layout.setVisibility(View.INVISIBLE);
                getWeatherInfo();
            }
        });

        save_current_location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.action_websearch).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        hideKeyboard();
        return super.onPrepareOptionsMenu(menu);
    }

    private void getProfileInfo() {
        if (profile_info_layout.getVisibility() == View.VISIBLE)
            profile_info_layout.setVisibility(View.INVISIBLE);
        else {
            int locationSaved = db.getAllLocals().size();
            int picturesTaken = db.getTotalLocalImgs().size();

            locations_saved.setText(getString(R.string.locations_saved) + " " + String.valueOf(locationSaved));
            pictures_taken.setText(getString(R.string.total_pictures_taken) + " " + String.valueOf(picturesTaken));
            profile_info_layout.setVisibility(View.VISIBLE);
        }
    }

    private void getWeatherInfo() {
        if (weather_info_layout.getVisibility() == View.VISIBLE)
            weather_info_layout.setVisibility(View.INVISIBLE);
        else
            new WeatherData(this, weather_info_layout).execute(new Local("Current location", "53.3379581", "-6.2650733"));
    }

    private void displayLocationList() {

        TextView empty_list_title = (TextView) findViewById(R.id.empty_list_title);

        if (locationList.size() != 0) {
            location_layout.setVisibility(View.VISIBLE);
            empty_list_title.setVisibility(View.GONE);

            ViewGroup parent = (ViewGroup) findViewById(R.id.location_layout);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            parent.removeAllViews();

            for (int i = locationList.size()-1; i >= 0; i--) {
                final Local location = locationList.get(i);
                View view = inflater.inflate(R.layout.location_layout, null);

                RelativeLayout profile_info_btn = (RelativeLayout) view.findViewById(R.id.profile_info_btn);
                TextView description = (TextView) view.findViewById(R.id.description);
                TextView lonlat = (TextView) view.findViewById(R.id.lonlat);
                TextView date = (TextView) view.findViewById(R.id.date);

                description.setText(location.getDescription());
                lonlat.setText("(" + location.getLat() + ", " + location.getLon() + ")");
                date.setText(location.getDate());

                profile_info_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, LocationDetailsActivity.class);
                        i.putExtra(LOCATION_ID, location.getId());
                        startActivity(i);
                    }
                });
                parent.addView(view);
            }
        } else {
            location_layout.setVisibility(View.GONE);
            empty_list_title.setVisibility(View.VISIBLE);
        }
    }

    private boolean hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
}
