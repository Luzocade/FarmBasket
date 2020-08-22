package com.smartagriculture;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class FarmerOrderListAdapter extends BaseAdapter {

    public ArrayList<Order> orders;
    private Context context;
    private LayoutInflater inflater;
    private ViewHolder holder;
    private String[] units = {"kg", "g", "ltr", "ml"};

    public FarmerOrderListAdapter(Context context, ArrayList<Order> orders) {
        this.orders = orders;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return super.areAllItemsEnabled();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int i) {
        return orders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.farmer_order_list_item_layout, null);


            holder.itemContainer = view.findViewById(R.id.itemContainer);
            holder.txtAddress = view.findViewById(R.id.txtAddress);
            holder.txtCustomerName = view.findViewById(R.id.txtCustomerName);
            holder.txtProductName = view.findViewById(R.id.txtProductName);
            holder.txtQty = view.findViewById(R.id.txtQty);

            holder.txtAddress.setText(orders.get(i).getCustomer().getAddress().getAddressString());
            holder.txtCustomerName.setText(orders.get(i).getCustomer().getName());
            holder.txtProductName.setText(orders.get(i).getProduct().getName());
            for (String u : units) {
                if (orders.get(i).getProduct().getPrice().split(" / ")[1].contains(u)) {
                    holder.txtQty.setText(orders.get(i).getQuantity() +" "+u);
                    break;
                }
            }
            if(orders.get(i).isUrgent())
                holder.itemContainer.setBackgroundResource(R.drawable.urgent_txtbox_bg);


            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        return view;
    }




    private class ViewHolder {
        TextView txtProductName, txtQty, txtCustomerName, txtAddress;
        LinearLayout itemContainer;
    }


}