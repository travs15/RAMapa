package com.esri.alejo.ramapa;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;

import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.BackgroundGrid;

import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;


/**
 * A simple {@link Fragment} subclass.
 */
public class fragmentMapa extends Fragment implements View.OnClickListener {
    private MapView vistaMap;
    ArcGISMap map;
    public View view;
    private LinearLayout contentProgress, contentProgressSearch, popup;
    private RelativeLayout contentMap;
    private ImageButton locate;

    private int requestCode = 2;
    String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION};


    //Uso para localizacion
    public LocationDisplay locationDisplay;


    public fragmentMapa() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mapa, container, false);
        vistaMap = view.findViewById(R.id.mapView);
        locationDisplay = vistaMap.getLocationDisplay();
        // Inflate the layout for this fragment
        initRecursos();
        crearMapa();
        geoLocalizacion();
        return view;
    }

    public void crearMapa(){

        vistaMap = (MapView) view.findViewById(R.id.mapView);
        vistaMap.setAttributionTextVisible(false);
        map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.673, -74.051, 12);
        vistaMap.setMap(map);

        map.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                String mapLoadStatus;
                mapLoadStatus = loadStatusChangedEvent.getNewLoadStatus().name();
                switch (mapLoadStatus) {
                    case "LOADED":
                        contentProgress.setVisibility(View.GONE);
                        contentMap.setVisibility(View.VISIBLE);
                        if(map.getInitialViewpoint() != null)
                            vistaMap.setViewpoint(map.getInitialViewpoint());
                        break;
                }
            }
        });

        vistaMap.setBackgroundGrid(new BackgroundGrid(Color.WHITE, Color.WHITE, 0, vistaMap.getBackgroundGrid().getGridSize()));

        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.6097100,  -74.0817500, 16);

        vistaMap.setWrapAroundMode(WrapAroundMode.DISABLED);

        //vistaMap.setMap(map);
    }

    private  void initRecursos(){
       contentProgress = (LinearLayout) view.findViewById(R.id.linearProgressBar);
       // contentProgressSearch = (LinearLayout) view.findViewById(R.id.linearProgressBarSearch);
        contentMap = (RelativeLayout) view.findViewById(R.id.contentMap);

        locate = (ImageButton) view.findViewById(R.id.myLocationButton);
        locate.setOnClickListener(this);

    }

    @Override
    public void onPause() {
        vistaMap.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        vistaMap.resume();
    }

    
    private void geoLocalizacion() {
        try {
            locationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                if (dataSourceStatusChangedEvent.isStarted())
                    return;

                if (dataSourceStatusChangedEvent.getError() == null)
                    return;
                }
             });
            locationDisplay.startAsync();
            } catch (Exception e) {
                Toast.makeText(view.getContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.myLocationButton:
                Toast.makeText(view.getContext(), "funciona",Toast.LENGTH_LONG).show();
                locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                //locationDisplay.startAsync();
                break;

        }

    }
}