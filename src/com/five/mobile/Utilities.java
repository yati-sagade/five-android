package com.five.mobile;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import android.os.*;
import android.app.*;
import android.widget.*;
import android.view.*;
import android.content.SharedPreferences;
import android.util.Log;
import android.graphics.*;
import android.content.*;
import android.graphics.drawable.*;


public class Utilities
{
    public static String join(String join, String... strings) {
        if (strings == null || strings.length == 0) {
            return "";
        } else if (strings.length == 1) {
            return strings[0];
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(strings[0]);
            for (int i = 1; i < strings.length; i++) {
                sb.append(join).append(strings[i]);
            }
            return sb.toString();
        }
    }

    public static boolean isCsrfTokenCookie(String cookieName)
    {
        return cookieName.equals("csrftoken");
    }

    public static boolean isSessionActive(SharedPreferences sharedPrefs)
    {
        boolean ret = false;
        try
        {
            ret = sharedPrefs.getBoolean(Constants.SESSION_ACTIVE_KEY, false);
        }
        catch (Exception e)
        {
            Log.d("five", "isSessionActive(): " + e.toString());
        }

        Log.d("five", "isSessionActive(): Returned value is " + ret);
        return ret;
    }

    public static void setSessionActive(SharedPreferences sharedPrefs, boolean active)
    {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(Constants.SESSION_ACTIVE_KEY, active);
        editor.commit();
    }

    public static void toggleSessionActive(SharedPreferences sharedPrefs)
    {
        setSessionActive(sharedPrefs, !isSessionActive(sharedPrefs));
    }

    public static void logResponse(String prefix, Response response)
    {
        if (response != null)
        {
            Log.d("five", prefix + ": " + response.toString());
        }
        else
        {
            Log.d("five", prefix + ": " + "null response");
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static String toJSON(FiveUser user, Context context) {
        JSONObject ret = new JSONObject();
        final String avatarFile = "avtr_" + user.id + ".png";
        try
        {
            JSONArray interests = new JSONArray(user.interests);
            ret.put("id", user.id)
               .put("firstName", user.firstName)
               .put("lastName", user.lastName)
               .put("handle", user.handle)
               .put("bio", user.bio)
               .put("openToStrangers", user.openToStrangers)
               .put("avatarFile", avatarFile)
               .put("interests", interests);
        }
        catch (JSONException e)
        {
            Log.d("five", "toJSON(fiveUser): " + e.toString());
        }
        // Write the avatar image to file.
        FileOutputStream out = null;
        try
        {
            Bitmap avatarBmp = drawableToBitmap(user.avatar);
            out = new FileOutputStream(getFiveMediaFile(avatarFile));
            avatarBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        catch (Exception e)
        {
            Log.d("five", "user to json: " + e.toString());
            return null;
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (Throwable ignore) { }
            }
        }
        return ret.toString();
    }

    public static FiveUser fromJSON(String userJsonString, Context context) {
        try
        {
            JSONObject userJSON = new JSONObject(userJsonString);
            String id = userJSON.getString("id"),
                   firstName = userJSON.getString("firstName"),
                   lastName = userJSON.getString("lastName"),
                   handle = userJSON.getString("handle"),
                   bio = userJSON.getString("bio"),
                   avatarFile = userJSON.getString("avatarFile");
            boolean openToStrangers = userJSON.getBoolean("openToStrangers");
            JSONArray interestsJSON = userJSON.getJSONArray("interests");
            List<String> interests = new ArrayList<String>();
            for (int i = 0; i < interestsJSON.length(); ++i)
            {
                interests.add(interestsJSON.getString(i));
            }
            Drawable avatar = null;
            FileInputStream in = null; 
            try
            {
                File file = getFiveMediaFile(avatarFile);
                in = new FileInputStream(file);
                avatar = new BitmapDrawable(context.getResources(), in);
            }
            catch (Exception e)
            {
                Log.d("five", "fromJSON(): " + e.toString());
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (Exception ignore) { }
                }
            }
            return new FiveUser(id, firstName, lastName, handle, bio, openToStrangers, interests, avatar);
        }
        catch (JSONException e)
        {
            Log.d("five", "toJSON(fiveUser): " + e.toString());
        }
        return null;
    }

    public static JSONObject toJSONObject(Place place, Context context) {
        final String imgFile = "place_" + place.id + ".png";
        JSONObject ret = null;
        try {
            ret = new JSONObject()
                 .put("id", place.id)
                 .put("name", place.name)
                 .put("description", place.description)
                 .put("image", imgFile)
                 .put("location",
                         new JSONObject().put("lat", place.location.latitude)
                                         .put("lon", place.location.longitude));
        }
        catch (Exception e) {
            Log.d("five", "Place.toJson(): " + e.toString());
        }
        FileOutputStream out = null;
        try
        {
            Bitmap imageBmp = drawableToBitmap(place.image);
            out = new FileOutputStream(getFiveMediaFile(imgFile));
            imageBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        catch (Exception e)
        {
            Log.d("five", "place to json: " + e.toString());
            return null;
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (Throwable ignore) { }
            }
        }
        return ret;
    }

    public String toJSON(Place place, Context context) {
        return toJSONObject(place, context).toString();
    }

    public static Place placeFromJSON(String placeJsonString, Context context) {
        try {
            JSONObject placeJson = new JSONObject(placeJsonString);
            return fromJSONObject(placeJson, context);
        }
        catch (Exception e) {
            Log.d("five", "place from json: " + e.toString());
        }
        return null;
    }

    public static Place fromJSONObject(JSONObject placeJson, Context context) {
        Place ret = null;
        try {
            String id = placeJson.getString("id"),
                   name = placeJson.getString("name"),
                   description = placeJson.getString("description"),
                   imgPath = placeJson.getString("image");
            JSONObject loc = placeJson.getJSONObject("location");
            double lat = loc.getDouble("lat"),
                   lon = loc.getDouble("lon");
            Drawable image = null;
            FileInputStream in = null; 
            try
            {
                File file = getFiveMediaFile(imgPath);
                in = new FileInputStream(file);
                image = new BitmapDrawable(context.getResources(), in);
                ret = new Place(id, name, description, new Place.Location(lat, lon), image);
            }
            catch (Exception e)
            {
                Log.d("five", "fromJSON(place): " + e.toString());
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (Exception ignore) { }
                }
            }
        } catch (Exception e) {
            Log.d("five", "place from json: " + e.toString());
        }
        return ret;
    }

    public static String toJSON(List<Place> places, Context context) {
        JSONArray ret = new JSONArray();
        for (Place place : places) {
            ret.put(toJSONObject(place, context));
        }
        return ret.toString();
    }

    public static List<Place> placesFromJSON(String placesJson, Context context) {
        try {
            List<Place> ret = new ArrayList<Place>();
            JSONArray arr = new JSONArray(placesJson);
            for (int i = 0; i < arr.length(); ++i) {
                ret.add(fromJSONObject(arr.getJSONObject(i), context));
            }
            return ret;
        } catch (Exception e) {
            Log.d("five", "placesFromJson() " + e.toString());
        }
        return null;
    }

    public static final String FIVE_MEDIA_DIRNAME = "five";

    public static File getFiveMediaDir()
    {
        File file = new File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ),
                FIVE_MEDIA_DIRNAME
        );
        if (!file.mkdirs())
        {
            Log.d("five", "directory not created");
        }
        return file;
    }

    public static File getFiveMediaFile(String fileName)
    {
        return new File(getFiveMediaDir(), fileName);
    }

    public static FiveClient getFiveClient(SharedPreferences sharedPrefs)
    {
        URL baseURL = null;
        FiveClient fiveClient = null;
        try
        {
            baseURL = new URL(Constants.FIVE_PROTO,
                              Constants.FIVE_HOST,
                              Constants.FIVE_PORT, "/");
            fiveClient = new FiveClient(baseURL, sharedPrefs);
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "MalformedURLException: " + e.toString());
        }
        return fiveClient;
    }

    public static void setWaiting(Activity activity)
    {
        setWaiting(activity, "Loading");
    }

    public static void setWaiting(Activity activity, String message)
    {
        activity.setContentView(R.layout.waiting);
        TextView textView = (TextView) activity.findViewById(R.id.waiting_message);
        textView.setText(message);
    }
}
