package com.smartagriculture;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.util.ArrayList;

public class CustomerHomeProductListAdapter extends BaseAdapter {

    private static final String TAG = "AppTest>>>>";
    Context context;
    ArrayList<Product> products;
    String name;
    private LayoutInflater inflater;
    private FirebaseDatabase database;
    private SharedPreferences userOrders;
    private String[] units = {"kg", "g", "ltr", "ml"};
    private ViewHolder holder;


    public CustomerHomeProductListAdapter(Context context, ArrayList<Product> products) {

        this.inflater = LayoutInflater.from(context);
        this.products = products;
        this.context = context;
        database = FirebaseDatabase.getInstance();
        userOrders = context.getSharedPreferences("userOrders", Context.MODE_PRIVATE);
    }


    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int i) {
        return products.get(i);
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
            view = inflater.inflate(R.layout.products_grid_item_layout, null);
            holder.txtOwnerName = view.findViewById(R.id.txtOwnerName);
            holder.txtPrice = view.findViewById(R.id.txtPrice);
            holder.txtCategory = view.findViewById(R.id.txtCategory);
            holder.txtProductName = view.findViewById(R.id.txtProductName);
            holder.pickerQty = view.findViewById(R.id.pickerQty);

            /*
            TODOD: 08-04-2018 get product details
            - category
            - Owner name
            - Product Name
            - Price
            - qty form saved orders
            */

//            getOwner(orders.get(i).getOwnerId());
            holder.txtOwnerName.setText(products.get(i).getOwnerName());
            holder.txtCategory.setText(products.get(i).getCategoryName());
            holder.txtPrice.setText(products.get(i).getPrice());
            holder.txtProductName.setText(products.get(i).getName());
            holder.pickerQty.setValue(userOrders.getInt(products.get(i).getPrice() +
                    "_" + products.get(i).getOwnerName() +
                    "_" + products.get(i).getQuantity() +
                    "_" + products.get(i).getOwnerId() +
                    "_" + products.get(i).getCategoryName() +
                    "_" + products.get(i).getName(), 0));
            int unit;
            for (String u : units) {
                if (products.get(i).getPrice().split(" / ")[1].contains(u)) {
                    if (products.get(i).getPrice().split(" / ")[1].replace(u, "").isEmpty())
                        unit = 1;
                    else
                        unit = Integer.parseInt(products.get(i).getPrice().split(" / ")[1].replace(u, ""));
                    holder.pickerQty.setUnit(unit);
                    holder.pickerQty.setMax(100 * unit);
                    break;
                }
            }


            holder.pickerQty.setValueChangedListener(new ValueChangedListener() {
                @Override
                public void valueChanged(int value, ActionEnum action) {
                    SharedPreferences.Editor userOrderEditor = userOrders.edit();
                    if (value != 0)
                        userOrderEditor.putInt(products.get(i).getPrice() +
                                "_" + products.get(i).getOwnerName() +
                                "_" + products.get(i).getQuantity() +
                                "_" + products.get(i).getOwnerId() +
                                "_" + products.get(i).getCategoryName() +
                                "_" + products.get(i).getName(), value);
                    else
                        userOrderEditor.remove(products.get(i).getPrice() +
                                "_" + products.get(i).getOwnerName() +
                                "_" + products.get(i).getQuantity() +
                                "_" + products.get(i).getOwnerId() +
                                "_" + products.get(i).getCategoryName() +
                                "_" + products.get(i).getName());
                    userOrderEditor.apply();
                    //Log.i(TAG, "valueChanged: orders: " + userOrders.getAll());
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