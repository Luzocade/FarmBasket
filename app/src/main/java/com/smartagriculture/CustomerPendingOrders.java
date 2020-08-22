package com.smartagriculture;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CustomerPendingOrders extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CALL = 123;

    private FirebaseDatabase database;
    private UiEnhancement uiEnhancement;
    private ListView pendingListView;
    private ProgressBar loading;
    private TextView txtEmpty;
    private ArrayList<Order> myOrders;
    private String myPhno;
    private CustomerPendingOrdersListAdapter customerPendingOrdersListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_pending_orders);

        database = FirebaseDatabase.getInstance();
        uiEnhancement = new UiEnhancement();
        pendingListView = findViewById(R.id.pendingListView);
        loading = findViewById(R.id.loading);
        txtEmpty = findViewById(R.id.txtEmpty);
        myOrders = new ArrayList<>();
        customerPendingOrdersListAdapter = new CustomerPendingOrdersListAdapter(this,myOrders);
        myPhno = getSharedPreferences("userDetails",MODE_PRIVATE).getString("phno","");

        uiEnhancement.blur(this,R.drawable.crops,20,true,findViewById(R.id.bg));

        requestCallPermission();

    }

    private void requestCallPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(CustomerPendingOrders.this, Manifest.permission.CALL_PHONE)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_call_permission)
                        .setMessage(R.string.customer_txt_call_permission_description)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CustomerPendingOrders.this,
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


    @Override
    protected void onResume() {
        super.onResume();
        getOrders();
    }

    private void getOrders() {
        uiEnhancement.fadeSwitch(pendingListView,loading,200);
        myOrders.clear();
        DatabaseReference fGroup = database.getReference("farmers");
        fGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot farmer : dataSnapshot.getChildren()){
                        Farmer f = farmer.getValue(Farmer.class);
                        if(f.getOrders().size()>0){
                            for(Order o : f.getOrders()){
                                if(o.getCustomer().getPhno().equalsIgnoreCase(myPhno))
                                    myOrders.add(o);
                            }
                        }
                    }
                    if(myOrders.size()>0) {
                        customerPendingOrdersListAdapter.notifyDataSetChanged();
                        pendingListView.setAdapter(customerPendingOrdersListAdapter);
                        uiEnhancement.fadeSwitch(loading, pendingListView, 200);
                    }else{
                        uiEnhancement.fadeSwitch(loading,txtEmpty,200);
                    }
                }else{
                    uiEnhancement.fadeSwitch(loading,txtEmpty,200);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
}
