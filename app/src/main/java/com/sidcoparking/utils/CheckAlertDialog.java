package com.sidcoparking.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sidcoparking.R;

public class CheckAlertDialog {
    private Activity mActivity;

    public CheckAlertDialog(Activity a) {
        this.mActivity = a;
    }

    @SuppressWarnings("InflateParams")
    public void build(String status,String duration,String amt,String entry,String exit,String parkedhour) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.check_alert);
        // set the custom dialog components - text, image and button

        TextView Tvstatus = dialog.findViewById(R.id.status);
        TextView Tvextrahour = dialog.findViewById(R.id.extrahour);
        TextView Tvaddition = dialog.findViewById(R.id.addition);
        TextView Tventry = dialog.findViewById(R.id.entry);
        TextView Tvexit = dialog.findViewById(R.id.exit);
        TextView Tvparkinghour = dialog.findViewById(R.id.parking_hour);
        Tvstatus.setText(status);
        Tvextrahour.setText("Extra Hour :"+duration);
        Tvaddition.setText("Additional Charges :"+amt);
        Tventry.setText("Entry :"+entry);
        Tvexit.setText("Expiry :"+exit);
        Tvparkinghour.setText("Duration :"+parkedhour);
        // image.setImageResource(R.mipmap.ic_launcher);
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
