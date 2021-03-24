package com.sidcoparking.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sidcoparking.R;
import com.sidcoparking.utils.CommonAlertDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Activity activity;
    List<Map<String, String>> filterData;
    CommonAlertDialog alert;
    public HashMap<Integer, Boolean> hashMapSelected;

    public VehicleListAdapter(Activity context, List<Map<String, String>> listCollectionone) {

        activity = context;
        filterData = listCollectionone;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        alert = new CommonAlertDialog(activity);
        hashMapSelected = new HashMap<>();
        for (int i = 0; i < filterData.size(); i++) {
            hashMapSelected.put(i, false);
        }

    }

    @Override
    public int getCount() {
        return filterData.size();
    }

    @Override
    public Object getItem(int position) {
        return filterData.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void makeAllUnselect(int position) {
        hashMapSelected.put(position, true);
        for (int i = 0; i < hashMapSelected.size(); i++) {
            if (i != position)
                hashMapSelected.put(i, false);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // View vi = convertView;
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.vehicle_adapter, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.VehicleType.setText(filterData.get(position).get(
                "VehicleType"));
        if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("1")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.car));
        } else if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("2")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.bike));
        } else if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("3")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.auto));
        } else if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("4")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.rickshaw));
        } else if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("5")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.bus));
        } else if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("6")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.hcv));
        } else if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("7")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.lcv));
        } else if (filterData.get(position).get(
                "VehicleTypeId").equalsIgnoreCase("8")) {
            holder.Image.setImageDrawable(activity.getResources().getDrawable(R.drawable.minibus));
        }

        if (hashMapSelected.get(position) == true) {
            holder.bg.setBackground(activity.getResources().getDrawable(R.drawable.circle_blue));
        } else {
            holder.bg.setBackground(activity.getResources().getDrawable(R.drawable.circle));
        }

        //1	Car
        //2	Two Wheeler
        //3	Auto Rickshaw
        //4	Cycle Rickshaw
        //5	Bus
        //6	HCV
        //7	LCV
        //8	Mini Bus

        return convertView;
    }

    public static class ViewHolder {
        TextView VehicleType;
        ImageView Image;
        LinearLayout bg;

        public ViewHolder(View view) {
            Image = view.findViewById(R.id.carimage);
            VehicleType = view.findViewById(R.id.vehicle_type);
            bg = view.findViewById(R.id.colorchange);
        }
    }
}

