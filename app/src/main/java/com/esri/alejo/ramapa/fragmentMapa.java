package com.esri.alejo.ramapa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;


/**
 * A simple {@link Fragment} subclass.
 */
public class fragmentMapa extends Fragment implements View.OnClickListener {
    private MapView vistaMap;
    public View view;
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
        crearMapa();
        geoLocalizacion();
        return view;
    }

    //todo lo referente a crear la vista del mapa--------------------------------------------------
    public void crearMapa(){
        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.6097100,  -74.0817500, 16);
        vistaMap.setMap(map);
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

    //geolocalizacion
    //obtener punto de localizacion con gps
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

    }
}