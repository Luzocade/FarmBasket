package com.smartagriculture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomerPendingOrdersListAdapter extends BaseAdapter {

    private class ViewHolder{
        TextView txtTime,txtSellerName,txtQtyPrice,txtProductName;
        ImageButton btnCall;
    }

    private ViewHolder holder;
    private ArrayList<Order> orders;
    private LayoutInflater inflater;
    private Context context;

    public CustomerPendingOrdersListAdapter(Context context, ArrayList<Order> orders) {
        this.orders = orders;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
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
        if(view == null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.customer_pending_orders_list_item_layout,null);
            holder.txtTime = view.findViewById(R.id.txtTime);
            holder.txtSellerName = view.findViewById(R.id.txtSellerName);
            holder.txtQtyPrice = view.findViewById(R.id.txtQtyPrice);
            holder.txtProductName = view.findViewById(R.id.txtProductName);
            holder.btnCall = view.findViewById(R.id.btnCall);

            holder.txtTime.setText(android.text.format.DateFormat.format("EEE dd MMM yyyy",orders.get(i).getTime()));
            holder.txtSellerName.setText(orders.get(i).getProduct().getOwnerName());
            holder.txtProductName.setText(orders.get(i).getProduct().getName());
            holder.txtQtyPrice.setText(String.format("%s %s x %s = %s %s",
                    orders.get(i).getQuantity(),
                    orders.get(i).getProduct().getPrice().split(" ")[3],
                    orders.get(i).getProduct().getPrice(),
                    context.getString(R.string.farmer_products_add_product_currency_txt),
                    String.valueOf(Integer.parseInt(orders.get(i).getProduct().getPrice().split(" ")[1]) * Integer.parseInt(orders.get(i).getQuantity()))));
            final int index = i;
            holder.btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        if (ContextCompat.checkSelfPermission(context,
                                Manifest.permission.CALL_PHONE)
                                == PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent(Intent.ACTION_CALL);

                            intent.setData(Uri.parse("tel:" + orders.get(index).getProduct().getOwnerId()));
                            context.startActivity(intent);
                        }

                }
            });

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }
}
