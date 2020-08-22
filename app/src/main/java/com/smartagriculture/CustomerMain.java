package com.smartagriculture;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMain extends AppCompatActivity {
    UiEnhancement uiEnhancement;
    SharedPreferences userDetails;
    private static final String TAG = "AppTest>>>>";
    BottomNavigationView navigation;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.customerMainContent,new CustomerHome()).commit();
                    Log.i(TAG, "onNavigationItemSelected: "+R.string.customer_nav_title_home);
                    return true;
                /*case R.id.navigation_chat:
                    Log.i(TAG, "onNavigationItemSelected: "+R.string.customer_nav_title_chats);
                    return true;*/
                case R.id.navigation_profile:
                    getSupportFragmentManager().beginTransaction().replace(R.id.customerMainContent,new CustomerProfile()).commit();
                    Log.i(TAG, "onNavigationItemSelected: "+R.string.customer_nav_title_profile);
                    return true;
                case R.id.navigation_orders:
                    getSupportFragmentManager().beginTransaction().replace(R.id.customerMainContent,new CustomerOrders()).commit();
                    Log.i(TAG, "onNavigationItemSelected: "+R.string.customer_nav_title_orders);
                    return true;
            }
            return false;
        }
    };
    private SharedPreferences userOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);
        uiEnhancement = new UiEnhancement();
        userDetails = getSharedPreferences("userDetails",MODE_PRIVATE);
        userOrders = getSharedPreferences("userOrders",MODE_PRIVATE);

        //getUser();

        uiEnhancement.blur(this,R.drawable.crops,10,true,findViewById(R.id.bg2));

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.customerMainContent,new CustomerHome()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor userOrdersEditor = userOrders.edit();
        userOrdersEditor.clear().apply();
    }
}
