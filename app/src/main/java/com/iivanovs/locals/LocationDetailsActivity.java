package com.iivanovs.locals;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.iivanovs.locals.entity.Local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationDetailsActivity extends AppCompatActivity {
    private final String LOCATION_ID = "LOCATION_ID";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private ActionBar actionBar;
    TextView locations_saved, pictures_taken, coordinates, address;
    EditText description;
    LinearLayout profile_info_layout;
    RelativeLayout profile_info_btn, directions_btn, nearby_places_btn, save_btn, delete_btn, take_picture_btn;
    DBManager db;
    Local local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        int id = getIntent().getIntExtra(LOCATION_ID, -1);

        db = new DBManager(this);
        local = db.getLocalById(id);
        setTitle(local.getDescription());

        description = (EditText) findViewById(R.id.description);
        coordinates = (TextView) findViewById(R.id.coordinates);
        address = (TextView) findViewById(R.id.address);

        description.setText(local.getDescription());
        coordinates.setText("(" + local.getLat() + ", " + local.getLon() + ")");
        address.setText(getAddress(local));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerTitle = getString(R.string.drawer_open);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        profile_info_layout = (LinearLayout) findViewById(R.id.profile_info_layout);
        profile_info_layout.setVisibility(View.INVISIBLE);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.app_name
        ) {
            public void onDrawerClosed(View view) {
                actionBar.setTitle(getTitle());
                profile_info_layout.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(mDrawerTitle);
                profile_info_layout.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        locations_saved = (TextView) findViewById(R.id.locations_saved);
        pictures_taken = (TextView) findViewById(R.id.pictures_taken);

        findViewById(R.id.scrollView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return hideKeyboard();
            }
        });

        setButtonListeners();
    }

    private void setButtonListeners() {

        profile_info_btn = (RelativeLayout) findViewById(R.id.profile_info_btn);
        directions_btn = (RelativeLayout) findViewById(R.id.directions_btn);
        nearby_places_btn = (RelativeLayout) findViewById(R.id.nearby_places_btn);
        save_btn = (RelativeLayout) findViewById(R.id.save_btn);
        delete_btn = (RelativeLayout) findViewById(R.id.delete_btn);
        take_picture_btn = (RelativeLayout) findViewById(R.id.take_picture_btn);

        profile_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile_info_layout.getVisibility() == View.VISIBLE)
                    profile_info_layout.setVisibility(View.INVISIBLE);
                else
                    getProfileInfo();
            }
        });

        directions_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        nearby_places_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (local != null) {
                    local.setDescription(description.getText().toString());
                    db.updateLocal(local);
                    Toast.makeText(LocationDetailsActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (local != null) {
                    db.deleteLocal(local);
                    Toast.makeText(LocationDetailsActivity.this, "Location deleted", Toast.LENGTH_SHORT).show();
                    LocationDetailsActivity.this.finish();
                }
            }
        });

        take_picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(left_drawer);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
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

    public String getAddress(Local local) {
        Geocoder geocoder;
        List<Address> addresses;
        String address = "";
        try {
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(Double.parseDouble(local.getLat()), Double.parseDouble(local.getLon()), 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0) + ", " +
                addresses.get(0).getLocality() + ", " +
                addresses.get(0).getAdminArea() + ", " +
                addresses.get(0).getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    private boolean hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;


    }
}
