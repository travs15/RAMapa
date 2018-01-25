package com.esri.alejo.ramapa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.karan.churi.PermissionManager.PermissionManager;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    public static String TAG = "app RAmap";
    PermissionManager permissionManager;
    public ActionBar actionBar;
    public static TextView globalBarText;
    public Activity main;
    private BottomNavigationView navigation;
    int colorGeneral, colorPe, colorSel;
    private FragmentManager manager;
    private  Fragment fragMapa, fragInstrucciones, selected;
    private fragmentMapa fragMAPA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main=this;
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud9088059687,none,HC5X0H4AH4YDXH46C082");

        actionBar = this.getSupportActionBar();
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setCustomView(R.layout.action_bar);
        globalBarText = (TextView)this.getSupportActionBar().getCustomView().findViewById(R.id.actionBarText);
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);

        colorGeneral = ContextCompat.getColor(main, R.color.color_general);
        //colorPe = ContextCompat.getColor(main, R.color.colorPrimaryPE);

        globalBarText.setText("RA map");


        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        createFragmentList();

        selected = fragMapa;

        manager = getFragmentManager();
        manager.beginTransaction().add(R.id.fragment_default, fragMapa).hide(fragMapa).commit();
        manager.beginTransaction().add(R.id.fragment_default, fragInstrucciones).hide(fragInstrucciones).commit();


        //navigation.setSelectedItemId(R.id.navigation_ar);

        disableShiftingModeOfBottomNavigationView(navigation);


        getIntent().setAction("Already created");


    }

    private void createFragmentList() {
        fragMapa = new fragmentMapa();
        fragInstrucciones = new FragmentInstructions();

    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            android.support.v4.app.FragmentManager fragManager = getSupportFragmentManager();
            globalBarText.setBackgroundColor(colorGeneral);
            switch (item.getItemId()) {
                case R.id.navigation_mapa:
                    //mTextMessage.setText(R.string.title_home);
                    setTitle("Map");
                    selectFragment(fragMapa);
                    //fragmentMapa fragMAp = new fragmentMapa();
                    //fragManager.beginTransaction().replace(R.id.fragment_default,fragMapa).commit();
                    globalBarText.setText("Mapa");
                    return true;
                case R.id.navigation_ins:
                    //mTextMessage.setTet(R.string.title_dashboard);
                    setTitle("Map");
                    selectFragment(fragInstrucciones);
                    //FragmentInstructions fragIns = new FragmentInstructions();
                    //fragManager.beginTransaction().replace(R.id.fragment_default,fragInstrucciones).commit();
                    globalBarText.setText("Instrucciones");
                    return true;
            }
            return false;
        }
    };


    private void selectFragment(Fragment fragment){
        vaciasPilaFragment();

        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.hide(selected).show(fragment).commit();
        selected = fragment;
    }

    private void vaciasPilaFragment(){
        for(int i = 0; i < manager.getBackStackEntryCount(); ++i) {
            manager.popBackStack();
        }
    }

    // se sobreeescribe el metodo para poder obtener los permisos que se denegaron o permitieron
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
        /*para hacer un arreglo de los permisos concedidos y lo que no
        ArrayList<String> grantedPermissions = permissionManager.getStatus().get(0).granted;
        ArrayList<String> deniedPermissions = permissionManager.getStatus().get(0).denied;

        for(String item:grantedPermissions){
            txtGranted.setText(txtGranted.getText()+"\n"+item);
        }
        for(String item:deniedPermissions){
            txtDenied.setText(txtDenied.getText()+"\n"+item);
        }*/
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fragMAPA.locationDisplay.startAsync();
        } else {
            Toast.makeText(fragMAPA.view.getContext(), "locacion denegada", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("RestrictedApi")
    private void disableShiftingModeOfBottomNavigationView(BottomNavigationView btmNavigationView) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) btmNavigationView.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);

            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Unable to change value of shift mode");
        }
    }
    @Override
    protected void onResume() {
        Log.v("Example", "onResume");

        String action = getIntent().getAction();
        // Prevent endless loop by adding a unique action, don't restart if action is present
        if(action == null || !action.equals("Already created")) {
            Log.v("Example", "Force restart");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Remove the unique action so the next time onResume is called it will restart
        else
            getIntent().setAction(null);

        super.onResume();
    }
}
