package com.five.mobile;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.json.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.*;
import org.apache.http.client.methods.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.apache.http.client.*;
import org.apache.http.cookie.*;

import android.os.*;
import android.util.Log;
import android.content.SharedPreferences;
import android.webkit.*;
import android.location.*;
import android.graphics.*;
import android.graphics.drawable.*;

public class FiveClient
{
    
    // The base URL to the Five server
    private URL baseURL = null;
    private SharedPreferences sharedPrefs = null;
    

    public FiveClient(URL baseURL, SharedPreferences sharedPrefs)
    {
        this.baseURL = baseURL;
        this.sharedPrefs = sharedPrefs;
    }
    
    private List<FiveUser> parseNearbyFiveUsers(Response response) throws IOException
    {
        if (response == null)
        {
            return null;
        }
        List<FiveUser> users = new ArrayList<FiveUser>();
        try
        {
            JSONObject content = new JSONObject(response.content);
            JSONArray data = content.getJSONArray("data");
            final int numUsers = data.length();
            for (int i = 0; i < numUsers; ++i)
            {
                JSONObject userJson = data.getJSONObject(i);
                FiveUser user = parseFiveUser(userJson);
                users.add(user);
                Log.d("five", "parseNearbyFiveUsers(): Added user: " + user.toString());
            }
        }
        catch (JSONException e)
        {
            Log.d("five", "JSON decode error: " + e.toString());
            return null;
        }
        return users;
    }

    private FiveUser parseFiveUser(JSONObject userJson) throws IOException
    {
        try
        {
            String id = userJson.getString("id"),
                   handle = userJson.getString("handle"),
                   bio = userJson.getString("bio"),
                   firstName = userJson.getString("first_name"),
                   lastName = userJson.getString("last_name"),
                   avatarURI = userJson.getString("avatar");
            boolean openToStrangers = userJson.getBoolean("meet_new_people");
            JSONArray interestsJson = userJson.getJSONArray("interests");
            List<String> interests = new ArrayList<String>();
            final int numInterests = interestsJson.length();
            for (int j = 0; j < numInterests; ++j)
            {
                interests.add(interestsJson.getString(j));
            }
            Log.d("five", "parseFiveUser(): Most of the user constructed");
            Drawable avatar = null;
            try
            {
                Log.d("five", "parseFiveUser(): About to load the drawable");
                avatar = loadDrawable(new URL(baseURL, avatarURI).toURI());
                Log.d("five", "parseFiveUser(): done");
            }
            catch (URISyntaxException e)
            {
                Log.d("five", "parseFiveUser(): Bad URI " + avatarURI + ": " + e.toString());
                return null;
            }
            catch (Exception e)
            {
                Log.d("five", "parseFiveUser(): " + e.toString());
                return null;
            }
            FiveUser user = new FiveUser(id, firstName, lastName, handle, bio, openToStrangers, interests, avatar);
            Log.d("five", "Returning user: " + user.toString());
            return user;
        }
        catch (JSONException e)
        {
            Log.d("five", "JSON decode error: " + e.toString());
        }
        catch (Exception e)
        {
            Log.d("five", "parseFiveUser: " + e.toString());
        }
        return null;
    }

    public List<Place> parseNearbyPlaces(Response response)
    {
        if (response == null)
        {
            return null;
        }
        List<Place> places = new ArrayList<Place>();
        try
        {
            JSONObject content = new JSONObject(response.content);
            JSONArray data = content.getJSONArray("data");
            final int length = data.length();
            Map<URI, Drawable> uniq = new HashMap<URI, Drawable>();
            for (int i = 0; i < length; ++i)
            {
                JSONObject placeJson = data.getJSONObject(i);
                String id = placeJson.getString("id"),
                       name = placeJson.getString("name"),
                       descr = placeJson.getString("description"),
                       icon = placeJson.getString("icon");
                JSONObject locJson = placeJson.getJSONObject("location");
                double lat = locJson.getDouble("lat"),
                       lon = locJson.getDouble("lon");
                Place.Location loc = new Place.Location(lat, lon);
                Drawable image = null;
                try
                {
                    URI uri = new URI(icon);
                    image = uniq.get(uri);
                    if (image == null)
                    {
                        image = loadDrawable(uri);
                        uniq.put(uri, image);
                    }
                }
                catch (URISyntaxException e)
                {
                    Log.d("five", "FiveClient.parseNearbyPlaces(): " + e.toString());
                }
                catch (Exception e)
                {
                    Log.d("five", "FiveClient.parseNearbyPlaces(): " + e.toString());
                }
                Place place = new Place(id, name, descr, loc, image);
                places.add(place);
            }
        }
        catch (JSONException e)
        {
            Log.d("five", "Error decoding JSON response: " + e.toString());
            return null;
        }
        return places;
    }

    public Response ping(String to, String payload) throws IOException
    {
        try
        {
            Response response = post("/ping/" + to + "/", payload);
            return response;
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "ping: " + e.toString());
        }
        return null;
    }
    
    public void loadDrawable(URI uri, ResultHandler handler) throws IOException
    {
        new NetworkTask().execute(FiveCommand.LOAD_DRAWABLE, uri, handler);
    }

    public Drawable loadDrawable(URI uri) throws IOException
    {
        try
        {
            InputStream in = (InputStream) uri.toURL().getContent();
            Drawable drawable = Drawable.createFromStream(in, uri.toString());
            in.close();
            return drawable;
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Log.d("five", "FiveClient.loadDrawable(): " + e.toString());
        }
        return null;
    }


    public void loadDrawables(List<URI> uris, ResultHandler handler) throws IOException
    {
        new NetworkTask().execute(FiveCommand.LOAD_DRAWABLES, uris, handler);
    }

    public List<Drawable> loadDrawables(List<URI> uris) throws IOException
    {
        // So that we don't fetch the same URL many times
        Map<URI, Drawable> uniq = new HashMap<URI, Drawable>();
        List<Drawable> ret = new ArrayList<Drawable>(uris.size());
        int idx = 0;
        for (URI uri : uris)
        {
            if (!uniq.containsKey(uri))
            {
                // We haven't yet fetched this one, so get and cache it.
                Drawable d = loadDrawable(uri);
                uniq.put(uri, d);
            }
            ret.add(idx++, uniq.get(uri));
        }
        return ret;
    }

    /**
     * Asynchronously checks the current user in to a place.
     *
     * @param place      The checkin target place.
     * @param handler    A ResultHandler whose handle() method will be called
     *                   with a Response object containing the server response
     *                   to this checkin.
     */
    public void checkIn(Place place, ResultHandler handler)
    {
        new NetworkTask().execute(FiveCommand.CHECK_IN, place, handler);
    }

    /**
     * Checks the current user in to a place.
     *
     * @param place    The checkin target place.
     * @return         The server response to the checkin.
     */
    public Response checkIn(Place place) throws IOException
    {
        String file = "/checkin/" + place.id + "/";
        Response response = null;
        try
        {
            response = post(file, "");
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "checkIn: " + e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "checkIn: " + e.toString());
        }
        return response;
    }

    /**
     * Asynchronously gets a list of nearby places and calls a given callback
     * when done.
     *
     * @param location    The location to search for; Must contain a valid
     *                    latitude and longitude.
     * @param handler     A ResultHandler object whose handle() method is
     *                    called with a List<Place>.
     */
    public void getNearbyPlaces(Location location, ResultHandler handler)
    {
        new NetworkTask().execute(FiveCommand.NEARBY_PLACES, location, handler);
    }

    /**
     * Get a list of nearby places.
     *
     * @param location    The location to search for; Must contain a valid
     *                    latitude and longitude.
     * @return            A list of Place objects.
     */
    public List<Place> getNearbyPlaces(Location location) throws IOException
    {
        // The API for nearby places is
        //      /nearby/<lat>,<lon>/
        String nearbyPlacesFile = "/nearby/" + location.getLatitude() + "," + location.getLongitude() + "/";
        Response response = null;
        try
        {
            response = post(nearbyPlacesFile, "");
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "getNearbyPlaces: " + e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "getNearbyPlaces: " + e.toString());
        }
        Utilities.logResponse("Five.getNearbyPlaces()", response);
        if (response == null)
        {
            return null;
        }
        return parseNearbyPlaces(response);
    }

    /**
     * Asynchronously gets a list of nearby users and calls the callback when
     * done.
     *
     * @param handler    A ResultHandler object whose handle() method is called
     *                   with a List<FiveUser>.
     */
    public void getNearbyFiveUsers(ResultHandler handler)
    {
        new NetworkTask().execute(FiveCommand.NEARBY_PEOPLE, handler);
    }

    /**
     * Gets a list of nearby five users
     *
     * @return    A List of FiveUser objects representing the people around
     *            this user.
     */
    public List<FiveUser> getNearbyFiveUsers() throws IOException
    {
        String file = "/who/";
        Response response = null;
        try
        {
            response = get(file);
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "getNearbyFiveUsers: " + e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "getNearbyFiveUsers: " + e.toString());
        }

        Utilities.logResponse("Five.getNearbyFiveUsers()", response);
        if (response == null)
        {
            return null;
        }
        List<FiveUser> users = parseNearbyFiveUsers(response);
        return users;
    }

    /**
     * Fetches asynchronously the details of the currently logged in user,
     * and calls a callback once done.
     *
     * @param handler    A ResultHandler whose handle() method is called with
     *                   a FiveUser object containing details about the current
     *                   user.
     */
    public void getCurrentFiveUser(ResultHandler handler)
    {
        new NetworkTask().execute(FiveCommand.CURRENT_USER, handler);
    }

    /**
     * Gets a local representation of the currently logged in user.
     *
     * @return    A FiveUser object containing details about the current user,
     *            or null if there was an error parsing the server response.
     */
    public FiveUser getCurrentFiveUser() throws IOException
    {
        String file = "/me/";
        Response response = null;
        try
        {
            response = get(file);
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "getCurrentFiveUser: " + e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "getCurrentFiveUser: " + e.toString());
        }
        Utilities.logResponse("Five.getCurrentFiveUser()", response);
        if (response == null)
        {
            Log.d("five", "getCurrentFiveUser(): response was null, so returning null");
            return null;
        }
        JSONObject userJson = null;
        try
        {
            userJson = new JSONObject(response.content);
        }
        catch (JSONException e)
        {
            Log.d("five", "getCurrentFiveUser(): " + e.toString());
            return null;
        }
        catch (Exception e)
        {
            Log.d("five", "getCurrentFiveUser(): " + e.toString());
            return null;
        }
        FiveUser user = parseFiveUser(userJson);
        if (user == null)
        {
            Log.d("five", "getCurrentFiveUser(): user is null from parseFiveUser");
        }
        return user;
    }

    /**
     * Asynchronously logs a user in, calling a callback once done.
     *
     * @param handle      The Five handle of the user.
     * @param password    The password of the user.    
     * @param handler     A ResultHandler whose handle() method will be called
     *                    with a Response object containing the server response
     *                    for the login.
     */
    public void login(String handle, String password, ResultHandler handler) throws IOException
    {
        new NetworkTask().execute(FiveCommand.LOGIN, handle, password, handler);
    }

    /**
     * Logs a user in to Five from this device.
     *
     * @param handle      The Five handle of the user.
     * @param password    The password of the user.    
     * @return            A Response object containing the server response to
     *                    the login.
     */
    public Response login(String handle, String password) throws IOException
    {
        String body = "";
        try
        {
            body = new JSONObject().put("handle", handle).put("password", password).toString();
        }
        catch (JSONException e)
        {
            Log.d("five", "login: " + e.toString());
            return null;
        }

        String loginFile = "/login/";
        try
        {
            Response response = post(loginFile, body);
            return response;
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "login: " + e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "login: " + e.toString());
        }
        return null;
    }

    public FiveNotification getNotification() {
        String notificationFile = "/pull/";
        try { 
            Response response = post(notificationFile, "");
            return getFiveNotification(response);
        }
        catch (MalformedURLException e) {
            Log.d("five", "login: " + e.toString());
        }
        catch (URISyntaxException e) {
            Log.d("five", "login: " + e.toString());
        }
        catch (Exception e) {
            Log.d("five", "login: " + e.toString());
        }
        return null;
    }

    public FiveNotification getFiveNotification(Response response) {
        FiveNotification ret = null;
        try {
            Log.d("five", "The json notif is " + response.content);
            JSONObject content = new JSONObject(response.content);
            JSONArray allNotifications = content.getJSONArray("data");
            for (int idx = 0; idx < allNotifications.length(); ++idx) {
                JSONObject data = allNotifications.getJSONObject(idx);
                final String msg = data.getString("data");
                if (msg.equals("")) {
                    return null;
                }
                final String img = data.getString("image");
                Drawable image = null;
                try {
                    URI uri = new URL(baseURL, img).toURI();
                    image = loadDrawable(uri);
                    ret = new FiveNotification(msg, image);
                } catch (URISyntaxException e) {
                    Log.d("five", "FiveClient.getNotification(): " + e.toString());
                } catch (Exception e) {
                    Log.d("five", "FiveClient.getNotification(): " + e.toString());
                }
            }
        } catch (JSONException e) {
            Log.d("five", "Error decoding JSON response: " + e.toString());
        }
        return ret;
    }

    /**
     * Starts an asynchronous request for the details of the current user.
     *
     * @param handler    A ResultHandler whose handle() method will be called
     *                   with a Response object containing the server response.
     *
     */
    public void userDetails(ResultHandler handler) throws IOException
    {
        new NetworkTask().execute(handler);
    }

    /**
     * Fetches details about the current user.
     *
     * @return    A Response object containing the server response.
     */
    public Response userDetails() throws IOException
    {
        String detailsFile = "/details/";
        try
        {
            Response response = get(detailsFile);
            return response;
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "userDetails: " + e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "userDetails: " + e.toString());
        }
        return null;
    }

    /**
     * Starts an asynchronous logout.
     *
     * @param handler    A ResultHandler whose handle() method will be called
     *                   with a Response object(the server response to the
     *                   logout).
     */
    public void logout(ResultHandler handler) throws IOException
    {
        new NetworkTask().execute(FiveCommand.LOGOUT, handler);
    }

    /**
     * Logs the current user out.
     *
     * @return    The server response for the logout, as returned by post().
     */
    public Response logout() throws IOException
    {
        String logoutFile = "/logout/";
        try
        {
            Response response = post(logoutFile, "");
            if (response == null)
            {
                Log.d("five", "logout: Response was null");
            }
            return response;
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "logout: " + e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("five", "logout: " + e.toString());
        }
        return null;
    }

    /**
     * Reads a String from an InputStream. The Stream is *not* closed after
     * the read.
     */
    private static String toString(InputStream in, String encoding)
    {
        /* This trick I owe to
         * http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
         */
        Scanner s = new Scanner(in, encoding).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Reads an InputStream completely and returns its contents.
     */
    private static String toString(InputStream in)
    {
        /* This trick I owe to
         * http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
         */
        Scanner s = new Scanner(in).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Returns the encoding from the value of a Content-Type header.
     *
     * @param contentType    A string representing a content type, optionally
     *                       with a "charset=..." declaration.
     * @return               The value of the charset declaration within the
     *                       content type string. If no charset declaration is
     *                       found, utf-8 is returned.
     */
    private static String getResponseEncoding(String contentType)
    {
        if (contentType == null)
        {
            return "utf-8";
        }
        String[] values = contentType.split(";");
        final String start = "charset=";
        String ret = "";
        for (String value : values)
        {
            value = value.trim();
            if (value.toLowerCase().startsWith(start))
            {
                ret = value.substring(start.length());
            }
        }
        if ("".equals(ret))
        {
            // Assume the sanest thing we can
            ret = "utf-8";
        }
        return ret;
    }
    
    private String what(Object obj)
    {
        return obj == null ? "null" : "not null";
    }

    /*
     * The following two methods come from
     * http://stackoverflow.com/questions/5802595/using-cookies-across-activities-when-using-httpclient
     */

    /**
     * Stores the cookies from an HTTP response locally.
     *
     * @param httpClient    An AbstractHttpClient which represents our end of
     *                      the request.
     */
    private void storeCookies(AbstractHttpClient httpClient)
    {
        List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie == null)
                {
                    Log.d("five", "FiveClient.storeCookies(): null cookie!!");
                    continue;
                }
                String cookieName = cookie.getName();
                String cookieValue = cookie.getValue();
                if (cookieName == null || cookieValue == null)
                {
                    continue;
                }

                String cookieDomain = cookie.getDomain();
                if (cookieDomain == null)
                {
                    cookieDomain = Constants.FIVE_HOST;
                }

                String cookieString = cookieName + "=" + cookieValue + "; domain=" + cookieDomain;
                android.webkit.CookieManager.getInstance().setCookie(cookieDomain, cookieString); 
            }
            CookieSyncManager.getInstance().sync();
        }
    }

    /**
     * Adds the current session's cookies and other required headers to an HTTP
     * request. This adds the session cookies, some headers like Content-Type
     * (set to application/json), X-Requested-With (set to XMLHttpRequest to
     * simulate AJAX) and CSRFToken (set to the appropriate CSRF cookie sent
     * by the server to prevent CSRF attacks).
     *
     * @param httpClient    An AbstractHttpClient using which the request will
     *                      be made.
     * @param message       An HttpMessage which is a part of this request.
     * @param charset       The charset for this request.
     */
    private void addCookiesAndHeaders(AbstractHttpClient httpClient, HttpMessage message, String charset)
    {
        // Add the mimetype
        message.setHeader(new BasicHeader("Content-Type", "application/json; charset=" + charset));
        // Simulate AJAX
        message.setHeader(new BasicHeader("X-Requested-With", "XMLHttpRequest"));

        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        String domain = Constants.FIVE_HOST;
        String domainCookiesString = cookieManager.getCookie(domain);
        if (domainCookiesString == null)
        {
            return;
        }
        String[] cookies = domainCookiesString.split(";");
        message.setHeader(new BasicHeader("Cookie", domainCookiesString));

        // Find the csrftoken cookie and set the X-CSRFToken header
        for (String cookie : cookies)
        {
            String[] keyValue = cookie.split("=");
            String key = keyValue[0];
            String value = (keyValue.length > 1) ? keyValue[1] : "";
            if ("csrftoken".equals(key.trim()))
            {
                // Set the value of this cookie in the X-CSRFToken header
                message.setHeader(new BasicHeader("X-CSRFToken", value));
            }
            httpClient.getCookieStore().addCookie(new BasicClientCookie(key.trim(), value));
        }

    }

    /**
     * Sends an HTTP GET request to the Five server and returns the response.
     *
     * @param file    A string representing the "file" part of the URL,
     *                relative to the Five root. e.g., to get the details of
     *                the currently logged in user, call with "/me/".
     *
     * @return        A Response object or null if either the Content-Type
     *                or the response body are not set.
     */
    private Response get(String file)
        throws MalformedURLException, IOException, URISyntaxException
    {
        if (!file.endsWith("/"))
        {
            file += "/";
        }
        URL url = new URL(baseURL, file);
        Response ret = null;
        final String charset = "utf-8";
        HttpGet httpGet = new HttpGet(url.toURI());
        DefaultHttpClient httpClient = new DefaultHttpClient();
        addCookiesAndHeaders(httpClient, httpGet, charset);

        HttpResponse response = (HttpResponse) httpClient.execute(httpGet);
        
        ret = buildResponse(response);
        if (ret != null)
        {
            storeCookies(httpClient);
        }
        else
        {
            Log.d("five", "GET: null response");
        }
        return ret;
    }

    /**
     * Sends an HTTP POST request to the five server at a specified endpoint.
     *
     * @param file      The file part of the POST endpoint, relative to the
     *                  five root.
     * @param payload   The POST request body.
     * @return          The response from the server as a Response object or
     *                  null if either the response Content-Type or the
     *                  response body are not set.
     */
    private Response post(String file, String payload)
        throws IOException, MalformedURLException, URISyntaxException
    {
        if (!file.endsWith("/"))
        {
            file += "/";
        }

        final URL url = new URL(baseURL, file);
        final String charset = "utf-8";
        Response ret = null;
        try
        {
            HttpPost httpPost = new HttpPost(url.toURI());
            httpPost.setEntity(new StringEntity(payload, charset));

            DefaultHttpClient httpClient = new DefaultHttpClient();
            addCookiesAndHeaders(httpClient, httpPost, charset);
            
            HttpResponse response = (HttpResponse) httpClient.execute(httpPost);

            ret = buildResponse(response);
            if (ret != null)
            {
                storeCookies(httpClient);
            }
        }
        catch (MalformedURLException e)
        {
            Log.d("five", "POST: " + e.toString());
            throw e;
        }
        catch (IOException e)
        {
            Log.d("five", "POST: " + e.toString());
            throw e;
        }
        return ret;
    }

    /**
     * Builds a com.five.mobile.Response object from a HttpResponse.
     *
     * @param response    A HttpResponse object.
     * @return            A com.five.mobile.Response or null if the response
     *                    body/content-type are not set.
     */
    private Response buildResponse(HttpResponse response) throws IOException
    {
        HttpEntity entity = response.getEntity();

        Response ret = null;
        if (entity != null)
        {
            InputStream in = entity.getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
            {
                in = new GZIPInputStream(in);
            }
            Header contentType = response.getFirstHeader("Content-Type");
            if (contentType != null)
            {
                String encoding = getResponseEncoding(contentType.getValue());
                String message = toString(in, encoding);
                in.close();
                int statusCode = response.getStatusLine().getStatusCode();
                Header[] cookies = response.getHeaders("Set-Cookie");
                Map<String, List<String>> headers = getResponseHeaders(response);
                ret = new Response(statusCode, contentType.getValue(), message, headers);
            }
            else
            {
                Log.d("five", "buildResponse(): null contenttype");
            }
        }
        else
        {
            Log.d("five", "buildResponse(): Null entity");
        }
        return ret;
    }

    /**
     * Get all the response headers from an HttpResponse as a
     * Map<String, List<String>>
     *
     * @param response    An HttpResponse object.
     * @return            A map of header names to the list of header values
     *                    in a Map<String, List<String>>.
     */
    private Map<String, List<String>> getResponseHeaders(HttpResponse response)
    {
        HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
        Header[] headers = response.getAllHeaders();
        for (Header header : headers)
        {
            String name = header.getName(),
                   value = header.getValue();
            if (!ret.containsKey(name))
            {
                ret.put(name, new ArrayList<String>());
            }
            ret.get(name).add(value);
        }
        return ret;
    }

    /**
     * Interface to specify the callback for asynchronous Five client calls.
     */
    public interface ResultHandler {
        public void handle(Object result);
    }

    private class NetworkTask extends AsyncTask<Object, Integer, Object>
    {
        private FiveCommand cmd = FiveCommand.INVALID;
        private ResultHandler handler = null;
        boolean success = true;

        protected Object doInBackground(Object... params)
        {
            cmd = (FiveCommand) params[0];
            Object response = null;
            switch(cmd)
            {
            case LOGIN:
                handler = (ResultHandler) params[3];
                String handle = (String) params[1],
                       password = (String) params[2];
                try
                {
                    response = FiveClient.this.login(handle, password);
                }
                catch (Exception e)
                {
                    Log.d("five", "LoginTask.doInBackground(): " + e.toString());
                }
                return response;

            case LOGOUT:
                handler = (ResultHandler) params[1];
                try
                {
                    response = FiveClient.this.logout();
                }
                catch (Exception e)
                {
                    Log.d("five", "Exception in logout: " + e.toString());
                    return null;
                }
                break;

            case DETAILS:
                handler = (ResultHandler) params[1];
                try
                {
                    response = FiveClient.this.userDetails();
                }
                catch (Exception e)
                {
                    Log.d("five", "Exception in getting details: " + e.toString());
                }
                break;

            case NEARBY_PLACES:
                handler = (ResultHandler) params[2];
                try
                {
                    Location loc = (Location) params[1];
                    response = FiveClient.this.getNearbyPlaces(loc);
                }
                catch (Exception e)
                {
                    Log.d("five", "Exception in getting nearby places: " + e.toString());
                }
                break;

            case CHECK_IN:
                handler = (ResultHandler) params[2];
                try
                {
                    Place place = (Place) params[1];
                    response = FiveClient.this.checkIn(place);
                }
                catch (Exception e)
                {
                    Log.d("five", "Exception in checkin: " + e.toString());
                }
                break;

            case CURRENT_USER:
                handler = (ResultHandler) params[1];
                if (handler == null)
                {
                    Log.d("five", "handler for 7 is Null");
                }
                try
                {
                    FiveUser user = FiveClient.this.getCurrentFiveUser();
                    if (user == null)
                    {
                        Log.d("five", "User is null!!!");
                    }
                    else
                    {
                        Log.d("five", "Got user: " + user.toString() + " with avatar " + what(user.avatar));
                    }
                    response = user;
                }
                catch (Exception e)
                {
                    Log.d("five", "Exception in current user: " + e.toString());
                }
                break;

            case NEARBY_PEOPLE:
                handler = (ResultHandler) params[1];
                try
                {
                    response = FiveClient.this.getNearbyFiveUsers();
                }
                catch (Exception e)
                {
                    Log.d("five", "Exception in getting nearby users: " + e.toString());
                }
                break;

            default:
                Log.d("five", "unknown command: " + cmd);
                success = false;
            }
            return response;
        }

        protected void onPostExecute(Object resp)
        {
            if (success)
            {
                Log.d("five", "About to call the handler for " + cmd);
                handler.handle(resp);
            }
            else
            {
                Log.d("five", "We were unsuccessful");
            }
        }


        private ResultHandler getResultHandlerForCommand(FiveCommand cmd, Object[] params)
        {
            int idx = -1;
            switch (cmd)
            {
            case LOGIN: idx = 3; break;

            case DETAILS:
            case LOGOUT:
            case CURRENT_USER:
            case NEARBY_PEOPLE: idx = 1; break;

            case CHECK_IN:
            case NEARBY_PLACES:
            case LOAD_DRAWABLE:
            case LOAD_DRAWABLES: idx = 2; break;
            }
            ResultHandler ret = idx < 0 ? null : (ResultHandler) params[idx];
            return ret;
        }
    }

    public static enum FiveCommand
    {
        INVALID,
        LOGIN,
        DETAILS,
        LOGOUT,
        CHECK_IN,
        NEARBY_PLACES,
        NEARBY_PEOPLE,
        LOAD_DRAWABLE, 
        LOAD_DRAWABLES,
        CURRENT_USER;

        public static final int size = FiveCommand.values().length;
    }
}

