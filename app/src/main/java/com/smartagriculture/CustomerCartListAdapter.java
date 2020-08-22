package com.smartagriculture;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.util.ArrayList;

public class CustomerCartListAdapter extends BaseAdapter {

    private static final String TAG = "AppTest>>>>";
    Context context;
    ArrayList<Order> orders;
    String name;
    private LayoutInflater inflater;
    private FirebaseDatabase database;
    private SharedPreferences userOrders;
    private String[] units = {"kg", "g", "ltr", "ml"};
    private ViewHolder holder;
    private ListView listView;
    private UiEnhancement uiEnhancement;
    private View from,to;


    public CustomerCartListAdapter(Context context, ArrayList<Order> orders, ListView listView, LinearLayout from, LinearLayout to) {

        this.inflater = LayoutInflater.from(context);
        this.orders = orders;
        this.context = context;
        this.listView = listView;
        database = FirebaseDatabase.getInstance();
        userOrders = context.getSharedPreferences("userOrders", Context.MODE_PRIVATE);
        this.from = from;
        this.to = to;
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
    public View getView(final int i, View view, ViewGroup viewGroup) {

        holder = null;

        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.cart_list_item_layout, null);
            holder.txtOwnerName = view.findViewById(R.id.txtOwnerName);
            holder.txtPrice = view.findViewById(R.id.txtPrice);
            holder.txtCategory = view.findViewById(R.id.txtCategory);
            holder.txtProductName = view.findViewById(R.id.txtProductName);
            holder.pickerQty = view.findViewById(R.id.pickerQty);
            uiEnhancement = new UiEnhancement();

            /*
            TODOD: 08-04-2018 get product details
            - category
            - Owner name
            - Product Name
            - Price
            - qty form saved orders
            */

//            getOwner(orders.get(i).getOwnerId());
            holder.txtOwnerName.setText(orders.get(i).getProduct().getOwnerName());
            holder.txtCategory.setText(orders.get(i).getProduct().getCategoryName());
            holder.txtPrice.setText(orders.get(i).getProduct().getPrice());
            holder.txtProductName.setText(orders.get(i).getProduct().getName());
            holder.pickerQty.setValue(Integer.parseInt(orders.get(i).getQuantity()));
            /*holder.pickerQty.setValue(userOrders.getInt(orders.get(i).getPrice() +
                    "_" + orders.get(i).getOwnerName() +
                    "_" + orders.get(i).getOwnerId() +
                    "_" + orders.get(i).getCategoryName() +
                    "_" + orders.get(i).getName(), 0));*/
            int unit;
            for (String u : units) {
                if (orders.get(i).getProduct().getPrice().split(" / ")[1].contains(u)) {
                    if (orders.get(i).getProduct().getPrice().split(" / ")[1].replace(u, "").isEmpty())
                        unit = 1;
                    else
                        unit = Integer.parseInt(orders.get(i).getProduct().getPrice().split(" / ")[1].replace(u, ""));
                    //Log.i(TAG, "getView: unit: " + orders.get(i).getPrice().split(" / ")[1].replace(u, ""));
                    holder.pickerQty.setUnit(unit);
                    holder.pickerQty.setMax(100 * unit);
                    break;
                }
            }


            final View finalView = view;
            holder.pickerQty.setValueChangedListener(new ValueChangedListener() {
                @Override
                public void valueChanged(int value, ActionEnum action) {
                    SharedPreferences.Editor userOrderEditor = userOrders.edit();
                    if (value != 0)
                        userOrderEditor.putInt(orders.get(i).getProduct().getPrice() +
                                "_" + orders.get(i).getProduct().getOwnerName() +
                                "_" + orders.get(i).getProduct().getQuantity() +
                                "_" + orders.get(i).getProduct().getOwnerId() +
                                "_" + orders.get(i).getProduct().getCategoryName() +
                                "_" + orders.get(i).getProduct().getName(), value);
                    else {
                        userOrderEditor.remove(orders.get(i).getProduct().getPrice() +
                                "_" + orders.get(i).getProduct().getOwnerName() +
                                "_" + orders.get(i).getProduct().getQuantity() +
                                "_" + orders.get(i).getProduct().getOwnerId() +
                                "_" + orders.get(i).getProduct().getCategoryName() +
                                "_" + orders.get(i).getProduct().getName());
                        orders.remove(i);
                        listView.removeViewInLayout(finalView);
                        notifyDataSetChanged();
                        if(getCount()==0)
                            uiEnhancement.fadeSwitch(from,to,0);
                    }
                    userOrderEditor.apply();
                    Log.i(TAG, "valueChanged: orders: " + userOrders.getAll());
                }
            });

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private class ViewHolder {
        TextView txtOwnerName, txtPrice, txtCategory, txtProductName;
        NumberPicker pickerQty;
    }
}