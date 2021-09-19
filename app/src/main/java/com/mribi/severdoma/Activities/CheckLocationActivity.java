package com.mribi.severdoma.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mribi.severdoma.R;

public class CheckLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude = 0;
    private double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_select);

        Intent intent = getIntent();
        Log.i("main", "start");
        if (intent != null){
            latitude = intent.getDoubleExtra(AddNewBisiness.LATITUDE, 0);
            longitude = intent.getDoubleExtra(AddNewBisiness.LONGITUDE, 0);
        }
        else{
            Toast.makeText(this, "Не удалось получить данные", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.i("main", "cam");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))).setVisible(true);
        mMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(latitude, longitude), new LatLng(latitude, longitude)));
        mMap.setMinZoomPreference(10);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }
}
