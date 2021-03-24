package com.sidcoparking.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.sidcoparking.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerBookingAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    ProgressDialog progressDialog;

    List<Map<String, String>> originaldata;
    List<Map<String, String>> filterData;

    public CustomerBookingAdapter(Activity context, List<Map<String, String>> listCollectionone) {
        activity = context;
        originaldata = listCollectionone;
        filterData = listCollectionone;

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

    }

    @Override
    public int getCount() {
        return this.filterData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.filterData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    public void pos(int position) {

        filterData.remove(filterData.get(position));

    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        if (vi == null) {

            vi = inflater.inflate(R.layout.customerbooking_adapter, null);
            holder = new ViewHolder();

            holder.TxtVehicleType = vi.findViewById(R.id.veh_type);
            holder.TxtVehicleNo = vi.findViewById(R.id.veh_no);
            holder.TxtStreetName = vi.findViewById(R.id.street_name);
            holder.TxtAmount = vi.findViewById(R.id.amount);
            holder.TxtEntryTime = vi.findViewById(R.id.entry_time);
            holder.TxtExpiryTime = vi.findViewById(R.id.expiry_time);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }
        holder.TxtVehicleType.setText(filterData.get(position).get(
                "VehicleType"));
        holder.TxtVehicleNo.setText(filterData.get(position).get(
                "VehicleNo"));
        holder.TxtStreetName.setText(filterData.get(position).get(
                "StreetName"));
        holder.TxtAmount.setText("₹ "+filterData.get(position).get(
                "Amount"));
        holder.TxtEntryTime.setText(filterData.get(position).get(
                "EntryTime"));
        holder.TxtExpiryTime.setText(filterData.get(position).get(
                "ExpiryTime"));
        return vi;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = originaldata;
                    results.count = originaldata.size();
                } else {

                    List<Map<String, String>> filterResultsData = new ArrayList<Map<String, String>>();
                    for (Map<String, String> data : originaldata) {
                        //In this loop, you'll filter through originalData and compare each item to charSequence.
                        //If you find a match, add it to your new ArrayList
                        //I'm not sure how you're going to do comparison, so you'll need to fill out this conditional
                        if (data.get("VehicleNo").contains(constraint)) {
                            filterResultsData.add(data);
                        }
                    }

                    results.values = filterResultsData;
                    results.count = filterData.size();

                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterData = (List<Map<String, String>>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder {

        TextView TxtVehicleType, TxtVehicleNo,TxtStreetName,TxtAmount,TxtEntryTime, TxtExpiryTime;

    }

}
