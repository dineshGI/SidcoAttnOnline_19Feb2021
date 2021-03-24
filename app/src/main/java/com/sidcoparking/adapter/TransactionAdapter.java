package com.sidcoparking.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sidcoparking.R;
import com.sidcoparking.activity.MainActivity;
import com.sidcoparking.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TransactionAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    ProgressDialog progressDialog;

    List<Map<String, String>> originaldata;
    List<Map<String, String>> filterData;

    public TransactionAdapter(Activity context, List<Map<String, String>> listCollectionone) {
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

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        if (vi == null) {

            vi = inflater.inflate(R.layout.transaction_adapter, null);
            holder = new ViewHolder();

            holder.TxtVehicleType = vi.findViewById(R.id.veh_type);
            holder.TxtVehicleNo = vi.findViewById(R.id.veh_no);
            holder.TxtEntryTime = vi.findViewById(R.id.entry_time);
            holder.TxtExpiryTime = vi.findViewById(R.id.expiry_time);
            holder.Reprint = vi.findViewById(R.id.btn_reprint);
            holder.TxtHours = vi.findViewById(R.id.hours);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.TxtVehicleType.setText(": " + filterData.get(position).get(
                "VehicleType"));
        holder.TxtVehicleNo.setText(": " + filterData.get(position).get(
                "VehicleNo"));
        holder.TxtEntryTime.setText(": " + filterData.get(position).get(
                "Entrytime"));
        holder.TxtExpiryTime.setText(": " + filterData.get(position).get(
                "Expirytime"));
        holder.TxtHours.setText(": " + filterData.get(position).get(
                "ParkingHour"));

        holder.Reprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //((ListView) parent).performItemClick(v, position, 0);

                Log.e("asdsad", filterData.get(position).get(
                        "ParkingHour"));
                StringBuffer printvalues = new StringBuffer();
                if (filterData.get(position).get(
                        "Isfoc").equalsIgnoreCase("2")) {
                    printvalues.append(activity.getString(R.string.onstreet_parking) + "\n");
                    printvalues.append(activity.getString(R.string.reprint_ticket) + "\n");
                    printvalues.append(activity.getString(R.string.line) + "\n");
                    printvalues.append(filterData.get(position).get(
                            "StreetName") + "\n");
                    printvalues.append("V.No     :" + filterData.get(position).get(
                            "VehicleNo") + "\n");
                    printvalues.append("V.Type   :" + filterData.get(position).get(
                            "VehicleType") + "\n");
                    if (!filterData.get(position).get(
                            "MobileNo").isEmpty()) {
                        printvalues.append("Mobile No:" + filterData.get(position).get(
                                "MobileNo") + "\n");
                    }
                    if (filterData.get(position).get(
                            "Isfoc").equalsIgnoreCase("1")) {
                        printvalues.append("FOC      :" + "Yes" + "\n");
                    }

                    printvalues.append("Txn ID   :" + filterData.get(position).get(
                            "TransId") + "\n");

                    printvalues.append("Entry    :" + filterData.get(position).get(
                            "Entrytime") + "\n");

                    String StrHour = Util.showextrabyminutes(filterData.get(position).get(
                            "Expirytime"), filterData.get(position).get(
                            "Entrytime") + "\n");

                    printvalues.append("Hours    :" + StrHour + " Mins Grace Time" + "\n");

                    printvalues.append("Amount   :" + "Rs." + filterData.get(position).get(
                            "ParkingFee") + "\n");
                    printvalues.append(activity.getString(R.string.line) + "\n");
                    printvalues.append(activity.getString(R.string.mdg_by_gerek) + "\n");
                    printvalues.append(activity.getString(R.string.pwd_by_giretail) + "\n");
                    printvalues.append(activity.getString(R.string.parking_ownrisk) + "\n");

                } else {

                    printvalues.append(activity.getString(R.string.onstreet_parking) + "\n");
                    printvalues.append(activity.getString(R.string.reprint_ticket) + "\n");
                    printvalues.append(activity.getString(R.string.line) + "\n");
                    printvalues.append(filterData.get(position).get(
                            "StreetName") + "\n");
                    printvalues.append("V.No     :" + filterData.get(position).get(
                            "VehicleNo") + "\n");
                    printvalues.append("V.Type   :" + filterData.get(position).get(
                            "VehicleType") + "\n");

                    if (!filterData.get(position).get(
                            "MobileNo").isEmpty()) {
                        printvalues.append("Mobile No:" + filterData.get(position).get(
                                "MobileNo") + "\n");
                    }
                    if (filterData.get(position).get(
                            "Isfoc").equalsIgnoreCase("1")) {
                        printvalues.append("FOC      :" + "Yes" + "\n");
                    }
                    printvalues.append("Txn ID   :" + filterData.get(position).get(
                            "TransId") + "\n");
                    printvalues.append("Entry:" + filterData.get(position).get(
                            "Entrytime") + "\n");

                    printvalues.append("Expiry:" + filterData.get(position).get(
                            "Expirytime") + "\n");
                    printvalues.append("Hours    :" + filterData.get(position).get(
                            "ParkingHour") + "\n");
                    printvalues.append("Amount   :" + "Rs." + filterData.get(position).get(
                            "ParkingFee") + "\n");
                    printvalues.append(activity.getString(R.string.line));
                    printvalues.append("\n" + "Pwd by GI Retail Pvt.Ltd");
                    printvalues.append("\n" + "  Mgd by GRGK Pvt. Ltd");
                    printvalues.append("\n" + " PARKING AT OWNERS RISK" + "\n\n\n\n");
                }
                MainActivity.printContent = printvalues.toString();
                Util.Logcat.e("PRINT" + printvalues.toString());
                MainActivity.getInstance().CommonPrint();


            }
        });

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
        TextView TxtVehicleType, TxtVehicleNo, TxtEntryTime, TxtExpiryTime, TxtHours;
        //Button Reprint;
        ImageView Reprint;

    }

}
