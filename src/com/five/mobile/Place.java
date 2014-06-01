package com.five.mobile;

import org.json.*;
import android.location.*;
import android.graphics.drawable.Drawable;
import java.net.*;

public class Place
{
    public Place(String id, String name, String description, Location location, Drawable image)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.image = image;
    }

    public final String id;

    public final String name;

    public final String description;

    public final Location location;

    public final Drawable image;

    public static class Location
    {
        public Location(double latitude, double longitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public final double latitude;

        public final double longitude;
    }

}

