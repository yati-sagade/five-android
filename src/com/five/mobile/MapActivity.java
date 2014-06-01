package com.five.mobile;

import android.os.*;
import android.app.*;
import android.view.*;
import android.text.*;
import android.graphics.*;
import android.content.*;
import android.location.*;
import android.widget.*;
import android.util.*;
import com.google.android.gms.maps.*;

public class MapActivity extends Activity {
    private MapView mapView = null;
    private GoogleMap map = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        mapView.onSaveInstanceState(b);
        super.onSaveInstanceState(b);
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    private GoogleMapOptions setMapOptions() {
        GoogleMapOptions options = new GoogleMapOptions();
        return null;
    }

}
