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
import android.content.*;
import android.location.*;
import android.widget.*;
import android.util.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.*;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent _intent) {
        if (_intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, FiveService.class);
            PendingIntent pintent = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, 5000, pintent);
        }
    }
}


