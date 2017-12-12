package com.iivanovs.locals;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.iivanovs.locals.entity.Local;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CreateLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String LOCATION_ID = "LOCATION_ID";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private ActionBar actionBar;
    TextView locations_saved, pictures_taken, coordinates, address, address_title;
    EditText description;
    LinearLayout profile_info_layout, weather_info_layout, location_weather_info_layout;
    RelativeLayout profile_info_btn, directions_btn, nearby_places_btn,
            save_btn, delete_btn, take_picture_btn, all_locationds_btn, weather_btn, map_btn, location_weather_info_btn;
    DBManager db;
    Local local;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private static int LOCATION_COUNTER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        int id = getIntent().getIntExtra(LOCATION_ID, -1);

        double lat = getIntent().getDoubleExtra("lat", -1);
        double lon = getIntent().getDoubleExtra("lon", -1);

        db = new DBManager(this);
        local = new Local();
        local.setLat(Double.toString(lat));
        local.setLon(Double.toString(lon));

        description = (EditText) findViewById(R.id.description);
        coordinates = (TextView) findViewById(R.id.coordinates);
        new GetAddressPositionTask().execute(local);
        address = (TextView) findViewById(R.id.address);

        description.setBackgroundResource(R.drawable.text_field_style);

        coordinates.setText("(" + lat + ", " + lon + ")");
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

        map_btn = (RelativeLayout) findViewById(R.id.map_btn);


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
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(mDrawerTitle);
                profile_info_layout.setVisibility(View.INVISIBLE);
                weather_info_layout.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        locations_saved = (TextView) findViewById(R.id.locations_saved);
        pictures_taken = (TextView) findViewById(R.id.pictures_taken);

        findViewById(R.id.mainLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return hideKeyboard();
            }
        });
        buildGoogleApiClient();

        setButtonListeners();
    }

    private void setButtonListeners() {

        profile_info_btn = (RelativeLayout) findViewById(R.id.profile_info_btn);
        weather_btn = (RelativeLayout) findViewById(R.id.weather_btn);
        all_locationds_btn = (RelativeLayout) findViewById(R.id.all_locationds_btn);
        save_btn = (RelativeLayout) findViewById(R.id.save_btn);

        delete_btn = (RelativeLayout) findViewById(R.id.delete_btn);
        TextView t = (TextView) findViewById(R.id.textView8);
        t.setText(R.string.cancel);

        nearby_places_btn = (RelativeLayout) findViewById(R.id.nearby_places_btn);
        nearby_places_btn.setVisibility(View.GONE);

        directions_btn = (RelativeLayout) findViewById(R.id.directions_btn);
        directions_btn.setVisibility(View.GONE);

        take_picture_btn = (RelativeLayout) findViewById(R.id.take_picture_btn);
        take_picture_btn.setVisibility(View.GONE);

        location_weather_info_btn = (RelativeLayout) findViewById(R.id.location_weather_info_btn);
        location_weather_info_btn.setVisibility(View.GONE);

        location_weather_info_layout = (LinearLayout) findViewById(R.id.location_weather_info_layout);
        location_weather_info_layout.setVisibility(View.GONE);

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
                startActivity(new Intent(CreateLocationActivity.this, MainActivity.class));
                CreateLocationActivity.this.finish();
            }
        });

        weather_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_info_layout.setVisibility(View.INVISIBLE);
                getWeatherInfo();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (local != null) {
                    local.setDescription(description.getText().toString());
                    db.createLocal(local);
                    Toast.makeText(CreateLocationActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateLocationActivity.this, MainActivity.class));
                    CreateLocationActivity.this.finish();
                }
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateLocationActivity.super.onBackPressed();
            }
        });

        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateLocationActivity.this, MapsActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
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
        switch (item.getItemId()) {
            case R.id.action_websearch:
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, actionBar.getTitle());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Toast message", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ConstraintLayout left_drawer = (ConstraintLayout) findViewById(R.id.left_drawer);
        menu.findItem(R.id.action_websearch).setVisible(false);
        hideKeyboard();
        return super.onPrepareOptionsMenu(menu);
    }

    private void getProfileInfo() {
        int locationSaved = db.getAllLocals().size();
        int picturesTaken = db.getTotalLocalImgs().size();

        locations_saved.setText(getString(R.string.locations_saved) + " " + String.valueOf(locationSaved));
        pictures_taken.setText(getString(R.string.total_pictures_taken) + " " + String.valueOf(picturesTaken));
        profile_info_layout.setVisibility(View.VISIBLE);
    }

    private void getWeatherInfo() {
        if (weather_info_layout.getVisibility() == View.VISIBLE)
            weather_info_layout.setVisibility(View.INVISIBLE);
        else {
            if (ActivityCompat.checkSelfPermission(CreateLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CreateLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateLocationActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                new WeatherData(this, weather_info_layout).execute(new Local("Current location", Double.toString(mLastLocation.getLatitude()),
                        Double.toString(mLastLocation.getLongitude())));
            }
        }
    }
    private boolean hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    private class GetAddressPositionTask extends AsyncTask<Local, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address_title = (TextView) findViewById(R.id.address_title);
            address_title.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(Local... params) {
            Geocoder geocoder;
            List<Address> addresses;
            String addr = "";
            try {
                geocoder = new Geocoder(CreateLocationActivity.this, Locale.getDefault());

                addresses = geocoder.getFromLocation(Double.parseDouble(local.getLat()), Double.parseDouble(local.getLon()), 1);
                if (addresses != null && addresses.size() > 0) {
                    addr = addresses.get(0).getAddressLine(0) + ", " +
                            addresses.get(0).getLocality() + ", " +
                            addresses.get(0).getAdminArea() + ", " +
                            addresses.get(0).getCountryName();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!result.equalsIgnoreCase("")) {
                address_title.setVisibility(View.VISIBLE);
                address.setText(result);
            }
        }
    }

    //location stuff code

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(CreateLocationActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(MainActivity.class.getSimpleName(), "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateLocationActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        // Once connected with google api, get the location
    }

    @Override
    public void onLocationChanged(Location location) {
        //Whatever is required when the location changes
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public static void incrementCounter(){
        LOCATION_COUNTER++;
    }

    public static int getCounter(){ return LOCATION_COUNTER; }
}