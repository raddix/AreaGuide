package com.censarone.areaguide;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.censarone.util.ConstantsUtil;
import com.censarone.util.ItenaryModel;
import com.censarone.util.TimeFactor;
import com.tomtom.online.sdk.common.location.LatLng;
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
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private TomtomMap tomtomMap;
    private RoutingApi routingApi;
    private Route route;
    private Icon departureIcon;
    private Icon destinationIcon;

    private LatLng currentPostion;
    private ArrayList<FuzzySearchResult> mList;

    private String[] selectedCateogory;

    private ArrayList<ItenaryModel> list = new ArrayList<>();
    private ArrayList<ItenaryModel> trueList = new ArrayList<>();

    private Integer timeTaken = 0;
    private Integer count = 0;

    private String totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initTomTomServices();
        initUIViews();


        currentPostion = (LatLng) getIntent().getSerializableExtra(ConstantsUtil.CURRENT_POSITION);
        mList = (ArrayList<FuzzySearchResult>) getIntent().getSerializableExtra(ConstantsUtil.COMPLETE_LIST);
        list = (ArrayList<ItenaryModel>) getIntent().getSerializableExtra(ConstantsUtil.MODEL_LIST);
        selectedCateogory = getIntent().getStringArrayExtra(ConstantsUtil.SELECTED_CATEGORY);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("The size of the list ",trueList.size()+"");
                totalTime = TimeFactor.convertTime(timeTaken);
                Intent intent = new Intent(MainActivity.this,ShowDetailsActivity.class);
                intent.putExtra(ConstantsUtil.MODEL_LIST,trueList);
                intent.putExtra(ConstantsUtil.TIME_TAKEN,totalTime);
                startActivity(intent);
            }
        });


    }


    private void drawCompleteMap(List<FuzzySearchResult> resultList) {
        findTimeOfAllRoute(resultList);

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

    private void findTimeOfAllRoute(List<FuzzySearchResult> resultList) {
        LatLng start = currentPostion;

        for(int i=0; i<resultList.size(); i++)
        {
            calculateRoute(start,resultList.get(i).getPosition());
            start = resultList.get(i).getPosition();
        }
    }

    private void calculateRoute(LatLng start, LatLng stop)
    {
        RouteQuery routeQuery = RouteQueryBuilder.create(start,stop)
                .withComputeBestOrder(true)
                .withConsiderTraffic(true).build();

        Disposable subscribe = routingApi.planRoute(routeQuery).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RouteResponse>() {
                    @Override
                    public void accept(RouteResponse routeResult) throws Exception {
                        for (FullRoute fullRoute : routeResult.getRoutes()) {
                            Log.i("FullRoute",fullRoute.getSummary().toString());
                            ItenaryModel model = list.get(count);
                            Integer totalTime = TimeFactor.getTimeFactor(selectedCateogory[count].toLowerCase())+fullRoute.getSummary().getTravelTimeInSeconds();
                            model.setTimeTaken(TimeFactor.convertTime(totalTime));
                            timeTaken+= totalTime;
                            Log.i("Time Updated","Now the time is "+timeTaken);
                            trueList.add(model);
                            count++;
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        this.tomtomMap = tomtomMap;
        this.tomtomMap.setMyLocationEnabled(true);
        this.tomtomMap.centerOnMyLocation();
        this.tomtomMap.getMarkerSettings().setMarkersClustering(true);
        this.tomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(createCustomViewAdapter());
        drawCompleteMap(mList);
    }


    private void initTomTomServices() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
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
                            Log.i("FullRoute",fullRoute.getSummary().toString());
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
