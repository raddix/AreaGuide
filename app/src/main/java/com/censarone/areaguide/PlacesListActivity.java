package com.censarone.areaguide;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.censarone.util.ConstantsUtil;
import com.censarone.util.ItenaryModel;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQuery;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class PlacesListActivity extends AppCompatActivity {

    private SearchApi searchApi;

    private List<FuzzySearchResult> selectedPlace = new ArrayList<>();
    private List<FuzzySearchResult> currentList;
    private List<String> currentPlaces = new ArrayList<>();

    private ArrayList<ItenaryModel> modelList = new ArrayList<>();

    private ListView listView;

    private LatLng currentPostion;

    private String[] selectedCateogory;

    public static final int STANDARD_RADIUS = 30 * 1000;

    private Integer totalCount = 0;

    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        listView = findViewById(R.id.places_list_view);
        titleTextView = findViewById(R.id.title_places);

        selectedCateogory = getIntent().getExtras().getStringArray(ConstantsUtil.SELECTED_CATEGORY);
        double[] latLng = getIntent().getExtras().getDoubleArray(ConstantsUtil.CURRENT_POSITION);
        currentPostion = new LatLng(latLng[0],latLng[1]);

        searchApi = OnlineSearchApi.create(this);

        titleTextView.setText(selectedCateogory[0].toUpperCase());
        searchPlaces(selectedCateogory[0]);
    }

    private void searchPlaces(String input) {
        totalCount++;
        final FuzzySearchQuery fuzz = FuzzySearchQueryBuilder.create(input)
                .withPreciseness(new LatLngAcc(currentPostion, STANDARD_RADIUS))
                .withTypeAhead(true)
                .withCategory(true).build();

        searchApi.search(fuzz)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {
                    @Override
                    public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                        if(fuzzySearchResponse.getResults().isEmpty())
                            Toast.makeText(PlacesListActivity.this,"Sorry no result found",Toast.LENGTH_SHORT).show();
                        else
                        {
                            currentList = fuzzySearchResponse.getResults();
                            for(FuzzySearchResult result : currentList)
                            {
                                currentPlaces.add(result.getPoi().getName());
                            }


                            showCurrentList();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(PlacesListActivity.this, "We got an error", Toast.LENGTH_SHORT).show();
                        Log.e("ERROR", "Error in getting results "+throwable.getMessage());
                    }
                });
    }

    private void showCurrentList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, currentPlaces);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FuzzySearchResult result = currentList.get(i);
                ItenaryModel model = new ItenaryModel(totalCount,result.getPoi().getName());
                modelList.add(model);

                selectedPlace.add(result);
                if(totalCount>=selectedCateogory.length) {
                    ArrayList<FuzzySearchResult> completeList = new ArrayList<>();
                    completeList.addAll(selectedPlace);
                    Toast.makeText(PlacesListActivity.this, "First Step Completed !! YAY", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PlacesListActivity.this,MainActivity.class);
                    intent.putExtra(ConstantsUtil.CURRENT_POSITION,currentPostion);
                    intent.putExtra("test",completeList);
                    intent.putExtra("an",modelList);
                    startActivity(intent);

                    //intent.putExtra("test",selectedPlace);
                }
                else
                {
                    currentPlaces.clear();
                    titleTextView.setText(selectedCateogory[totalCount].toUpperCase());
                    searchPlaces(selectedCateogory[totalCount]);
                }
            }
        });
    }
}
