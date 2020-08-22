package com.smartagriculture;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class FarmerOrders extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CALL = 123;

    private ArrayList<Order> orders;
    private FirebaseDatabase database;
    private ListView ordersListView;
    private FarmerOrderListAdapter farmerOrderListAdapter;
    private ProgressBar loading;
    private UiEnhancement uiEnhancement;
    private SharedPreferences userDetails;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_orders);

        uiEnhancement = new UiEnhancement();
        userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);

        uiEnhancement.blur(this, R.drawable.crops, 20, true, findViewById(R.id.bg));

        database = FirebaseDatabase.getInstance();
        orders = new ArrayList<>();
        ordersListView = findViewById(R.id.ordersListView);
        farmerOrderListAdapter = new FarmerOrderListAdapter(this, orders);
        loading = findViewById(R.id.loading);
        txtEmpty = findViewById(R.id.txtEmpty);

        uiEnhancement.gone(ordersListView, txtEmpty);
        uiEnhancement.visible(loading);
        loading.setIndeterminate(true);

        ordersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="
                        + farmerOrderListAdapter.orders.get(i).getCustomer().getAddress().getLatitude()
                        + ","
                        + farmerOrderListAdapter.orders.get(i).getCustomer().getAddress().getLongitude()
                        + "&mode=d");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });


        ordersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int index = i;
                new AlertDialog.Builder(Objects.requireNonNull(FarmerOrders.this))
                        .setMessage(R.string.farmer_orders_delivery_confirmation_prompt)
                        .setPositiveButton(R.string.farmer_orders_delivery_confirmation_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uiEnhancement.fadeSwitch(ordersListView,loading,200);
                                orders.remove(index);
                                DatabaseReference ordersRef = database.getReference("farmers/" + userDetails.getString("phno", "") + "/orders");
                                ordersRef.setValue(orders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        getOrders();
                                    }
                                });
                            }
                        }).setNegativeButton(R.string.farmer_orders_delivery_confirmation_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setNeutralButton(R.string.farmer_order_list_tem_call_btn_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        makeCall(orders.get(index).getCustomer().getPhno());
                    }
                }).create().show();


                return true;
            }
        });

        requestCallPermission();
    }



    private void requestCallPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(FarmerOrders.this, Manifest.permission.CALL_PHONE)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_call_permission)
                        .setMessage(R.string.text_call_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(FarmerOrders.this,
                                        new String[]{Manifest.permission.CALL_PHONE},
                                        MY_PERMISSIONS_REQUEST_CALL);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL);
            }
        }
    }

    private void getOrders() {
        orders.clear();
        DatabaseReference farmer = database.getReference("farmers/" + userDetails.getString("phno", ""));
        farmer.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Farmer f = dataSnapshot.getValue(Farmer.class);
                if (f != null) {
                    orders.addAll(f.getOrders());
                    if (orders.size() > 0) {
                        Collections.sort(orders, new Comparator<Order>() {
                            @Override
                            public int compare(Order o1, Order o2) {
                                return Boolean.compare(!o1.isUrgent(), !o2.isUrgent());
                            }
                        });
                        farmerOrderListAdapter.notifyDataSetChanged();
                        ordersListView.setAdapter(farmerOrderListAdapter);
                        uiEnhancement.fadeSwitch(loading, ordersListView, 200);
                    } else {
                        if (loading.getVisibility() == View.VISIBLE)
                            uiEnhancement.fadeSwitch(loading, txtEmpty, 200);
                        else if (ordersListView.getVisibility() == View.VISIBLE)
                            uiEnhancement.fadeSwitch(ordersListView, txtEmpty, 200);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getOrders();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Call permission granted", Toast.LENGTH_SHORT).show();

                } else {
                    requestCallPermission();
                }
                return;
            }

        }
    }

    private void makeCall(String phno) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);

            intent.setData(Uri.parse("tel:" + phno));
            startActivity(intent);
        }


    }
}
