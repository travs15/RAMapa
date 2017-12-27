package com.esri.alejo.ramapa;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    PermissionManager permissionManager;
        TextView txtGranted,txtDenied;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            android.support.v4.app.FragmentManager fragManager = getSupportFragmentManager();
            switch (item.getItemId()) {
                case R.id.navigation_mapa:
                    //mTextMessage.setText(R.string.title_home);
                    setTitle("Map");
                    fragmentMapa fragMAp = new fragmentMapa();
                    fragManager.beginTransaction().replace(R.id.fragent_default,fragMAp).commit();
                    return true;
                case R.id.navigation_ar:
                    //mTextMessage.setText(R.string.title_dashboard);
                    setTitle("Map");
                    FragmentAR fragAR = new FragmentAR();
                    fragManager.beginTransaction().replace(R.id.fragent_default,fragAR).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //referenencia a los textos para ver los permisos
        txtGranted = (TextView) findViewById(R.id.txtGranted);
        txtDenied = (TextView) findViewById(R.id.txtDenied);
        //uso del permission manager, de la libreria descargada
        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    // se sobreeescribe el metodo para poder obtener los permisos que se denegaron o permitieron
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);

        ArrayList<String> grantedPermissions = permissionManager.getStatus().get(0).granted;
        ArrayList<String> deniedPermissions = permissionManager.getStatus().get(0).denied;

        for(String item:grantedPermissions){
            txtGranted.setText(txtGranted.getText()+"\n"+item);
        }
        for(String item:deniedPermissions){
            txtDenied.setText(txtDenied.getText()+"\n"+item);
        }
    }
}
