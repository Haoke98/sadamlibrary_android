package com.sadam.sadamlibarary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MyListView extends ListView {
    public MyListView(Context context) {
        super(context);
        this.setAdapter(new ArrayAdapter<Integer>(context,R.layout.my_list_view_item,new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12}));
//        this.setS
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdapter(new ArrayAdapter<Integer>(context,R.layout.my_list_view_item,new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12}));
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
