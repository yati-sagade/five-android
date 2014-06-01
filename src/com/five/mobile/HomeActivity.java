package com.five.mobile;

import java.net.*;
import java.util.*;
import android.os.*;
import android.app.*;
import android.view.*;
import android.content.*;
import android.location.*;
import android.widget.*;
import android.util.Log;
import android.support.v4.app.*;


public class HomeActivity extends Activity
{
    private FiveUser user = null;
    private FiveClient fiveClient = null;
    private SharedPreferences sharedPrefs = null;
    private LayoutInflater layoutInflater = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Utilities.setWaiting(this);

        // Enable navigating to the main activity.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPrefs = getSharedPreferences(Constants.FIVE_SHARED_PREFS_NAME,
                                           Context.MODE_PRIVATE);

        fiveClient = Utilities.getFiveClient(sharedPrefs);
        
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
        populateNearbyUsers();
    }

    private void populateNearbyUsers()
    {
        fiveClient.getNearbyFiveUsers(nearbyPeopleHandler);
    }
    
    private FiveClient.ResultHandler nearbyPeopleHandler = new FiveClient.ResultHandler() {
        @Override
        public void handle(Object result)
        {
            setContentView(R.layout.home);
            List<FiveUser> users = (List<FiveUser>) result;
            for (FiveUser user : users)
            {
                Log.d("five", user.toString());
            }
            ListView userListView = (ListView) findViewById(R.id.user_list);
            userListView.setAdapter(new FiveUserListAdapter(layoutInflater, users));
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            String name = NavUtils.getParentActivityName(this);
            Log.d("five", "Parent activity name: " + name);

            if (upIntent == null)
            {
                Log.d("five", "upIntent was null");
                upIntent = getParentActivityIntent();
                if (upIntent == null)
                {
                    Log.d("five", "upIntent was null, again");
                    return true;
                }
            }
            if (NavUtils.shouldUpRecreateTask(this, upIntent))
            {
                // This activity is not a part of this app's task - it may have
                // been started by some other app. So synthesize a back stack.
                android.app.TaskStackBuilder.create(this)
                                // Add all of this activity's parents to the
                                // back stack.
                                .addNextIntentWithParentStack(upIntent)
                                // Start the closest parent.
                                .startActivities();
            }
            else
            {
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
