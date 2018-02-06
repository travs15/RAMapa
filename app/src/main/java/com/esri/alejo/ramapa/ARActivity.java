package com.esri.alejo.ramapa;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//import com.esri.android.map.Layer

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.BackgroundGrid;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ARActivity extends AppCompatActivity
        implements SensorEventListener,LocationListener,NavigationView.OnNavigationItemSelectedListener {

    private SensorManager sensorManager;
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private TextView tvCurrentLocation;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private Camera camera;
    private ARCamera arCamera;

    public Location location;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;
    private LocationManager locationManager;
    private View viewAR;

    private MapView vistaMapLittle;
    private ArcGISMap mapaLittle;

    public fragmentMapa fragMap;
    public LocationDisplay locationDisplay;
    public RelativeLayout contentMap;

    final static String TAG = "ARActivity";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private FeatureLayer restaurantes, parqueaderos, hoteles;
    private LayerList layers;
    //obtener posicion
    private Point posicion;
    private Object[] arregloFeatures;

    //AROverlayView arOver = new AROverlayView(getBaseContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud9088059687,none,HC5X0H4AH4YDXH46C082");

        //geoLocalizacion2();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        contentMap = (RelativeLayout) this.findViewById(R.id.layout_miniMap);

        //agregar mapa pequeño
        createLittleMap();

        //ar content------------------------------------
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location);
        arOverlayView = new AROverlayView(this);
        toggle.syncState();

        Intent actAr = getIntent();
        //mapa2 = (mapaCarga) actAr.getSerializableExtra("miMapa");

    }

    public void createLittleMap(){

        fragMap = new fragmentMapa();
        vistaMapLittle = this.findViewById(R.id.mapView);
        mapaLittle = new ArcGISMap(this.getResources().getString(R.string.URL_mapa_alrededores));
        //mapaLittle = mapa2.getMap();
        vistaMapLittle.setMap(mapaLittle);
        vistaMapLittle.setVisibility(View.VISIBLE);
        vistaMapLittle.setBackgroundGrid(new BackgroundGrid(Color.WHITE, Color.WHITE, 0, vistaMapLittle.getBackgroundGrid().getGridSize()));
        vistaMapLittle.setWrapAroundMode(WrapAroundMode.DISABLED);
        locationDisplay = vistaMapLittle.getLocationDisplay();
        locationDisplay.startAsync();
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        vistaMapLittle.setOnTouchListener(new IdentifyFeatureLayerTouchListener(vistaMapLittle.getContext(), vistaMapLittle));
        mapaLittle.addLoadStatusChangedListener(new LoadStatusChangedListener() {
            @Override
            public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                String mapLoadStatus;
                mapLoadStatus = loadStatusChangedEvent.getNewLoadStatus().name();
                switch (mapLoadStatus) {
                    case "LOADED":
                        Toast.makeText(vistaMapLittle.getContext(),"Cargado",Toast.LENGTH_LONG).show();
                        contentMap.setVisibility(View.VISIBLE);
                        LayerList layers = mapaLittle.getOperationalLayers();
                        if(!layers.isEmpty()){
                            parqueaderos = (FeatureLayer) layers.get(0);
                            restaurantes = (FeatureLayer) layers.get(1);
                            hoteles = (FeatureLayer) layers.get(2);
                        }
                        if(mapaLittle.getInitialViewpoint() != null){
                            vistaMapLittle.setViewpoint(mapaLittle.getInitialViewpoint());
                        }

                        break;
                }
            }
        });
    }

    private class IdentifyFeatureLayerTouchListener extends DefaultMapViewOnTouchListener {

        private FeatureLayer layer = null; // reference to the layer to identify features in

        // provide a default constructor
        public IdentifyFeatureLayerTouchListener(Context context, MapView mapView) {
            super(context, mapView);
        }

        // override the onSingleTapConfirmed gesture to handle a single tap on the MapView
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Toast.makeText(vistaMapLittle.getContext(),"presionado",Toast.LENGTH_LONG).show();
            /*try {
                actualizarPunto(location);
                //puntosCapa(parqueaderos);
            } catch (ExecutionException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }*/
            actualizarPunto(location);
            //puntosCapa(parqueaderos);
            hacerConsulta();
            return super.onSingleTapConfirmed(e);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        vistaMapLittle.resume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
    }

    @Override
    public void onPause() {
        releaseCamera();
        vistaMapLittle.pause();
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        super.onPause();
    }

    public void requestCameraPermission() {
        //colocado para hacer la verificacion en tiempo de ejecucion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void initLocationService() {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try   {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled)    {
                // cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null)   {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateLatestLocation();
                }
            }

            if (isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null)  {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();
                }
            }
        } catch (Exception ex)  {
            Log.e(TAG, ex.getMessage());

        }
    }

    //actualizar el punto de la posicion actual para hacer el buffer
    private void actualizarPunto(Location loc){
       posicion = new Point(loc.getLatitude(),loc.getLongitude(),loc.getAltitude(), SpatialReferences.getWgs84());
       Geometry xx = GeometryEngine.project(posicion,mapaLittle.getSpatialReference());
       posicion = (Point)xx;
    }

    public void puntosCapa(FeatureLayer lay) throws ExecutionException, InterruptedException {
        float radioBuffer = 500;
        //FeatureQueryResult query = lay.
        //crear los parametros para el query
        QueryParameters queryParam = new QueryParameters();
        //clausula de busqueda
        queryParam.setWhereClause("1=1");
        //referencia espacial del query
        queryParam.setOutSpatialReference(SpatialReferences.getWgs84());
        //relacion espacial, o la accion que se quiere realizar, en este caso intersectar
        queryParam.setSpatialRelationship(QueryParameters.SpatialRelationship.INTERSECTS);
        //geometria con la que se quiere intersectar o hacer la accion
        queryParam.setGeometry(GeometryEngine.buffer(posicion,radioBuffer));
        Toast.makeText(vistaMapLittle.getContext(),"buffer hecho", Toast.LENGTH_LONG).show();
        //arreglo de features que bota la seleccion de features en el feature layer
        ListenableFuture<FeatureQueryResult> featureQueryResultListenableFuture =
                lay.selectFeaturesAsync(queryParam, FeatureLayer.SelectionMode.NEW);
        //featureQueryResultListenableFuture.get().
        Toast.makeText(vistaMapLittle.getContext(), "obtenido", Toast.LENGTH_SHORT).show();
        arregloFeatures = featureQueryResultListenableFuture.get().getFields().toArray();
        Toast.makeText(vistaMapLittle.getContext(), "feature"+ arregloFeatures.length, Toast.LENGTH_SHORT).show();
    }

    public void hacerConsulta() {
        final ServiceFeatureTable serviceFT = new ServiceFeatureTable(this.getResources().getString(R.string.URL_mapa_alrededores));
        serviceFT.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
        serviceFT.loadAsync();
        //call select features
        serviceFT.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                float radioBuffer = 500;
                QueryParameters queryParam = new QueryParameters();
                //clausula de busqueda
                queryParam.setWhereClause("1=1");
                //referencia espacial del query
                queryParam.setOutSpatialReference(SpatialReferences.getWgs84());
                //relacion espacial, o la accion que se quiere realizar, en este caso intersectar
                queryParam.setSpatialRelationship(QueryParameters.SpatialRelationship.INTERSECTS);
                //geometria con la que se quiere intersectar o hacer la accion
                queryParam.setGeometry(GeometryEngine.buffer(posicion,radioBuffer));
                queryParam.setReturnGeometry(false);
                queryParam.getOrderByFields().add(new QueryParameters.OrderBy("nombre",QueryParameters.SortOrder.DESCENDING));
                // set all outfields
                List<String> outFields = new ArrayList<>();
                outFields.add("*");
                //arreglo de features que bota la seleccion de features en el feature layer
                final ListenableFuture<FeatureQueryResult> featureQResult = serviceFT.populateFromServiceAsync(queryParam,true,outFields);
                featureQResult.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FeatureQueryResult result = featureQResult.get();
                            Iterator<Feature> iterator = result.iterator();
                            Feature feat;

                            ARPoint arPoint = null;
                            ArrayList<ARPoint> itemPuntos = new ArrayList<>();

                            while(iterator.hasNext()){
                                feat = iterator.next();
                                Point punto = (Point) feat.getGeometry();
                                arPoint =
                                        new ARPoint(feat.getFeatureTable().getField("nombre").toString(),
                                                punto.getX(),punto.getY(),punto.getZ());
                                arOverlayView.agregarArPoints(arPoint);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    private void updateLatestLocation() {
        if (arOverlayView !=null && location != null) {
            arOverlayView.updateCurrentLocation(location);
            tvCurrentLocation.setText(String.format("lat: %s \nlon: %s \naltitude: %s \n",
                    location.getLatitude(), location.getLongitude(), location.getAltitude()));
            locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
            //actualizarPunto(location);
        }
    }

    ////location and sensors--------------------------------


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        android.support.v4.app.FragmentManager fragManager = getSupportFragmentManager();

        switch (item.getItemId()){
            case R.id.nav_layers:


        }
        // Handle navigation view item clicks here.
        /*int id = item.getItemId();

         else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        updateLatestLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
