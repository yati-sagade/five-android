package com.five.mobile;
import android.graphics.*;
import android.graphics.drawable.*;

public class FiveNotification {
    
    final String message;

    final Drawable image;

    public FiveNotification(String message, Drawable image) {
        this.message = message;
        this.image = image;
    }
}

