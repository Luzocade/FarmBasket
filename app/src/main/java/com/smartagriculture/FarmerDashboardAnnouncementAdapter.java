package com.smartagriculture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FarmerDashboardAnnouncementAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ViewHolder holder;
    public ArrayList<Announcement> announcements;

    public FarmerDashboardAnnouncementAdapter(Context context, ArrayList<Announcement> announcements) {
        this.context = context;
        this.announcements = announcements;
        this.inflater = LayoutInflater.from(context);
    }

    private class ViewHolder{
        TextView messageView;
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
        return announcements.size();
    }

    @Override
    public Object getItem(int i) {
        return announcements.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.farmer_dashboard_announcement_item_layout, null);
            holder.messageView = view.findViewById(R.id.txtAnnounceMessage);
            holder.messageView.setText(announcements.get(i).getAnnouncement());

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        return view;
    }
}
