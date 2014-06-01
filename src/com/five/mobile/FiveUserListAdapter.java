package com.five.mobile;

import java.util.*;
import android.util.*;
import android.widget.*;
import android.view.*;

public class FiveUserListAdapter extends BaseListAdapter
{
    private List<FiveUser> users;

    public FiveUserListAdapter(LayoutInflater inflater, List<FiveUser> users)
    {
        super(inflater);
        this.users = users;
    }

    @Override
    public int getCount()
    {
        return users.size();
    }

    @Override
    public Object getItem(int idx)
    {
        return users.get(idx);
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
            view = layoutInflater.inflate(R.layout.user_item, parent, false);
        }

        FiveUser user = (FiveUser) getItem(idx);

        ImageView imgView = (ImageView) view.findViewById(R.id.item_image);
        if (user.avatar!= null)
        {
            try
            {
                imgView.setImageDrawable(user.avatar);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d("five", "Exception here: " + e.toString());
            }
        }

        TextView titleView = (TextView) view.findViewById(R.id.item_title);
        titleView.setText(user.handle);

        TextView descrView = (TextView) view.findViewById(R.id.item_description);
        String descr = user.bio.trim().equals("") ? "No bio" : user.bio;
        descrView.setText(descr);
        
        Log.d("five", "getView(): " + view);
        return view;
    }
}

