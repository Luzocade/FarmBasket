package com.smartagriculture;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class CustomerOrders extends Fragment {

    private static final String TAG = "AppTest>>>>";
    private LinearLayout layoutOrders, layoutEmptyOrders,layoutSubmitSuccess;
    private UiEnhancement uiEnhancement;
    private ListView ordersList;
    private Button btnConfirm, btnShop,btnConShop;
    private CheckBox checkUrgent;
    private View v;
    private SharedPreferences userDetails, userOrders;
    private ArrayList<Order> orders;
    private CustomerCartListAdapter customerCartListAdapter;
    private FirebaseDatabase database;
    private int ordersToPut = 0, ordersPut = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_customer_orders, container, false);

        layoutEmptyOrders = v.findViewById(R.id.layoutEmptyOrders);
        layoutOrders = v.findViewById(R.id.layoutOrders);
        layoutSubmitSuccess = v.findViewById(R.id.layoutSubmitSuccess);
        ordersList = v.findViewById(R.id.ordersList);
        btnConfirm = v.findViewById(R.id.btnConfirm);
        btnShop = v.findViewById(R.id.btnShop);
        btnConShop = v.findViewById(R.id.btnConShop);
        checkUrgent = v.findViewById(R.id.checkUrgent);
        uiEnhancement = new UiEnhancement();
        userDetails = Objects.requireNonNull(getActivity()).getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        userOrders = Objects.requireNonNull(getActivity()).getSharedPreferences("userOrders", Context.MODE_PRIVATE);
        orders = new ArrayList<>();
        customerCartListAdapter = new CustomerCartListAdapter(getActivity(), orders, ordersList, layoutOrders, layoutEmptyOrders);
        database = FirebaseDatabase.getInstance();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getOrders();
        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.customerMainContent, new CustomerHome()).commit();
                ((CustomerMain) getActivity()).navigation.setSelectedItemId(R.id.navigation_home);
            }
        });

        btnConShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.customerMainContent, new CustomerHome()).commit();
                ((CustomerMain) getActivity()).navigation.setSelectedItemId(R.id.navigation_home);
            }
        });

        checkUrgent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                for (Order o : orders)
                    o.setUrgent(b);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitOrders();
            }
        });

        /*SharedPreferences.Editor orderEditor = userOrders.edit();
        orderEditor.clear();
        orderEditor.apply();*/

    }

    private void submitOrders() {

        if(userDetails.getString("addr","").equalsIgnoreCase("")){
            new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                    .setTitle(R.string.title_address_not_set)
                    .setMessage(R.string.text_address_not_set)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            Intent intent = new Intent(getActivity(),EditCustomerAddress.class);
                            getActivity().startActivity(intent);
                        }
                    })
                    .create()
                    .show();
            return;
        }

        SharedPreferences.Editor userOrdersEditor = userOrders.edit();
        userOrdersEditor.clear().apply();

        Addr address = new Addr(userDetails.getString("addr",""),
                Double.parseDouble(userDetails.getString("addrPt","").split("_")[0]),
                Double.parseDouble(userDetails.getString("addrPt","").split("_")[1]));

        ordersToPut = orders.size();
        for (Order o : orders) {
            o.setCustomer(new Customer(userDetails.getString("name", ""), userDetails.getString("phno", ""), userDetails.getString("area", "")));
            o.setTime(Calendar.getInstance().getTimeInMillis());
            o.setDelivered(false);
            o.getCustomer().setAddress(address);
        }

        ArrayList<String> distinctOwners;
        distinctOwners = findDistinctOwners();

        final GenericTypeIndicator<ArrayList<Order>> typeOrder = new GenericTypeIndicator<ArrayList<Order>>() {
        };

        for (final String ownerId : distinctOwners) {
            final ArrayList<Order> ownerOrders = new ArrayList<>();
            for (Order o : orders)
                if (o.getProduct().getOwnerId().equals(ownerId))
                    ownerOrders.add(o);
            Log.i(TAG, "submitOrders: ownerOrdersSize: " + ownerOrders.size());
            final DatabaseReference farmerOrders = database.getReference("farmers/" + ownerId + "/orders");
            farmerOrders.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()) {
                        ArrayList<Order> orderArrayList = dataSnapshot.getValue(typeOrder);
                        if (orderArrayList != null) {
                            //Log.i(TAG, "onDataChange: recievedArrayList: " + orderArrayList.size());
                        }
                        orderArrayList.addAll(ownerOrders);
                        farmerOrders.setValue(orderArrayList).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Log.i(TAG, "onSuccess: add success for: " + ownerId + " : "+orderArrayList.size());
                            }
                        });
                    }else{
                        farmerOrders.setValue(ownerOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Log.i(TAG, "onSuccess: add success for: " + ownerId + " : "+ownerOrders.size());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        uiEnhancement.fadeSwitch(layoutOrders,layoutSubmitSuccess,200);
    }


    private ArrayList<String> findDistinctOwners() {
        ArrayList<String> DO = new ArrayList<>();
        for (Order o : orders) {
            if (!DO.contains(o.getProduct().getOwnerId()))
                DO.add(o.getProduct().getOwnerId());
        }

        return DO;
    }

    private void getOrders() {
        Map<String, ?> orderMap = userOrders.getAll();
        for (Map.Entry<String, ?> entry : orderMap.entrySet()) {
            String[] ps = entry.getKey().split("_");
            Product p = new Product(ps[4], ps[5], ps[2], ps[3], ps[0]);
            p.setOwnerName(ps[1]);
            Order o = new Order(p, entry.getValue().toString(), false);
            orders.add(o);
        }
        customerCartListAdapter.notifyDataSetChanged();
        if (customerCartListAdapter.getCount() >= 1)
            ordersList.setAdapter(customerCartListAdapter);
        else
            uiEnhancement.fadeSwitch(layoutOrders, layoutEmptyOrders, 0);
    }
}
