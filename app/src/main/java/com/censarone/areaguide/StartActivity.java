package com.censarone.areaguide;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.censarone.util.ConstantsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartActivity extends AppCompatActivity implements LocationListener {

    private String[] catValues;

    private LinearLayout linearLayout;
    private Button nextButton;

    private View.OnClickListener checkBoxOnClick;

    private List<CheckBox> checkBoxList = new ArrayList<>();

    private Map<Integer, String> categorySelectedMap = new HashMap<>();
    private double[] latLng = new double[2];

    private ProgressBar progressBar;

    private void setCurrentLocation(Location location) {
        latLng[0] = location.getLatitude();
        latLng[1] = location.getLongitude();
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initFields();
        addCheckBoxesIntoLayout();
        checkIfLocationEnabled();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        getCurrentLocation();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);





        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categorySelectedMap.size()<2)
                    Toast.makeText(StartActivity.this, "Please select at Least two categories before continuing", Toast.LENGTH_SHORT).show();
                else
                {
                    String[] selectedCategory = getSelectedCategory();
                    Intent intent = new Intent(StartActivity.this,PlacesListActivity.class);
                    intent.putExtra(ConstantsUtil.SELECTED_CATEGORY,selectedCategory);
                    intent.putExtra(ConstantsUtil.CURRENT_POSITION,latLng);
                    startActivity(intent);
                }
            }
        });


    }

    private void getCurrentLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try
        {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        }
        catch (SecurityException e)
        {
            Log.i("Location Tag","User declined the location");
            System.exit(0);
        }

    }

    private void checkIfLocationEnabled() {
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        final Context context = StartActivity.this;

        if(!gps_enabled ) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_error));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    System.exit(0);

                }
            });
            dialog.show();
        }
    }

    private String[] getSelectedCategory() {
        String[] selectedCategory = new String[categorySelectedMap.size()];
        int i=0;

        for(Map.Entry<Integer,String> entry : categorySelectedMap.entrySet())
        {
            selectedCategory[i] = entry.getValue();
            i++;
        }

        return selectedCategory;
    }

    private void addCheckBoxesIntoLayout() {
        for(int i = 0; i < catValues.length; i++) {
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setOnClickListener(checkBoxOnClick);
            cb.setId(i);
            cb.setText(catValues[i].toUpperCase());
            checkBoxList.add(cb);
            linearLayout.addView(cb);
        }
    }

    private void initFields() {
        linearLayout = findViewById(R.id.linear_layout);
        catValues = getResources().getStringArray(R.array.Categories);
        nextButton = findViewById(R.id.next_button);
        progressBar = findViewById(R.id.start_progress_bar);

        checkBoxOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CheckBox) v).isChecked();

                if(isChecked)
                {
                    if(categorySelectedMap.size()>=5)
                    {
                        Toast.makeText(StartActivity.this,"Sorry, but we only have time for five destinations !!",Toast.LENGTH_SHORT).show();
                        ((CheckBox) v).setChecked(false);
                    }
                    else
                        categorySelectedMap.put(v.getId(),catValues[v.getId()]);
                }
                else
                    categorySelectedMap.remove(v.getId());
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfLocationEnabled();
    }

    private void clearCheckBoxes() {
        for(CheckBox cb : checkBoxList)
            cb.setChecked(false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkIfLocationEnabled();
        clearCheckBoxes();
    }

    @Override
    public void onLocationChanged(Location location) {
        setCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();

                } else {
                    System.exit(0);
                }
            }
        }
    }
}
