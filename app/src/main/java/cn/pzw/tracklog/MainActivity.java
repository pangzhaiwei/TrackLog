package cn.pzw.tracklog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import cn.pzw.tracklog.bean.LocationBean;

public class MainActivity extends AppCompatActivity{

    //permission part
    private String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private final int mRequestCode = 110;
    private List<String> permissionList = new ArrayList<>();
    private AlertDialog mAlertDialog;

    //ui
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    //3d building show?
    private boolean buildingShowing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        initToolbar();

        initDrawerLayout();

        initBaseMap();

        initNavigationView();

    }

    private void initToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void initDrawerLayout(){
        mDrawerLayout = findViewById(R.id.drawer_layout);
    }

    private void initNavigationView(){
        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setCheckedItem(R.id.nav_normal);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map_container);
                if (fragment instanceof MainMapFragment){
                    MainMapFragment baseFragment = (MainMapFragment)fragment;
                    switch (item.getItemId()){
                        case R.id.nav_normal:
                            baseFragment.setModeNormal();
                            break;
                        case R.id.nav_satellite:
                            baseFragment.setModeSatellite();
                            break;
                        case R.id.nav_traffic:
                            if (baseFragment.getTrafficState()){
                                baseFragment.setTraffic(false);
                            }else {
                                baseFragment.setTraffic(true);
                            }
                            break;
                        case R.id.nav_3d_building:
                            if (buildingShowing){
                                baseFragment.setBuilding(false);
                                buildingShowing = false;
                            }else{
                                baseFragment.setBuilding(true);
                                buildingShowing = true;
                            }
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mian_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.toolbar_search_item:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                MainMapFragment mainMapFragment = (MainMapFragment)(MainMapFragment)getSupportFragmentManager().findFragmentById(R.id.map_container);
                LocationBean locationBean = mainMapFragment.getCenterLocation();
                intent.putExtra("CenterPoint", locationBean);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBaseMap() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.map_container, new MainMapFragment());
        transaction.commit();
    }

    private void checkPermission() {
        permissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.size() != 0) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, mRequestCode);
        } else {
            Log.d("MainActivity", "Permission granted");
        }
    }

    private void showAlertDialog() {
        mAlertDialog = new AlertDialog.Builder(MyApplication.getContext())
                .setTitle("warning")
                .setMessage("please granted these permission or this application cannot work")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();
                        Uri packageUri = Uri.parse("package:cn.pzw.tracklog");
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();
                    }
                })
                .create();
        mAlertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean hasPermissionDismiss = false;
        if (requestCode == mRequestCode) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
            if (hasPermissionDismiss) {
                showAlertDialog();
            } else {
                Log.d("MainActivity", "Permission granted");
            }
        }
    }
}
