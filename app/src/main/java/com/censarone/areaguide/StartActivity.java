package com.censarone.areaguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    private String[] catValues;
    private boolean[] checkedCat;

    private ArrayList<Integer> userSelectCat=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        showDialogueBox();
    }

    private void showDialogueBox() {
        catValues=getResources().getStringArray(R.array.Categories);
        checkedCat=new boolean[catValues.length];
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(StartActivity.this);
        mBuilder.setTitle("Choose Category");
        mBuilder.setMultiChoiceItems(catValues, checkedCat, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    if (!userSelectCat.contains(which)) {
                        userSelectCat.add(which);

                    } else {
                        userSelectCat.remove(which);
                    }
                }
            }
        }).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String item = "";
                for (int i = 0; i < userSelectCat.size(); i++) {
                    if (i != userSelectCat.size() - 1)
                        item = item + catValues[userSelectCat.get(i)] + ",";
                    else
                        item = item + catValues[userSelectCat.get(i)];

                }

                Intent intent = new Intent(StartActivity.this,MainActivity.class);
                intent.putExtra("category",userSelectCat);
                startActivity(intent);


            }
        }).setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userSelectCat.clear();
                for(int i=0;i<checkedCat.length;i++)
                {
                    checkedCat[i]=false;

                }
            }
        });
        AlertDialog mDialog=mBuilder.create();
        mDialog.show();
    }
}
