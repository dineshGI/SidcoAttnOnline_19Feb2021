package com.sidcoparking.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sidcoparking.R;

public class VerifyAlertDialog {
    private Activity mActivity;

    public VerifyAlertDialog(Activity a) {
        this.mActivity = a;
    }

    @SuppressWarnings("InflateParams")
    public void build(String vehicle,String extratime,String hour) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.verify_cutom_alert);
        // set the custom dialog components - text, image and button
        TextView textvehicle = dialog.findViewById(R.id.available);
        TextView texttime = dialog.findViewById(R.id.time);
        TextView texthour = dialog.findViewById(R.id.hour);
        texttime.setText("Additional Charges :"+hour);
        texthour.setText("Extra Hours        :"+extratime);
        textvehicle.setText(vehicle);
         Util.Logcat.e("vehicle:::::::::"+vehicle);
        //image.setImageResource(R.mipmap.ic_launcher);
        Button dialogButton = dialog.findViewById(R.id.okay);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}
