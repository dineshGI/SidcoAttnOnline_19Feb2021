package com.sidcoparking.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.R;
import com.sidcoparking.utils.Util;

import static com.sidcoparking.utils.Util.FCM;

public class SplashActivity extends AppCompatActivity {

    DatabaseHelper TransactionDB;

    private static final long DELAY_TIME = 3000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        getSupportActionBar().hide();

        TransactionDB = new DatabaseHelper(this);

        if (Util.getData("tariffcheck", getApplicationContext()) == null) {
            Util.saveData("tariffcheck", "0", getApplicationContext());
            Util.Logcat.e("tariffcheck" + "0");
        }

        if (Util.getData("finecheck", getApplicationContext()) == null) {
            Util.saveData("finecheck", "0", getApplicationContext());
            Util.Logcat.e("finecheck" + "0");
        }
       /* Intent home = new Intent(getApplicationContext(), ScanPlate.class);
        startActivity(home);*/
        Util.Logcat.e("PUSH :" + FCM);
        if (Util.getData("FCM", getApplicationContext()).equalsIgnoreCase("0")) {
            Intent home = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(home);
            Util.saveData("FCM", "1", getApplicationContext());
            finish();
        } else {
            init();
        }
    }

    private void init() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Util.Logcat.e("terminalid" + Util.getData("terminalid", getApplicationContext()));
                if (Util.getData("terminalid", getApplicationContext()) == null || Util.getData("terminalid", getApplicationContext()).isEmpty()) {
                    Intent home = new Intent(getApplicationContext(), AdminLogin.class);
                    startActivity(home);
                    finish();
                } else {
                    Intent home = new Intent(getApplicationContext(), Login.class);
                    startActivity(home);
                    finish();
                }

                /*Intent home = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(home);
                finish();*/

            }
        }, DELAY_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseInstanceId.getInstance().getInstanceId()

                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(Task<InstanceIdResult> task) {

                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Util.saveData("FCMToken", token, getApplicationContext());

                        //Log and toast
                        Util.Logcat.e("MSG - TOKEN: " + token);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
