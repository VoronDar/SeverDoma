package com.mribi.severdoma.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mribi.severdoma.R;

public class SelectLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude = 0;
    private double longitude = 0;

    private boolean isMarker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_map_select);

        Intent intent = getIntent();
        Log.i("main", "start");
        if (intent != null){
            Log.i("main", "BUNDLE");
            isMarker = true;
            latitude = intent.getDoubleExtra(AddNewBisiness.LATITUDE, 0);
            longitude = intent.getDoubleExtra(AddNewBisiness.LONGITUDE, 0);
            if ((int)latitude == 0 || (int)longitude == 0) isMarker = false;
            Log.i("main", Boolean.toString(isMarker));
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int)latitude == 0 || (int)longitude == 0) return;
                Intent intent = new Intent();
                intent.putExtra(AddNewBisiness.LATITUDE, latitude);
                intent.putExtra(AddNewBisiness.LONGITUDE, longitude);
                setResult(AddNewBisiness.RESULT_CODE_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.i("main", "cam");
        //if (!isMarker)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(68.9791700, 33.094166703522205)));
        //else{
        //    //mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        //    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))).setVisible(true);
        //}

        mMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(68.90443871066205, 33.038494773209095), new LatLng(68.9915702698826, 33.26001238077879)));
        mMap.setMinZoomPreference(11);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                       @Override
                                       public void onMapClick(LatLng latLng) {
                                           mMap.clear();
                                           mMap.addMarker(new MarkerOptions().position(latLng)).setVisible(true);
                                           latitude = latLng.latitude;
                                           longitude = latLng.longitude;
                                       }
                                   });

        }
}
