package com.sidcoparking.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.GeneratePassword;
import com.sidcoparking.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static com.sidcoparking.utils.Util.MOBILE_API;

public class AdminLogin extends Activity implements View.OnClickListener {

    EditText EdPassword, EdTerminalID;
    Button BtnPassword, BtnTerminal;
    CommonAlertDialog alert;
    LinearLayout Password, Terminal;

    @TargetApi(Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);

        EdPassword = findViewById(R.id.password);
        EdTerminalID = findViewById(R.id.terminalid);

        Password = findViewById(R.id.lypassword);
        Terminal = findViewById(R.id.lyterminalid);
        alert = new CommonAlertDialog(this);

        BtnPassword = findViewById(R.id.btn_password);
        BtnPassword.setOnClickListener(this);
        BtnTerminal = findViewById(R.id.btn_terminal);
        BtnTerminal.setOnClickListener(this);

        if ((ContextCompat.checkSelfPermission(AdminLogin.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(AdminLogin.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(AdminLogin.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(AdminLogin.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(AdminLogin.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                        0);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
       // telephonyManager.getDeviceId();
        Util.saveData("IMEINO", telephonyManager.getDeviceId(), getApplicationContext());
        Util.saveData("tarifdetails", "", getApplicationContext());
        Util.saveData("vehicleid", "", getApplicationContext());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_password:
                if (!EdPassword.getEditableText().toString().isEmpty()) {
                    if (GeneratePassword.GeneratePasswd().equalsIgnoreCase(EdPassword.getEditableText().toString())) {
                        Password.setVisibility(View.GONE);
                        Terminal.setVisibility(View.VISIBLE);
                    } else {
                        alert.build("Incorrect Password");
                    }
                } else {
                    alert.build("Enter Password");
                }

                break;
            case R.id.btn_terminal:
                if (!EdTerminalID.getEditableText().toString().isEmpty()) {
                    ValidateTerminal();
                } else {
                    alert.build("Enter Terminal Id");
                }
                break;

            default:
                break;

        }

    }

    private void ValidateTerminal() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        String imeino = telephonyManager.getDeviceId();
        try {
            JSONObject obj = new JSONObject();
            obj.put("TerminalId", EdTerminalID.getEditableText().toString());
            obj.put("IMEINo", imeino);
            Util.saveData("IMEINO", imeino, getApplicationContext());
            obj.put("TerminalType", "1");
            obj.put("UserId", "0");
            Util.Logcat.e("INPUT:::"+ obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(AdminLogin.this, params.toString(), MOBILE_API + "MbCustomer/UpdateTerminal", new VolleyResponseListener() {
                @Override
                public void onError(String message) {

                    if (message.contains("TimeoutError")) {
                        alert.build(getString(R.string.timeout_error));
                    } else {
                        alert.build(getString(R.string.server_error));
                    }
                    Util.Logcat.e("onError"+ message);
                }

                @Override
                public void onResponse(JSONObject response) {
                     Util.Logcat.e("onResponse"+ response);
                    try {
                        Util.Logcat.e("OUTPUT:::"+ Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {

                            Util.saveData("terminalid", EdTerminalID.getEditableText().toString(), getApplicationContext());
                            Util.Logcat.e("Name"+ EdTerminalID.getEditableText().toString());
                            Intent login = new Intent(AdminLogin.this, Login.class);
                            startActivity(login);
                            finish();

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            alert.build(resobject.getString("StatusDesc"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
