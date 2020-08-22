package com.smartagriculture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FarmerProductsProductListAdapter extends BaseAdapter {

    public ArrayList<Product> products = null;
    private LayoutInflater inflater = null;
    private Context context = null;
    private ViewHolder holder = null;
    private UiEnhancement uiEnhancement = null;

    public FarmerProductsProductListAdapter(Context context, ArrayList<Product> products) {

        this.inflater = LayoutInflater.from(context);
        this.products = products;
        this.context = context;
    }

    @Override
    public int getCount() {
        return this.products.size();
    }

    @Override
    public Object getItem(int i) {
        return this.products.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.farmer_products_product_list_item_layout, null);
            holder.txtAvailable = view.findViewById(R.id.txtAvailable);
            holder.txtProductName = view.findViewById(R.id.txtProductName);
            holder.txtPrice = view.findViewById(R.id.txtPrice);
            uiEnhancement = new UiEnhancement();

            holder.txtProductName.setText(products.get(i).getName());
            holder.txtAvailable.setText(products.get(i).getQuantity());
            holder.txtPrice.setText(products.get(i).getPrice());
            uiEnhancement.hide(false, 0, 0, view);
            uiEnhancement.show(true, 100 / products.size() * i, 200, view);
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

    public Product getProductAt(int i) {
        return this.products.get(i);
    }

    private class ViewHolder {

        TextView txtProductName = null, txtAvailable = null, txtPrice = null;

    }
}
