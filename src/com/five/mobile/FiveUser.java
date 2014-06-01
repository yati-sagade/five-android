package com.five.mobile;

import java.util.List;
import android.graphics.drawable.Drawable;

public class FiveUser
{
    public final String id;
    public final String firstName;
    public final String lastName;
    public final String handle;
    public final String bio;
    public final boolean openToStrangers;
    public final List<String> interests;
    public final Drawable avatar;

    public FiveUser(String id, String handle, String bio, boolean openToStrangers,
                    List<String> interests, Drawable avatar)
    {
        this.id = id;
        this.firstName = "";
        this.lastName = "";
        this.handle = handle;
        this.bio = bio;
        this.openToStrangers = openToStrangers;
        this.interests = interests;
        this.avatar = avatar;
    }

    public FiveUser(String id, String firstName, String lastName, String handle,
                    String bio, boolean openToStrangers, List<String> interests,
                    Drawable avatar)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.handle = handle;
        this.bio = bio;
        this.openToStrangers = openToStrangers;
        this.interests = interests;
        this.avatar = avatar;
    }

    @Override
    public String toString()
    {
        return "<User " + handle + ">";
    }
}


