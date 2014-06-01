package com.five.mobile;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;
import android.os.*;
import android.app.*;
import android.view.*;
import android.text.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.*;
import android.location.*;
import android.widget.*;
import android.util.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.*;


public class MainActivity extends Activity
                          implements GooglePlayServicesClient.ConnectionCallbacks,
                                     GooglePlayServicesClient.OnConnectionFailedListener
{
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final String SESSIONID_KEY = "sessionid";
    private boolean listeningForLocationUpdates = false;
    private FiveClient fiveClient = null;
    private SharedPreferences sharedPrefs = null;
    private FiveUser user = null;
    private Random rand = new Random();
    private AlarmManager alarmMgr = null;

    // This is our endpoint for querying the last user location.
    private LocationClient locationClient = null;

    private LayoutInflater layoutInflater = null;

    private MapView mapView = null;
    private GoogleMap map = null;
    private List<Place> places = null;

    int mapX = 0, mapY = 0;

    /**
     * Called by the Location services when a request to connect a client
     * finishes successfully.
     */
    @Override
    public void onConnected(Bundle bundle)
    {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        // Get the current location
        Location location = locationClient.getLastLocation();

        // Zoom the map in to our area.
        double lat = location.getLatitude(),
               lon = location.getLongitude();
        MapsInitializer.initialize(this);
        LatLng latLng = new LatLng(lat, lon);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 14.0F);
        map.animateCamera(update);
        map.addMarker(new MarkerOptions().position(latLng));

        drawHeatMap(latLng);
        // Get nearby places and show them 
        fiveClient.getNearbyPlaces(location, nearbyPlacesHandler);
    }

    private void addCircle(double lat, double lon, int radiusInMeters, int color)
    {
        LatLng pos = new LatLng(lat, lon);
        map.addCircle(new CircleOptions()
                         .center(pos)
                         .radius(radiusInMeters)
                         .strokeColor(color)
                         .fillColor(color));
    }

    private void addCircle(double lat, double lon)
    {
        addCircle(lat, lon, 10, Color.BLUE);
    }

    /** 
     * Called by the Location services when the connection to the client
     * drops because of an error
     */
    @Override
    public void onDisconnected()
    {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();

    }

    /**
     * Called when there is an error trying to connect to the client.
     * Some errors can be resolved by the Play Services.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // Some errors can be resolved by the Play Services API
        if (connectionResult.hasResolution())
        {
            try
            {
                // Start an activity to try to resolve
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST
                );
            }
            catch (IntentSender.SendIntentException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }


    // Error dialog fragment
    public static class ErrorDialogFragment extends DialogFragment
    {
        private Dialog dialog;

        public ErrorDialogFragment()
        {
            super();
            dialog = null;
        }

        public void setDialog(Dialog dialog)
        {
            this.dialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            return dialog;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
        case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            switch(resultCode)
            {
            case Activity.RESULT_OK:
                // Try connecting again
                break;
            }
            break;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        locationClient = new LocationClient(this, this, this);
        android.webkit.CookieSyncManager.createInstance(this);
        sharedPrefs = getSharedPreferences(Constants.FIVE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (sharedPrefs == null)
        {
            Log.d("five", "onCreate(): sharedPrefs is null!");
        }
        fiveClient = Utilities.getFiveClient(sharedPrefs);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, FiveService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, 5000, pintent);

        updateActionBar();
    }

    private void drawHeatMap(LatLng me) {
        int mapW = mapView.getWidth();
        int mapH = mapView.getHeight();
        Log.d("five", "In drawHeatMap(): " + mapX + ", " + mapY + " - " + mapW + ", " + mapH);
        Point p1 = new Point(mapX, mapY);
        Point p2 = new Point(mapX + mapW, mapY + mapH); 
        Projection proj = map.getProjection();
        double lat = me.latitude,
               lon = me.longitude;

        addCircle(lat + 0.0009, lon);
        addCircle(lat + 0.001, lon - 0.005);
        addCircle(lat + 0.0004, lon + 0.0004);
        addCircle(lat - 0.003, lon + 0.004);
        addCircle(lat + 0.0008, lon + 0.0007);
        addCircle(lat + 0.0009, lon + 0.0004);
        addCircle(lat + 0.001, lon + 0.0004);

        /* map.addMarker(new MarkerOptions().position(l1)); */
        /* map.addMarker(new MarkerOptions().position(l2)); */
        /* map.addMarker(new MarkerOptions().position(l3)); */
        /* map.addMarker(new MarkerOptions().position(l4)); */
        /* map.addMarker(new MarkerOptions().position(l5)); */
        /* map.addMarker(new MarkerOptions().position(l6)); */
        /* map.addMarker(new MarkerOptions().position(l7)); */

        /* for (int i = 0; i < 100; ++i) { */
        /*     int randX = Utilities.randInt(0, mapW), */
        /*         randY = Utilities.randInt(0, mapH); */
        /*     LatLng l1 = proj.fromScreenLocation(new Point(randX, randY)); */
        /* } */
        /* LatLng l1 = proj.fromScreenLocation(p1), */
        /*        l2 = proj.fromScreenLocation(p2); */
        /* long lat1 = (long) (l1.latitude * 10e6), */
        /*      lon1 = (long) (l1.longitude * 10e6), */
        /*      lat2 = (long) (l2.latitude * 10e6), */
        /*      lon2 = (long) (l2.longitude * 10e6); */
        /*  */
        /* for (int i = 0; i < 500; ++i) { */
        /*     double randLat = (double)Utilities.randLong(lat1, lon1); */
        /*     double randLon = (double)Utilities.randLong(lat2, lon2); */
        /*     Log.d("five", "" + lat1 + ", " + lon1 + " - " + lat2 + ", " + lon2 + " - " + randLat + ", " + randLon); */
        /*     map.addMarker(new MarkerOptions().position(new LatLng(randLat / 10e6, randLon / 10e6))); */
        /* } */
    }


    private void updateActionBar()
    {
        SpannableString s = new SpannableString(getString(R.string.app_name));
        ActionBar actionBar = getActionBar();
        s.setSpan(new TypefaceSpan(this, "GoodDog.otf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setDisplayShowHomeEnabled(false); 
        actionBar.setTitle(s);
    }

    @Override
    public void onPause()
    {
        mapView.onPause();
        android.webkit.CookieSyncManager.getInstance().sync();
        super.onPause();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (!isUserLoggedIn())
        {
            // Start the login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else
        {
            startPopulating();
        }
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        if (hasFocus) {
            int[] pos = new int[2];
            mapView.getLocationOnScreen(pos);
            mapX = pos[0];
            mapY = pos[1];
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy()
    {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle b)
    {
        super.onSaveInstanceState(b);
        mapView.onSaveInstanceState(b);
    }

    @Override
    public void onLowMemory()
    {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    private void doNotify(String title, String content)
    {
        Notification.Builder builder = new Notification.Builder(this)
                                       .setSmallIcon(R.drawable.logo)
                                       .setContentTitle(title)
                                       .setContentText(content);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    @Override
    public void onStop()
    {
        locationClient.disconnect();
        super.onStop();
    }

    private void startPopulating()
    {
        // All the data fetching happens async. This is the flow:
        // First get the current user.
        // Once we have the user, connect the location client.
        // Once the location client is connected, our onConnected() is called,
        // where we get the nearby places.
        // Once we have the nearby places, we populate the listview.
        fiveClient.getCurrentFiveUser(currentUserHandler);
    }

    public void refreshNearbyList()
    {
        // Utilities.setWaiting(this);
        startPopulating();
    }

    public void checkIn(Place place)
    {
    }

    /**
     * Click handler
     */
    public void checkIn(View view)
    {
        final List<Place> places = this.places;
        String placesJson = Utilities.toJSON(places, this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("places", placesJson);
        editor.commit();

        Intent intent = new Intent(this, CheckinActivity.class);
        intent.putExtra("user", Utilities.toJSON(user,this));
        startActivity(intent);
    }

    private String getLocationRepr(Location location)
    {
        double lat = location.getLatitude(),
               lon = location.getLongitude();
        float accuracy = (float) location.getAccuracy();
        return "" + lat + "," + lon + "(" + accuracy + "m)";
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), "Location Updates");
        }
    }

    /**
     * Add the action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void openSearch()
    {

    }

    private void openSettings()
    {

    }

    private void logout()
    {
        try
        {
            fiveClient.logout(logoutHandler);
        }
        catch (IOException e)
        {
            Log.d("five", "MainActivity.logout(): " + e.toString());
        }
    }

    public void clearCookies()
    {
        android.webkit.CookieManager.getInstance().removeAllCookie();
        android.webkit.CookieSyncManager.getInstance().sync();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_search:
            openSearch();
            return true;
        case R.id.action_refresh:
            refreshNearbyList();
            return true;
        case R.id.action_settings:
            openSettings();
            return true;
        case R.id.action_logout:
            logout();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Check if the user is logged in.
     */
    private boolean isUserLoggedIn()
    {
        if (sharedPrefs == null)
        {
            Log.d("five", "Ahem, the sharedprefs is null");
        }
        return Utilities.isSessionActive(sharedPrefs);
    }

    private final FiveClient.ResultHandler checkInHandler = new FiveClient.ResultHandler() {
        @Override
        public void handle(Object respObj) {
            Response response = (Response) respObj;
            Utilities.logResponse("post checkin", response);
            if (response.statusCode / 100 == 2)
            {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("user", Utilities.toJSON(MainActivity.this.user, MainActivity.this));
                startActivity(intent);
            }
        }
    };

    private final FiveClient.ResultHandler logoutHandler = new FiveClient.ResultHandler() {
        @Override
        public void handle(Object respObj) {
            Response response = (Response) respObj;
            if (response.statusCode / 100 == 2)
            {
                clearCookies();
                Utilities.setSessionActive(sharedPrefs, false);
                onStart();
            }
            else
            {
                Log.d("five", "some error happened in logout");
            }
        }
    };

    private final FiveClient.ResultHandler detailsHandler = new FiveClient.ResultHandler() {
        @Override
        public void handle(Object respObj) {
            Response response = (Response) respObj;
            if (response.statusCode / 100 == 2)
            {
                try
                {
                    JSONObject json = new JSONObject(response.content);
                }
                catch (org.json.JSONException e)
                {
                    Log.d("five", "details: " + e.toString());
                }
            }
            else
            {
                Log.d("five", "some error happened in getting details");
            }
        }
    };

    private FiveClient.ResultHandler nearbyPlacesHandler = new FiveClient.ResultHandler() {
        @Override
        public void handle(Object respObj) {
            Log.d("five", "Inside nearby handler");
            places = (List<Place>) respObj;
            Log.d("five", "Got the list of places");
            // updateView(places);
        }
    };

    /* private void updateView(final List<Place> places) */
    /* { */
    /*     setContentView(R.layout.main); */

    /*     // Construct and show the place list */
    /*     try */
    /*     { */
    /*         // Add our custom adapter to our place list view */
    /*         ListView placeView = (ListView) findViewById(R.id.place_list); */
    /*         Log.d("five", "Got the placeView"); */
    /*         placeView.setOnItemClickListener(new AdapterView.OnItemClickListener() { */
    /*             @Override */
    /*             public void onItemClick(AdapterView<?> parent, View view, int position, long id) */
    /*             { */
    /*                 Place place = places.get(position); */
    /*                 fiveClient.checkIn(place, checkInHandler); */
    /*            } */
    /*         }); */
    /*         Log.d("five", "Set the onItemClick listener"); */
    /*         placeView.setAdapter(new PlaceListAdapter(layoutInflater, places)); */
    /*         Log.d("five", "Set the adapter"); */

    /*     } */
    /*     catch (Exception e) */
    /*     { */
    /*         Log.d("five", "nearby: " + e.toString()); */
    /*     } */

    /* } */

    private FiveClient.ResultHandler currentUserHandler = new FiveClient.ResultHandler() {
        @Override
        public void handle(Object user) {
            if (user != null)
            {
                MainActivity.this.user = (FiveUser) user;
                Log.d("five", "in currentUserHandler: " + user.toString());
                locationClient.connect();
            }
        }

    };

    public void showMap(View view)
    {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}

