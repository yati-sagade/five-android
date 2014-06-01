package com.five.mobile;

import java.util.*;
import android.util.*;
import android.widget.*;
import android.view.*;


public class PlaceListAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Place> places;

    public PlaceListAdapter(LayoutInflater layoutInflater, List<Place> places)
    {
        super();
        this.layoutInflater = layoutInflater;
        this.places = places;
    }

    @Override
    public int getCount()
    {
        return places.size();
    }

    @Override
    public Object getItem(int idx)
    {
        return places.get(idx);
    }

    @Override
    public long getItemId(int idx)
    {
        return idx;
    }

    @Override
    public View getView(int idx, View view, ViewGroup parent)
    {
        // Inflate only if needed
        if (view == null)
        {
            view = layoutInflater.inflate(R.layout.place_item, parent, false);
        }

        Place place = (Place) getItem(idx);

        ImageView imgView = (ImageView) view.findViewById(R.id.item_image);
        if (place.image != null)
        {
            try
            {
                imgView.setImageDrawable(place.image);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d("five", "Exception setting place image: " + e.toString());
            }
        }

        TextView titleView = (TextView) view.findViewById(R.id.item_title);
        titleView.setText(place.name);

        TextView descrView = (TextView) view.findViewById(R.id.item_description);
        String descr = place.description.trim().equals("") ? "No description" : place.description;
        descrView.setText(descr);
        
        Log.d("five", "getView(): " + view);
        return view;
    }
}
