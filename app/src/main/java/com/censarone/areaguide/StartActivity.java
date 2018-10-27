package com.censarone.areaguide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private String[] catValues;

    private LinearLayout linearLayout;
    private Button nextButton;

    private View.OnClickListener checkBoxOnClick;

    private Map<Integer,String> categorySelectedMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initFields();
        addCheckBoxesIntoLayout();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categorySelectedMap.size()<2)
                    Toast.makeText(StartActivity.this, "Please select at Least two categories before continuing", Toast.LENGTH_SHORT).show();
                else
                {
                    String[] selectedCategory = getSelectedCategory();
                    Intent intent = new Intent(StartActivity.this,MainActivity.class);
                    intent.putExtra("selectedCategory",selectedCategory);
                    startActivity(intent);
                }
            }
        });


    }

    private String[] getSelectedCategory() {
        String[] selectedCategory = new String[categorySelectedMap.size()];
        int i=0;

        for(Map.Entry<Integer,String> entry : categorySelectedMap.entrySet())
        {
            selectedCategory[i] = entry.getValue();
            i++;
        }

        return selectedCategory;
    }

    private void addCheckBoxesIntoLayout() {
        for(int i = 0; i < catValues.length; i++) {
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setOnClickListener(checkBoxOnClick);
            cb.setId(i);
            cb.setText(catValues[i].toUpperCase());
            linearLayout.addView(cb);
        }
    }

    private void initFields() {
        linearLayout = findViewById(R.id.linear_layout);
        catValues = getResources().getStringArray(R.array.Categories);
        nextButton = findViewById(R.id.next_button);

        checkBoxOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CheckBox) v).isChecked();

                if(isChecked)
                {
                    if(categorySelectedMap.size()>=5)
                    {
                        Toast.makeText(StartActivity.this,"Sorry, but we only have time for five destinations !!",Toast.LENGTH_SHORT).show();
                        ((CheckBox) v).setChecked(false);
                    }
                    else
                        categorySelectedMap.put(v.getId(),catValues[v.getId()]);
                }
                else
                    categorySelectedMap.remove(v.getId());
            }
        };
    }
}
