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


public class CheckinActivity extends Activity {
    private FiveClient fiveClient = null;
    private SharedPreferences sharedPrefs = null;
    private FiveUser user = null;
    private LayoutInflater layoutInflater = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkin);

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPrefs = getSharedPreferences(Constants.FIVE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        fiveClient = Utilities.getFiveClient(sharedPrefs);
        final List<Place> places = Utilities.placesFromJSON(sharedPrefs.getString("places", "[]"), this);

        // Get the user from the passed in intent.
        String userJson = getIntent().getStringExtra("user");
        if (userJson != null)
        {
            user = Utilities.fromJSON(userJson, this);
            if (user == null)
            {
                Log.d("five", "User is null");
            }
        }

        try
        {
            // Add our custom adapter to our place list view
            ListView placeView = (ListView) findViewById(R.id.place_list);
            Log.d("five", "Got the placeView");
            placeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    Place place = places.get(position);
                    fiveClient.checkIn(place, checkInHandler);
               }
            });
            Log.d("five", "Set the onItemClick listener");
            placeView.setAdapter(new PlaceListAdapter(layoutInflater, places));
            Log.d("five", "Set the adapter");

        }
        catch (Exception e)
        {
            Log.d("five", "nearby: " + e.toString());
        }

    }

    private final FiveClient.ResultHandler checkInHandler = new FiveClient.ResultHandler() {
        @Override
        public void handle(Object respObj) {
            Response response = (Response) respObj;
            Utilities.logResponse("post checkin", response);
            if (response.statusCode / 100 == 2)
            {
                Intent intent = new Intent(CheckinActivity.this, HomeActivity.class);
                intent.putExtra("user", Utilities.toJSON(CheckinActivity.this.user, CheckinActivity.this));
                startActivity(intent);
            }
        }
    };
}
