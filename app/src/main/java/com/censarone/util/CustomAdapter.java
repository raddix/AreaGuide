package com.censarone.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.censarone.areaguide.R;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<ItenaryModel> {

    private ArrayList<ItenaryModel> data;
    private Context mContext;

    public CustomAdapter(ArrayList<ItenaryModel> data, Context context) {
        super(context,R.layout.list_adapter, data);
        this.data = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {

        ItenaryModel model = getItem(position);

        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_adapter,parent,false);

        }

        TextView id = convertView.findViewById(R.id.numbers);
        id.setText(model.getId()+".");

        TextView desc = convertView.findViewById(R.id.content);
        desc.setText(model.getDescription());

        return convertView;

    }
}
