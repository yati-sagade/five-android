package com.five.mobile;

import android.os.*;
import android.app.*;
import android.view.*;
import android.text.*;
import android.text.style.*;
import android.graphics.*;
import android.content.*;
import android.content.res.*;
import android.location.*;
import android.widget.*;
import android.util.*;

public class TypefaceSpan extends MetricAffectingSpan
{
    private static LruCache<String, Typeface> cache = new LruCache<String, Typeface>(12);
    private Typeface typeface;

    public TypefaceSpan(Context context, String typefaceName)
    {
        typeface = cache.get(typefaceName);
        if (typeface == null)
        {
            AssetManager assets = context.getApplicationContext().getAssets();
            typeface = Typeface.createFromAsset(
                           assets,
                           String.format("fonts/%s", typefaceName)
                       );
            cache.put(typefaceName, typeface);
        }
    }

    @Override
    public void updateMeasureState(TextPaint tp)
    {
        tp.setTypeface(typeface);
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp)
    {
        updateMeasureState(tp);
    }
}
