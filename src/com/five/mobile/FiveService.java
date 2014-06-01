package com.five.mobile;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;
import android.os.*;
import android.app.*;
import android.net.*;
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

public class FiveService extends IntentService {
    private PowerManager.WakeLock wakeLock = null;
    static final String tag = "five";
    private SharedPreferences sharedPrefs = null;
    private FiveClient fiveClient = null;
    private NotificationManager manager = null;

    public FiveService() {
        super("FiveService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("five", "Service::onStartCommand() called");
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sharedPrefs = getSharedPreferences(Constants.FIVE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        fiveClient = Utilities.getFiveClient(sharedPrefs);
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("five", "Service::onHandleIntent() called");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
        wakeLock.acquire();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getBackgroundDataSetting()) {
            stopSelf();
            return;
        }
        
        new PollTask().execute();
    }

    private class PollTask extends AsyncTask<Void, Void, FiveNotification> {
        @Override
        protected FiveNotification doInBackground(Void... params) {
            FiveNotification notification = fiveClient.getNotification();
            return notification;
        }

        @Override
        protected void onPostExecute(FiveNotification notification) {
            if (notification != null) {
                doNotify("Five", notification.message, notification.image);
            }
        }
    }

    private void doNotify(String title, String content, Drawable largeIcon)
    {
        Notification.Builder builder = new Notification.Builder(this)
                                       .setSmallIcon(R.drawable.logo)
                                       .setLargeIcon(Utilities.drawableToBitmap(largeIcon))
                                       .setContentTitle(title)
                                       .setContentText(content);
        manager.notify(0, builder.build());
    }
}
