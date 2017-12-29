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
    private View view;
    private LinearLayout contentProgress, contentProgressSearch, popup;
    private RelativeLayout contentMap;
    private ImageButton locate;

    private LocationDisplay mLocationDisplay;

    private int requestCode = 2;
    String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION};

    public fragmentMapa() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mapa, container, false);
        // Inflate the layout for this fragment
        initRecursos();
        crearMapa();
        //initLocation();
        //initSearch();

        return view;
    }

    public void crearMapa(){
        vistaMap = (MapView) view.findViewById(R.id.mapView);
        vistaMap.setAttributionTextVisible(false);
        map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.673, -74.051, 12);

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
        vistaMap.setMap(map);
        vistaMap.setWrapAroundMode(WrapAroundMode.DISABLED);

        //vistaMap.setMap(map);
    }

    private  void initRecursos(){
       contentProgress = (LinearLayout) view.findViewById(R.id.linearProgressBar);
       // contentProgressSearch = (LinearLayout) view.findViewById(R.id.linearProgressBarSearch);
        contentMap = (RelativeLayout) view.findViewById(R.id.contentMap);

        locate = (ImageButton) view.findViewById(R.id.myLocationButton);
       // locate.setOnClickListener(this);

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

    private void initLocation(){
        try {
            mLocationDisplay = vistaMap.getLocationDisplay();
            mLocationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
                @Override
                public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                    if (dataSourceStatusChangedEvent.isStarted())
                        return;

                    if (dataSourceStatusChangedEvent.getError() == null)
                        return;
                    boolean permissionCheck1 = ContextCompat.checkSelfPermission(view.getContext(), reqPermissions[0]) ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean permissionCheck2 = ContextCompat.checkSelfPermission(view.getContext(), reqPermissions[1]) ==
                            PackageManager.PERMISSION_GRANTED;

                    if (!(permissionCheck1 && permissionCheck2)) {
                        ActivityCompat.requestPermissions((Activity) view.getContext(), reqPermissions, requestCode);
                    } else {
                        String message = "No has activado la localización de tu celular";
                        Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            });
            mLocationDisplay.startAsync();
        }catch (Exception e){

        }
    }
    private  void initSearch(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.myLocationButton:
                Toast.makeText(view.getContext(), "funciona",Toast.LENGTH_LONG).show();
               // mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                // mLocationDisplay.startAsync();
                break;

        }

    }
}
