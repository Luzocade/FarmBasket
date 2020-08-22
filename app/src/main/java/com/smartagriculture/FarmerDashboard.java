package com.smartagriculture;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class FarmerDashboard extends Fragment {

    View v;
    Farmer farmer;
    SharedPreferences userDetails;
    TextView txtTotalOrdersCount, txtUrgentOrdersCount, txtRegularOrdersCount,txtOrderDashHeading;
    FirebaseDatabase database;
    ArrayList<Order> orders;
    ArrayList<Announcement> announcements;
    FarmerDashboardAnnouncementAdapter farmerDashboardAnnouncementAdapter;
    LinearLayout layoutAnnouncement;
    Button btnSeeOrders;
    ListView announcementListView;
    UiEnhancement uiEnhancement;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_farmer_dashboard, container, false);
        txtRegularOrdersCount = v.findViewById(R.id.txtRegularOrdersCount);
        txtUrgentOrdersCount = v.findViewById(R.id.txtUrgentOrdersCount);
        txtTotalOrdersCount = v.findViewById(R.id.txtTotalOrdersCount);
        txtOrderDashHeading = v.findViewById(R.id.txtOrderDashHeading);
        layoutAnnouncement = v.findViewById(R.id.layoutAnnouncement);
        btnSeeOrders = v.findViewById(R.id.btnSeeOrders);
        announcementListView = v.findViewById(R.id.announcementListView);
        orders = new ArrayList<>();
        announcements = new ArrayList<>();
        userDetails = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        farmer = new Farmer(userDetails.getString("name", ""), userDetails.getString("phno", ""), userDetails.getString("area", ""));
        database = FirebaseDatabase.getInstance();
        farmerDashboardAnnouncementAdapter = new FarmerDashboardAnnouncementAdapter(getActivity(),announcements);
        uiEnhancement = new UiEnhancement();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uiEnhancement.gone(layoutAnnouncement);
        getAnnouncements();
        getOrders();

    }

    private void getAnnouncements() {
        DatabaseReference announcementGroup = database.getReference("announcements");
        announcementGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for (DataSnapshot announcement : dataSnapshot.getChildren()){
                        Announcement a = announcement.getValue(Announcement.class);
                        /*if(announcement.child("endTime").getRef()==null)
                            announcements.add(a);
                        else */if(Calendar.getInstance().getTimeInMillis()<= Objects.requireNonNull(a).getEndTime()||Objects.requireNonNull(a).getEndTime()==0)
                            announcements.add(a);
                    }
                    farmerDashboardAnnouncementAdapter.notifyDataSetChanged();
                    announcementListView.setAdapter(farmerDashboardAnnouncementAdapter);
                    uiEnhancement.visible(layoutAnnouncement);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        announcementListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent in = new Intent(getActivity(), AnnouncementDetails.class);
                in.putExtra("postDate", announcements.get(i).getPostedTime());
                in.putExtra("msg", announcements.get(i).getAnnouncement());
                in.putExtra("endTime", announcements.get(i).getEndTime());
                Objects.requireNonNull(getActivity()).startActivity(in);
            }
        });

        btnSeeOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(getActivity(),FarmerOrders.class);
                Objects.requireNonNull(getActivity()).startActivity(in);
            }
        });
    }

    private void getOrders() {
        txtOrderDashHeading.setText(R.string.farmer_product_products_heading_loading_txt);
        final GenericTypeIndicator<ArrayList<Order>> typeOrder = new GenericTypeIndicator<ArrayList<Order>>() {
        };
        final DatabaseReference ordersRef = database.getReference("farmers/"+farmer.getPhno()+"/orders");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    orders = dataSnapshot.getValue(typeOrder);
                    txtTotalOrdersCount.setText(String.valueOf(orders.size()));
                    txtRegularOrdersCount.setText(String.valueOf(orders.size() - countUrgent()));
                    txtUrgentOrdersCount.setText(String.valueOf(countUrgent()));
                }
                txtOrderDashHeading.setText(R.string.farmer_dashboard_seg1_heading_txt);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int countUrgent() {
        int count = 0;
        for (Order order : /*farmer.getOrders()*/ orders)
            if (order.isUrgent())
                count++;
        return count;
    }

}
