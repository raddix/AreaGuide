package com.censarone.areaguide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.censarone.util.ConstantsUtil;
import com.censarone.util.CustomAdapter;
import com.censarone.util.ItenaryModel;

import java.util.ArrayList;

public class ShowDetailsActivity extends AppCompatActivity {

    private ListView listView;
    private TextView totalTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_details);

        listView = findViewById(R.id.list_item);
        totalTimeTextView = findViewById(R.id.total_time_text_view);

        ArrayList<ItenaryModel> list = (ArrayList<ItenaryModel>) getIntent().getSerializableExtra(ConstantsUtil.MODEL_LIST);

        CustomAdapter ad = new CustomAdapter(list,ShowDetailsActivity.this);
        listView.setAdapter(ad);
    }
}
