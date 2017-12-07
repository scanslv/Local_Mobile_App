package com.iivanovs.locals;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iivanovs.locals.entity.Local;
import com.iivanovs.locals.entity.LocalImg;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationDetailsActivity extends AppCompatActivity {
    String mCurrentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final String LOCATION_ID = "LOCATION_ID";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private ActionBar actionBar;
    TextView locations_saved, pictures_taken, coordinates, address, address_title;
    EditText description;
    LinearLayout profile_info_layout;
    ImageView img_view;
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

        findViewById(R.id.mainLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return hideKeyboard();
            }
        });

        setButtonListeners();
        showImgs();
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
    }

    private void showImgs() {
        ArrayList<LocalImg> localImgs;
        localImgs = (ArrayList<LocalImg>) db.getAllLocalImgs(local.getId());

//        ViewGroup parent = (ViewGroup) findViewById(R.id.location_imgs_layout);
        ViewGroup parentLeft = (ViewGroup) findViewById(R.id.left_img_column);
        ViewGroup parentRight = (ViewGroup) findViewById(R.id.right_img_column);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        parentLeft.removeAllViews();
        parentRight.removeAllViews();

        if (localImgs.size() != 0) {

            for (int i = 0; i < localImgs.size(); i++) {
                final LocalImg locationImg = localImgs.get(i);
                View view = inflater.inflate(R.layout.img_layout, null);

                ImageView img = (ImageView) view.findViewById(R.id.location_image);
                ImageView delete_btn = (ImageView) view.findViewById(R.id.remove_location_image);
//                Bitmap bitmap = BitmapFactory.decodeFile(locationImg.getImg_path());
//                img.setImageBitmap(bitmap);
                setPic(img, locationImg.getImg_path());

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
        int locationSaved = db.getAllLocals().size();
        int picturesTaken = db.getTotalLocalImgs().size();

        locations_saved.setText(getString(R.string.locations_saved) + " " + String.valueOf(locationSaved));
        pictures_taken.setText(getString(R.string.total_pictures_taken) + " " + String.valueOf(picturesTaken));
        profile_info_layout.setVisibility(View.VISIBLE);
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
        final LinearLayout layout = (LinearLayout)findViewById(R.id.left_img_column);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width  = layout.getMeasuredWidth();
                int height = layout.getMeasuredHeight();


                // Get the dimensions of the View
                int targetW = width;
                int targetH = height;

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(img_path, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;
                bmOptions.inScaled = true;

                Bitmap bitmap = BitmapFactory.decodeFile(img_path, bmOptions);
                imageView.setImageBitmap(bitmap);

            }
        });



    }


}
