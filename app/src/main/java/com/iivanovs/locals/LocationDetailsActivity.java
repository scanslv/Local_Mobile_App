package com.iivanovs.locals;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.iivanovs.locals.entity.LocalImg;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationDetailsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Activity thisActivity;
    boolean weather_displayed = false;
    String mCurrentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
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
    private int mShortAnimationDuration;
    private Animator mCurrentAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        thisActivity = this;
        int id = getIntent().getIntExtra(LOCATION_ID, -1);

        db = new DBManager(this);
        local = db.getLocalById(id);
        setTitle(local.getDescription());

        description = (EditText) findViewById(R.id.description);
        coordinates = (TextView) findViewById(R.id.coordinates);
        address = (TextView) findViewById(R.id.address);

        description.setText(local.getDescription());
        description.setBackgroundResource(R.drawable.text_field_style);

        coordinates.setText("(" + local.getLat() + ", " + local.getLon() + ")");
        new GetAddressPositionTask().execute(local);
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
        location_weather_info_layout = (LinearLayout) findViewById(R.id.location_weather_info_layout);
        location_weather_info_layout.setVisibility(View.GONE);

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

        locations_saved = (TextView) findViewById(R.id.locations_saved);
        pictures_taken = (TextView) findViewById(R.id.pictures_taken);

        findViewById(R.id.mainLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return hideKeyboard();
            }
        });

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        buildGoogleApiClient();

        setButtonListeners();
        showImgs();
    }

    private void setButtonListeners() {

        profile_info_btn = (RelativeLayout) findViewById(R.id.profile_info_btn);
        weather_btn = (RelativeLayout) findViewById(R.id.weather_btn);
        location_weather_info_btn = (RelativeLayout) findViewById(R.id.location_weather_info_btn);
        all_locationds_btn = (RelativeLayout) findViewById(R.id.all_locationds_btn);
        directions_btn = (RelativeLayout) findViewById(R.id.directions_btn);
        nearby_places_btn = (RelativeLayout) findViewById(R.id.nearby_places_btn);
        save_btn = (RelativeLayout) findViewById(R.id.save_btn);
        delete_btn = (RelativeLayout) findViewById(R.id.delete_btn);
        take_picture_btn = (RelativeLayout) findViewById(R.id.take_picture_btn);

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
                startActivity(new Intent(LocationDetailsActivity.this, MainActivity.class));
                LocationDetailsActivity.this.finish();
            }
        });

        weather_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_info_layout.setVisibility(View.INVISIBLE);
                getWeatherInfo();
            }
        });

        directions_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationDetailsActivity.this, MapsActivity.class);
                intent.putExtra("Option", "Directions");
                intent.putExtra("Lat", Double.parseDouble(local.getLat()));
                intent.putExtra("Lon", Double.parseDouble(local.getLon()));
                intent.putExtra("Description", local.getDescription());
                startActivity(intent);

            }
        });

        nearby_places_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationDetailsActivity.this, MapsActivity.class);
                intent.putExtra("Option", "NearbyLocations");
                intent.putExtra("Lat", Double.parseDouble(local.getLat()));
                intent.putExtra("Lon", Double.parseDouble(local.getLon()));
                startActivity(intent);
            }
        });

        location_weather_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView btn_image = (ImageView) findViewById(R.id.location_weather_info_btn_icon);
                TextView btn_text = (TextView) findViewById(R.id.location_weather_info_btn_text);
                if (!weather_displayed) {
                    weather_displayed = true;
                    new WeatherData(thisActivity, location_weather_info_layout).execute(local);
                    btn_text.setText(R.string.hide_weather_information);
                    int resId = thisActivity.getResources().getIdentifier("ic_arrow_up", "mipmap", thisActivity.getPackageName());
                    Bitmap bm = BitmapFactory.decodeResource(thisActivity.getResources(), resId);
                    btn_image.setImageBitmap(bm);
                } else {
                    weather_displayed = false;
                    location_weather_info_layout.setVisibility(View.GONE);
                    btn_text.setText(R.string.show_weather_information);
                    int resId = thisActivity.getResources().getIdentifier("ic_arrow_down", "mipmap", thisActivity.getPackageName());
                    Bitmap bm = BitmapFactory.decodeResource(thisActivity.getResources(), resId);
                    btn_image.setImageBitmap(bm);
                }
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(LocationDetailsActivity.this);
                    builder.setTitle("Remove location?");
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.deleteLocal(local);
                            dialog.dismiss();
                            Toast.makeText(LocationDetailsActivity.this, "Location removed", Toast.LENGTH_SHORT).show();
                            LocationDetailsActivity.this.finish();
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        take_picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LocationDetailsActivity.this, MapsActivity.class));
            }
        });
    }

    private void showImgs() {
        ArrayList<LocalImg> localImgs;
        localImgs = (ArrayList<LocalImg>) db.getAllLocalImgs(local.getId());

        ViewGroup parentLeft = (ViewGroup) findViewById(R.id.left_img_column);
        ViewGroup parentRight = (ViewGroup) findViewById(R.id.right_img_column);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        parentLeft.removeAllViews();
        parentRight.removeAllViews();

        if (localImgs.size() != 0) {

            for (int i = localImgs.size() - 1; i >= 0; i--) {
                final LocalImg locationImg = localImgs.get(i);
                View view = inflater.inflate(R.layout.img_layout, null);

                final ImageView img = (ImageView) view.findViewById(R.id.location_image);
                ImageView delete_btn = (ImageView) view.findViewById(R.id.remove_location_image);
                setPic(img, locationImg.getImg_path());

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zoomImageFromThumb(img, locationImg.getImg_path());
                    }
                });

                delete_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LocationDetailsActivity.this);
                        builder.setTitle("Remove image?");
                        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.deleteLocalImg(locationImg);
                                dialog.dismiss();
                                Toast.makeText(LocationDetailsActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
                                showImgs();
                            }
                        });
                        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                if (i % 2 == 0)
                    parentLeft.addView(view);
                else
                    parentRight.addView(view);
            }
        }
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
        else {
            if (ActivityCompat.checkSelfPermission(LocationDetailsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationDetailsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LocationDetailsActivity.this, new String[]{
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            LocalImg localImg = new LocalImg(mCurrentPhotoPath, local.getId());
            db.createLocalImg(localImg);
            showImgs();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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
                geocoder = new Geocoder(LocationDetailsActivity.this, Locale.getDefault());

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

    private void setPic(final ImageView imageView, final String img_path) {
        final LinearLayout layout = (LinearLayout) findViewById(R.id.left_img_column);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = layout.getMeasuredWidth();
                int height = layout.getMeasuredHeight();

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(img_path, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                int scaleFactor = Math.min(photoW / width, photoH / height);

                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;
                bmOptions.inScaled = true;

                Bitmap bitmap = BitmapFactory.decodeFile(img_path, bmOptions);
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void zoomImageFromThumb(final View thumbView, String img_path) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        setPic(expandedImageView, img_path);
//        Bitmap bitmap = BitmapFactory.decodeFile(img_path);
//        expandedImageView.setImageBitmap(bitmap);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.mainLayout)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        int ff = startBounds.left;
        int f = finalBounds.left;
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, 50))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    //location stuff code

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(LocationDetailsActivity.this)
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
            ActivityCompat.requestPermissions(LocationDetailsActivity.this, new String[]{
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

    public static void incrementCounter() {
        LOCATION_COUNTER++;
    }

    public static int getCounter() {
        return LOCATION_COUNTER;
    }
}
