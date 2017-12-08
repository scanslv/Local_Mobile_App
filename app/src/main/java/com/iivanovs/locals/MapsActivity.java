package com.iivanovs.locals;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    private String intentOption = "Nothing";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private ActionBar actionBar;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private static int LOCATION_COUNTER = 1;
    private static LatLng origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        final DBManager db = new DBManager(this);
        mMap = googleMap;

        //get intent and pass in the location to zoom to
        Intent intent = getIntent();
        if(intent != null){
            if(intent.getStringExtra("Option") != null)
                intentOption = intent.getStringExtra("Option");
        }

        if(intentOption.equals("NearbyLocations")){
            LatLng local = new LatLng(intent.getDoubleExtra("Lat", 0), intent.getDoubleExtra("Lon", 0));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(local, 15.0f));
        }
        else if(intentOption.equals("Directions")){
            currentLocation();
        }


        for(int i = 0; i < db.getAllLocals().size(); i++){
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(db.getAllLocals().get(i).getLat()), Double.parseDouble(db.getAllLocals().get(i).getLon())))
                    .title(db.getAllLocals().get(i).getDescription());
            mMap.addMarker(marker);
            System.out.println(db.getAllLocals().get(i).getLat() + "---" + db.getAllLocals().get(i).getLon());
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MainActivity.incrementCounter();

                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(point.latitude, point.longitude))
                        .title("Location no." + MainActivity.getCounter());
                mMap.addMarker(marker);

                Intent intent = new Intent(MapsActivity.this, CreateLocationActivity.class);
                intent.putExtra("lat", point.latitude);
                intent.putExtra("lon", point.longitude);
                startActivity(intent);
            }
        });
    }

    //location stuff code
    private void currentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            System.out.println("Location received...");

            origin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            Intent intent = getIntent();
            if(intent != null){
                if(intent.getStringExtra("Option") != null)
                    intentOption = intent.getStringExtra("Option");
            }
             if(intentOption.equals("Directions")) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13.0f));
                LatLng destination = new LatLng(intent.getDoubleExtra("Lat", 0), intent.getDoubleExtra("Lon", 0));


//                 Object dataTransfer[] = new Object[3];
//                 String url = getDirectionsUrl(origin.latitude, origin.longitude, destination.latitude, destination.longitude);
//                 GetDirectionsData getDirectionsData = new GetDirectionsData();
//                 dataTransfer[0] = mMap;
//                 dataTransfer[1] = url;
//                 dataTransfer[2] = destination;
//                 getDirectionsData.execute(dataTransfer);

                System.out.println("System out stuff printing...." + destination.toString());
                 Geocoder geocoder;
                 List<Address> addresses;
                 geocoder = new Geocoder(this, Locale.getDefault());

                 try {
                     addresses = geocoder.getFromLocation(destination.latitude, destination.longitude, 1);
                     String address = addresses.get(0).getAddressLine(0);
                     System.out.println(address);

                     String uri = "https://www.google.com/maps/dir/?api=1&origin=" + origin.latitude+","+origin.longitude+"&destination="+destination.latitude+","+destination.longitude+"&travelmode=driving&dir_action=navigate";
                     intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                     intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                     startActivity(intent);

                 } catch (IOException e) {
                     e.printStackTrace();
                 }
            }
        }
    }


    //
    //GoogleAPIClient method overrides
    //
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
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

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(MainActivity.class.getSimpleName(), "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        currentLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


//    Is used with GetDirectionsData, DataParser and commented out code in currentLocation() method
//    to draw a path/direction on the map activity
//    private String getDirectionsUrl(double latitude, double longitude, double end_latitude, double end_longitude)
//    {
//        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
//        googleDirectionsUrl.append("origin="+latitude+","+longitude);
//        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
//        googleDirectionsUrl.append("&key="+"AIzaSyCAcfy-02UHSu2F6WeQ1rhQhkCr51eBL9g");
//
//        return googleDirectionsUrl.toString();
//    }
}
