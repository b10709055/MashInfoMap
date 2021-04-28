package com.adam.testmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Point;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.android.gms.maps.MapsInitializer.initialize;

public class MapsActivity extends AppCompatActivity implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private boolean permissionDenied = false;
    private GoogleMap mMap;
    private UiSettings uiSettings;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkPermissions();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(25.015088, 121.542743);
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(true);
        start();
    }


    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "定位中...", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "要求權限" + permissions[index]
                                + "未授權，離開應用程式", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                initialize(this);
                break;
        }
    }

    private final static String mLogTag = "GeoJsonDemo";


    protected void start() {

        retrieveFileFromUrl();

    }


    private void retrieveFileFromUrl() {

        new DownloadGeoJsonFile().execute("https://raw.githubusercontent.com/kiang/pharmacies/master/json/points.json");

    }


    private class DownloadGeoJsonFile extends AsyncTask<String, Void, GeoJsonLayer> {


        protected GeoJsonLayer doInBackground(String... params) {
            try {
                InputStream stream = new URL(params[0]).openStream();

                String line;
                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                stream.close();

                return new GeoJsonLayer(mMap, new JSONObject(result.toString()));
            } catch (IOException e) {
                Log.e(mLogTag, "GeoJSON file could not be read");
            } catch (JSONException e) {
                Log.e(mLogTag, "GeoJSON file could not be converted to a JSONObject");
            }
            return null;
        }


        protected void onPostExecute(GeoJsonLayer layer) {
            if (layer != null) {
                addGeoJsonLayerToMap(layer);
            }
        }
    }

    private void addGeoJsonLayerToMap(GeoJsonLayer layer) {

        layer.addLayerToMap();
        layer.setOnFeatureClickListener(new GeoJsonLayer.GeoJsonOnFeatureClickListener() {

            public void onFeatureClick(Feature feature) {
                Point point = (Point) feature.getGeometry();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point.getGeometryObject())
                        .title(feature.getProperty("name"))
                        .snippet("成人口罩數：" + feature.getProperty("mask_adult") +
                                "\n" + "兒童口罩數：" + feature.getProperty("mask_child") +
                                "\n" + "販賣資息：" + feature.getProperty("note") +
                                "\n" + "聯絡電話：" + feature.getProperty("phone"));
                InfoWindowActivity adapter = new InfoWindowActivity(MapsActivity.this);
                mMap.setInfoWindowAdapter(adapter);
                mMap.addMarker(markerOptions).showInfoWindow();

            }
        });
    }

}
