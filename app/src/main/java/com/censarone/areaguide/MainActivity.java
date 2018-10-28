package com.censarone.areaguide;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.censarone.util.ConstantsUtil;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.map.BaseMarkerBalloon;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SingleLayoutBalloonViewAdapter;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQuery;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TomtomMap tomtomMap;
    private SearchApi searchApi;
    private RoutingApi routingApi;
    private Route route;
    private Icon departureIcon;
    private Icon destinationIcon;

    private LatLng currentPostion;

    private String[] selectedCateogory;

    private int it = 0;

    public static final int STANDARD_RADIUS = 30 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initTomTomServices();
        initUIViews();

        selectedCateogory = getIntent().getExtras().getStringArray(ConstantsUtil.SELECTED_CATEGORY);
        double[] latLng = getIntent().getExtras().getDoubleArray(ConstantsUtil.CURRENT_POSITION);

        currentPostion = new LatLng(latLng[0],latLng[1]);

        searchForPlaces();

    }

    private void searchForPlaces() {

        final List<FuzzySearchResult> resultList = new ArrayList<>();

        for(String s : selectedCateogory)
        {
            FuzzySearchQuery fuzz = FuzzySearchQueryBuilder.create(s)
                    .withPreciseness(new LatLngAcc(currentPostion, STANDARD_RADIUS))
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

                            it++;
                            if(it==(selectedCateogory.length))
                                drawCompleteMap(resultList);

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            it++;
                            if(it==(selectedCateogory.length-1))
                                drawCompleteMap(resultList);
                            Log.e("ERROR", "Error in getting results "+throwable.getMessage());
                        }
                    });

        }



    }

    private void drawCompleteMap(List<FuzzySearchResult> resultList) {

        FuzzySearchResult lastResult = resultList.get(resultList.size()-1);
        LatLng destination = lastResult.getPosition();

        createAndDisplayCustomMarker(destination,lastResult);

        resultList.remove(resultList.size()-1);

        LatLng[] wayPoints = new LatLng[resultList.size()];
        for(int i=0; i<resultList.size(); i++)
        {
            wayPoints[i]=resultList.get(i).getPosition();
            createAndDisplayCustomMarker(wayPoints[i],resultList.get(i));
        }

        drawRouteWithWayPoints(currentPostion, destination, wayPoints);
    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        this.tomtomMap = tomtomMap;
        this.tomtomMap.setMyLocationEnabled(true);
        this.tomtomMap.centerOnMyLocation();
        this.tomtomMap.getMarkerSettings().setMarkersClustering(true);
        this.tomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(createCustomViewAdapter());
    }


    private void initTomTomServices() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
        searchApi = OnlineSearchApi.create(this);
        routingApi = OnlineRoutingApi.create(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initUIViews() {
        departureIcon = Icon.Factory.fromResources(MainActivity.this, R.drawable.ic_map_route_departure);
        destinationIcon = Icon.Factory.fromResources(MainActivity.this, R.drawable.ic_map_route_destination);
    }


    private RouteQuery createRouteQuery(LatLng start, LatLng stop, LatLng[] wayPoints) {
        return RouteQueryBuilder.create(start,stop)
                .withWayPoints(wayPoints)
                .withComputeBestOrder(true)
                .withConsiderTraffic(true).build();
    }


    @SuppressLint("CheckResult")
    private void drawRouteWithWayPoints(LatLng start, LatLng stop, LatLng[] wayPoints) {
        RouteQuery routeQuery = createRouteQuery(start, stop, wayPoints);
        System.out.println("Inside some method");

        Disposable subscribe = routingApi.planRoute(routeQuery).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RouteResponse>() {
                    @Override
                    public void accept(RouteResponse routeResult) throws Exception {
                        for (FullRoute fullRoute : routeResult.getRoutes()) {
                            route = tomtomMap.addRoute(new RouteBuilder(
                                    fullRoute.getCoordinates()).startIcon(departureIcon).endIcon(destinationIcon).isActive(true));
                        }
                        tomtomMap.displayRoutesOverview();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

    }

    private void createAndDisplayCustomMarker(LatLng position,FuzzySearchResult result) {
        String address = result.getAddress().getFreeformAddress();
        String poiName = result.getPoi().getName();

        BaseMarkerBalloon markerBalloonData = new BaseMarkerBalloon();
        markerBalloonData.addProperty(getString(R.string.poi_name_key), poiName);
        markerBalloonData.addProperty(getString(R.string.address_key), address);

        MarkerBuilder markerBuilder = new MarkerBuilder(position)
                .markerBalloon(markerBalloonData)
                .shouldCluster(true);
        tomtomMap.addMarker(markerBuilder);
    }

    private SingleLayoutBalloonViewAdapter createCustomViewAdapter() {
        return new SingleLayoutBalloonViewAdapter(R.layout.marker_custom_balloon) {
            @Override
            public void onBindView(View view, final Marker marker, BaseMarkerBalloon baseMarkerBalloon) {
                Button btnAddWayPoint = view.findViewById(R.id.btn_balloon_waypoint);
                TextView textViewPoiName = view.findViewById(R.id.textview_balloon_poiname);
                TextView textViewPoiAddress = view.findViewById(R.id.textview_balloon_poiaddress);
                textViewPoiName.setText(baseMarkerBalloon.getStringProperty(getApplicationContext().getString(R.string.poi_name_key)));
                textViewPoiAddress.setText(baseMarkerBalloon.getStringProperty(getApplicationContext().getString(R.string.address_key)));
                btnAddWayPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "This functionality will be coming soon", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }


}
