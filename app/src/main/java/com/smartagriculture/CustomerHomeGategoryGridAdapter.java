package com.smartagriculture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomerHomeGategoryGridAdapter extends BaseAdapter {

    private static final String TAG = "AppTest>>>>";
    LayoutInflater inflater;
    Context context;
    ArrayList<Category> categories;
    ViewHolder holder;

    CustomerHomeGategoryGridAdapter(Context context, ArrayList<Category> categories){
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.categories = categories;
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
        return categories.size();
    }

    @Override
    public Object getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.category_grid_item_layout, null);
            holder.txtCategoryName = view.findViewById(R.id.txtCategoryName);
            holder.imgCategory = view.findViewById(R.id.imgCategory);

            holder.txtCategoryName.setText(categories.get(i).getName());
            Glide.with(context).load(categories.get(i).getPic()).into(holder.imgCategory);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        return view;
    }

    private class ViewHolder{
        ImageView imgCategory;
        TextView txtCategoryName;
    }
}
