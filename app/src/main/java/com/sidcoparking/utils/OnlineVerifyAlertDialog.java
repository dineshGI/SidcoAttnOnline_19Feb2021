package com.sidcoparking.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.R;
import com.sidcoparking.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class OnlineVerifyAlertDialog {
    private Activity mActivity;

    public OnlineVerifyAlertDialog(Activity a) {
        this.mActivity = a;

    }

    @SuppressWarnings("InflateParams")
    public void build(final JSONObject data) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.check_alert_online);
        // set the custom dialog components - text, image and button

        TextView Tvstatus = dialog.findViewById(R.id.status);
        TextView Tvextrahour = dialog.findViewById(R.id.extrahour);
        TextView Tvaddition = dialog.findViewById(R.id.addition);
        TextView Tventry = dialog.findViewById(R.id.entry);
        TextView Tvexit = dialog.findViewById(R.id.exit);
        try {
            Tvstatus.setText(data.getString("StatusDesc"));
            Tvextrahour.setText("Extra Hour :" + data.getString("Duration"));
            Tvaddition.setText("Additional Charges :" + data.getString("FareAmount"));
            Tventry.setText("Entry Time :" + data.getString("EntryTime"));
            Tvexit.setText("Expiry Time :" + data.getString("ExpiryTime"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // image.setImageResource(R.mipmap.ic_launcher);
        Button BtnPrint = dialog.findViewById(R.id.print);
        BtnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    DatabaseHelper TransactionDB = new DatabaseHelper(mActivity);
                    boolean isInserted = TransactionDB.insertData(data.getString("Transid"),
                            data.getString("VehicleTypeID"),
                            data.getString("VehicleNo"),
                            data.getString("StreetId"),
                            data.getString("Duration"),
                            data.getString("FareAmount"),
                            data.getString("IsGraceHour"),
                            data.getString("GraceHour"),
                            data.getString("GraceFee"),
                            data.getString("IsFOC"),
                            data.getString("UserId"),
                            data.getString("EntryTime"),
                            data.getString("ExpiryTime"),
                            data.getString("ServerTransid"),
                            data.getString("PaymentMode"),
                            data.getString("MobileNumber"),
                            data.getString("StreetName"), data.getString("Datetime"));
                    if (isInserted == true) {
                        Toast.makeText(mActivity, "Data Inserted", Toast.LENGTH_LONG).show();
                    }
                    StringBuffer printvalues = new StringBuffer();
                    printvalues.append(mActivity.getString(R.string.onstreet_parking)+ "\n");
                    printvalues.append(mActivity.getString(R.string.line)+ "\n");
                    printvalues.append(data.getString("StreetName")+ "\n");
                    printvalues.append("V.No     :" + data.getString("VehicleNo")+ "\n");
                    printvalues.append("V.Type   :" + data.getString("VehicleType")+ "\n");
                    if (!data.getString("MobileNumber").isEmpty()) {
                        printvalues.append("Mobile No:" + data.getString("MobileNumber")+ "\n");
                    }
                    if (data.getString("IsFOC").equalsIgnoreCase("1")) {
                        printvalues.append("FOC      :" + "Yes"+ "\n");
                    }
                    printvalues.append("Txn ID   :" + data.getString("Transid")+ "\n");
                    printvalues.append("Date     :" + Util.parkingtime(0)+ "\n");
                    printvalues.append("Entry    :" + data.getString("EntryTime")+ "\n");
                    printvalues.append("Expiry   :" + data.getString("ExpiryTime")+ "\n");
                    printvalues.append("Hours    :" + data.getString("Duration")+ "\n");
                    printvalues.append("Duration :" + Util.showparkingtime(data.getString("EntryTime"), data.getString("ExpiryTime"))+ "\n");
                    Util.Logcat.e("Duration::" + Util.showparkingtime(data.getString("EntryTime"), data.getString("ExpiryTime"))+ "\n");
                    printvalues.append("Amount   :" + "Rs." + data.getString("FareAmount")+ "\n");
                    printvalues.append(mActivity.getString(R.string.line)+ "\n");
                    printvalues.append("Powered by GI Retail Pvt. Ltd"+ "\n");
                    printvalues.append("  Mgd by GRGK Pvt. Ltd    "+ "\n");
                    printvalues.append("  PARKING AT OWNERS RISK   "+ "\n\n");
                    MainActivity.printContent = printvalues.toString();
                    MainActivity.getInstance().CommonPrint();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        Button BtnCancel = dialog.findViewById(R.id.cancel);
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}
