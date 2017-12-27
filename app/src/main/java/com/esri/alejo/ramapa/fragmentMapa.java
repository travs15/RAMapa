package com.esri.alejo.ramapa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;


/**
 * A simple {@link Fragment} subclass.
 */
public class fragmentMapa extends Fragment {
    private MapView vistaMap;
    private View view;

    public fragmentMapa() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mapa, container, false);
        // Inflate the layout for this fragment
        crearMapa();
        return view;
    }

    public void crearMapa(){
        vistaMap = view.findViewById(R.id.mapView);
        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);
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
}
