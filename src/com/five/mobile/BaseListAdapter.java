package com.five.mobile;

import java.util.*;
import android.util.*;
import android.widget.*;
import android.view.*;


abstract class BaseListAdapter extends BaseAdapter
{
    protected LayoutInflater layoutInflater;

    public BaseListAdapter(LayoutInflater layoutInflater)
    {
        super();
        this.layoutInflater = layoutInflater;
    }

}
