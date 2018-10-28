package com.censarone.areaguide;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
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

public class SearchResultLoader extends AsyncTaskLoader<List<FuzzySearchResult>> {

    private List<FuzzySearchResult> resultList = new ArrayList<>();
    public static final int STANDARD_RADIUS = 30 * 1000;

    private LatLng currentLocation;
    private SearchApi searchApi;
    private String[] selectedCateogory;

    public SearchResultLoader(@NonNull Context context,LatLng currentLocation,SearchApi searchApi,String[] selectedCateogory) {
        super(context);
        this.currentLocation=currentLocation;
        this.searchApi = searchApi;
        this.selectedCateogory = selectedCateogory;
    }

    @Nullable
    @Override
    public List<FuzzySearchResult> loadInBackground() {

        {

            final List<FuzzySearchResult> resultList = new ArrayList<>();

            for(String s : selectedCateogory)
            {
                FuzzySearchQuery fuzz = FuzzySearchQueryBuilder.create(s)
                        .withPreciseness(new LatLngAcc(currentLocation, STANDARD_RADIUS))
                        .withTypeAhead(true)
                        .withCategory(true).build();

                searchApi.search(fuzz)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {
                            @Override
                            public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                                if(fuzzySearchResponse.getResults().size()>0)
                                {
                                    FuzzySearchResult result = fuzzySearchResponse.getResults().get(0);
                                    resultList.add(result);
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Log.e("ERROR", "Error in getting results "+throwable.getMessage());
                            }
                        });

            }



        }
        return resultList;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }
}
