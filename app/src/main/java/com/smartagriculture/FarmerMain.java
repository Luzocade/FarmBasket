package com.smartagriculture;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FarmerMain extends AppCompatActivity {
    //private static final String TAG = "AppTest>>>>";
    private static int selectedTab = R.id.navigation_dashboard;
    BottomNavigationView navigation;

    Fragment selectedFragment;
    UiEnhancement uiEnhancement;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                /*case R.id.navigation_chat:
                    selectedTab = R.id.navigation_chat;
                    return true;*/
                case R.id.navigation_products:
                    selectedFragment = new FarmerProducts();
                    getSupportFragmentManager().beginTransaction().replace(R.id.farmerMainContent,new FarmerProducts()).commit();
                    selectedTab = R.id.navigation_products;
                    return true;
                case R.id.navigation_dashboard:
                    selectedFragment = new FarmerDashboard();
                    getSupportFragmentManager().beginTransaction().replace(R.id.farmerMainContent,new FarmerDashboard()).commit();
                    selectedTab = R.id.navigation_dashboard;
                    return true;
                case R.id.navigation_profile:
                    selectedFragment = new FarmerProfile();
                    getSupportFragmentManager().beginTransaction().replace(R.id.farmerMainContent,new FarmerProfile()).commit();
                    selectedTab = R.id.navigation_profile;
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_main);
        selectedTab = R.id.navigation_dashboard;

        //var init
        uiEnhancement = new UiEnhancement();
        //end var init

        //ui Init
        uiEnhancement.blur(this,R.drawable.crops,10,true,findViewById(R.id.bg));
        //end ui Init

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        selectedFragment = new Fragment();
        //navigation.setSelectedItemId(selectedTab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigation.setSelectedItemId(selectedTab);
    }

    /*@Override
    protected void onStart() {
        navigation.setSelectedItemId(selectedTab);
        super.onStart();
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        selectedFragment.onActivityResult(requestCode,resultCode,data);
    }*/

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        selectedFragment.onSaveInstanceState(outState);
    }*/
}
