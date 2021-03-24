package com.sidcoparking.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.Util;
import com.telpo.tps550.api.printer.UsbThermalPrinter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;

import static com.sidcoparking.utils.Util.FORGET_PASSWORD;
import static com.sidcoparking.utils.Util.GET_FINETYPE;
import static com.sidcoparking.utils.Util.GET_TARIFF;
import static com.sidcoparking.utils.Util.LOGIN;
import static com.sidcoparking.utils.Util.STATUS_UPDATE;

public class Login extends Activity {

    EditText EdUsername, EdPassword, EdTerminalId;
    Button BtnLogin;
    String osName, device, latitude, longitude;
    CommonAlertDialog alert;
    GpsTracker gpsTracker;
    TextView TxtVersion, TxtForgetPassword;

    CheckBox chkbox;
    ProgressDialog pd;
    UsbThermalPrinter mUsbThermalPrinter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        if ((ContextCompat.checkSelfPermission(Login.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                        0);
            }
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        mUsbThermalPrinter = new UsbThermalPrinter(Login.this);
        pd = new ProgressDialog(this);
        EdUsername = findViewById(R.id.username);
        EdPassword = findViewById(R.id.password);
        EdTerminalId = findViewById(R.id.terminalid);
        TxtForgetPassword = findViewById(R.id.forgetpassword);
        EdTerminalId.setText(Util.getData("terminalid", getApplicationContext()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AutofillManager autofillManager = getSystemService(AutofillManager.class);
            autofillManager.disableAutofillServices();
            EdUsername.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            EdPassword.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        EdUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (EdUsername.getEditableText().toString().equalsIgnoreCase("admin")) {
                    Intent login = new Intent(Login.this, AdminLogin.class);
                    startActivity(login);
                }
            }
        });

        BtnLogin = findViewById(R.id.btn_login);
        TxtVersion = findViewById(R.id.version);
        TxtVersion.setText(Util.app_version_name);
        alert = new CommonAlertDialog(this);
        chkbox = findViewById(R.id.rememberme);
        gpsTracker = new GpsTracker(Login.this);
        if (gpsTracker.canGetLocation()) {
            latitude = String.valueOf(gpsTracker.getLatitude());
            longitude = String.valueOf(gpsTracker.getLongitude());
            //Util.Logcat.e("lat:::", String.valueOf(latitude));
            //Util.Logcat.e("long:::", String.valueOf(longitude));
        }

        devicedetails();

        chkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    if (!Util.getData("loginuser", getApplicationContext()).isEmpty()) {
                        EdUsername.setText(Util.getData("loginuser", getApplicationContext()));
                        EdPassword.setText(Util.getData("loginpass", getApplicationContext()));
                    }

                } else {
                    EdUsername.setText("");
                    EdPassword.setText("");
                }

            }
        });

        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EdUsername.getEditableText().toString().isEmpty()) {
                    alert.build(getResources().getString(R.string.enter_username));
                } else //login();
                    if (EdPassword.getEditableText().toString().isEmpty()) {
                        alert.build(getResources().getString(R.string.enter_password));
                    } else {

                        login();
                    }
            }
        });

        TxtForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!EdUsername.getEditableText().toString().isEmpty()) {
                    Forgetpassword();
                } else {
                    alert.build(getResources().getString(R.string.enter_username));
                }
            }
        });

    }

   /* private void update() {
        //https://github.com/amitshekhariitbhu/Fast-Android-Networking
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        String url = "http://14.141.212.203/isamanapk/sampleapk.apk";
        String dirPath = "/mnt/sdcard/Download";
        String fileName = "sampleapk.apk";
        AndroidNetworking.download(url, dirPath, fileName)
                .setTag("downloadTest")
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        // do anything with progress
                        pd.show();
                        pd.setCancelable(false);
                        pd.setMessage("Updating App. Please Wait...");
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // do anything after completion
                        pd.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File("/mnt/sdcard/Download/sampleapk.apk");
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle errora
                        alert.build("App Update Failed");

                    }
                });
    }*/

    private void SaveFineDetails(String userid) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", userid);
            Util.Logcat.e("FINE:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponseNopgrss(Login.this, params.toString(), GET_FINETYPE, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("TARIFF:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            //_VechicleTraiff
                            Util.saveData("finedetails", resobject.toString(), getApplicationContext());
                            Util.saveData("finecheck", "1", getApplicationContext());
                            Util.Logcat.e("FINETYPE SAVING" + "SUCCESS");
                            UpdateStatus("2");
                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            Util.Logcat.e("FINETYPE SAVING" + "FAILED");
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

    private void GetTariff() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("TerminalId", Util.getData("terminalid", getApplicationContext()));
            obj.put("UserId", "0");
            Util.Logcat.e("GET_TARIFF:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponseNopgrss(Login.this, params.toString(), GET_TARIFF, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("GET_TARIFF:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            //_VechicleTraiff

                            Util.saveData("tarifdetails", resobject.toString(), getApplicationContext());
                            Util.saveData("tariffcheck", "1", getApplicationContext());

                            UpdateStatus("1");
                            Util.Logcat.e("tarifdetails" + Util.getData("tarifdetails", getApplicationContext()));
                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            Util.Logcat.e("TARIFF SAVING" + "FAILED");
                        }
                        //nabeelahmednwa@okicici
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdateStatus(final String flag) {

        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", Util.getData("UserId", getApplicationContext()));
            obj.put("TerminalId", Util.getData("terminalid", getApplicationContext()));
            obj.put("UpdateFlg", flag);
            //obj.put("UserId", "1");
            Util.Logcat.e("FINE:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponseNopgrss(Login.this, params.toString(), STATUS_UPDATE, new VolleyResponseListener() {

                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("BasicInfo:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            //_VechicleTraiff
                            if (flag.equalsIgnoreCase("1")) {
                                Util.Logcat.e("TARIFF UPDATE" + "SUCCESS");
                            } else if (flag.equalsIgnoreCase("2")) {
                                Util.Logcat.e("FINE UPDATE" + "SUCCESS");
                            }

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            Util.Logcat.e("SERVER UPDATE" + "FAILED");
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

    private void login() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("LoginId", EdUsername.getEditableText().toString());
            obj.put("Password", Util.EncryptURL(EdPassword.getEditableText().toString()));
            obj.put("DeviceType", "2");
            obj.put("TerminalId", Util.getData("terminalid", getApplicationContext()));
            obj.put("IMEINumber", Util.getData("IMEINO", getApplicationContext()));
            obj.put("DeviceInfo", device);
            Util.saveData("DeviceInfo", device, getApplicationContext());
            obj.put("Version", Util.app_version);
            obj.put("Latitude", latitude);
            obj.put("Longitude", longitude);
            obj.put("FcmToken", Util.getData("FCMToken", getApplicationContext()));

            Util.Logcat.e("FCMToken:::" + Util.getData("FCMToken", getApplicationContext()));
            Util.Logcat.e("INPUT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);

            CallApi.postResponse(Login.this, params.toString(), LOGIN, new VolleyResponseListener() {
                @Override
                public void onError(String message) {

                    if (message.contains("TimeoutError")) {
                        alert.build(getString(R.string.timeout_error));
                    } else {
                        alert.build(getString(R.string.server_error));
                    }
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("OUTPUT:::" + Util.Decrypt(response.getString("Postresponse")));
                        final JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {

                            Util.saveData("UserId", resobject.getString("ATDId"), getApplicationContext());
                            Util.saveData("LoginId", resobject.getString("LoginId"), getApplicationContext());
                            Util.saveData("Name", resobject.getString("Name"), getApplicationContext());
                            //IsVehicleTypeUpdated,IsTariffUpdated
                            Util.saveData("IsVehicleTypeUpdated", resobject.getString("IsVehicleTypeUpdated"), getApplicationContext());
                            Util.saveData("IsTariffUpdated", resobject.getString("IsTariffUpdated"), getApplicationContext());
                            Util.saveData("ServerCallTiming", resobject.getString("ServerCallTiming"), getApplicationContext());
                            Util.saveData("WorkStatus", resobject.getString("SettlementId"), getApplicationContext());
                            Util.saveData("LogId", resobject.getString("LogId"), getApplicationContext());
                            Util.saveData("RoleId", resobject.getString("RoleId"), getApplicationContext());
                            Util.saveData("RoleName", "", getApplicationContext());
                            Util.Logcat.e("Name" + resobject.getString("Name"));
                            Util.Logcat.e("tarifdetails" + Util.getData("tarifdetails", getApplicationContext()));

                            if (resobject.getString("IsTariffUpdated").equalsIgnoreCase("1") || Util.getData("tarifdetails", getApplicationContext()).isEmpty()) {
                                GetTariff();
                            }

                            if (chkbox.isChecked() == true) {
                                Util.saveData("loginuser", EdUsername.getEditableText().toString(), getApplicationContext());
                                Util.saveData("loginpass", EdPassword.getEditableText().toString(), getApplicationContext());
                            }

                            //get vehicle details
                           /* if (resobject.getString("IsVehicleTypeUpdated").equalsIgnoreCase("1") || Util.getData("vehicleid", getApplicationContext()).isEmpty()) {
                                SaveVehicleDetails(resobject.getString("ATDId"));
                            }*/

                            if (resobject.getString("IsFineUpdated").equalsIgnoreCase("1") || Util.getData("finedetails", getApplicationContext()).isEmpty()) {
                                SaveFineDetails(resobject.getString("ATDId"));
                            }

                            pd.show();
                            pd.setMessage("Loading...");
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    pd.dismiss();

                                    try {
                                        if (!resobject.getString("VersionChk").equalsIgnoreCase("1")) {
                                            Intent home = new Intent(Login.this, MainActivity.class);
                                            startActivity(home);
                                            finish();

                                        } else {
                                            AlertDialog.Builder dlg = new AlertDialog.Builder(Login.this, R.style.alertDialog);
                                            // dlg.setTitle("App Update");
                                            dlg.setMessage("Kindly Update New version");
                                            dlg.setCancelable(false);
                                            dlg.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    try {
                                                        update(resobject.getString("Downloadlink"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            dlg.show();
                                        }
                                    } catch (JSONException e) {

                                    }

                                }
                            }, 3000);

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

    private void update(String url) {

        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        String dirPath = "/mnt/sdcard/Download";

        String fileName = "sidcoparking.apk";

        AndroidNetworking.download(url, dirPath, fileName)
                .setTag("App Update")
                .setPriority(Priority.HIGH)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        // do anything with progress
                        pd.show();
                        pd.setCancelable(false);
                        pd.setMessage("Updating New Version App. Please Wait...");
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // do anything after completion
                        pd.dismiss();

                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sidcoparking.apk");

                        if (file.exists()) {

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri uri = FileProvider.getUriForFile(Login.this, "com.sidcoparking", file);
                                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            }
                            startActivity(intent);
                        } else {
                            Toast.makeText(Login.this, "ّFile not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Util.Logcat.e(String.valueOf(error));
                        pd.dismiss();
                        alert.build("App Update Failed ! Contact Admin");
                    }
                });
    }

    private void devicedetails() {

        String device_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        try {
            Field[] fields = Build.VERSION_CODES.class.getFields();
            osName = "Android " + fields[Build.VERSION.SDK_INT + 1].getName();
        } catch (ArrayIndexOutOfBoundsException e) {
            Util.Logcat.e("ArrayIndexOutOfBounds:" + e);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.getDeviceId();
        // Util.Logcat.e("IMEI No" + telephonyManager.getDeviceId());
        Util.saveData("IMEINO", telephonyManager.getDeviceId(), getApplicationContext());

        device = device_id + "," + "null" + "," + osName + "," + Build.VERSION.RELEASE + "," + Build.SERIAL + "," + Build.MANUFACTURER + "," + Build.MODEL + "," + "null" + "," + "null" + "," + "null" + "," + latitude + "," + longitude + "," + "null" + ",";
        Util.Logcat.e("device>" + device);
    }

    private void Forgetpassword() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("LoginId", EdUsername.getEditableText().toString());
            obj.put("Password", "");
            Util.Logcat.e("Forgetpassword:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(Login.this, params.toString(), FORGET_PASSWORD, new VolleyResponseListener() {
                @Override
                public void onError(String message) {

                    if (message.contains("TimeoutError")) {
                        alert.build(getString(R.string.timeout_error));
                    } else {
                        alert.build(getString(R.string.server_error));
                    }
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("FORGET PASSWORD:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            Util.Logcat.e("FORGET PASS" + "SUCESS");
                            alert.build(resobject.getString("StatusDesc"));

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            Util.Logcat.e("FORGET PASS" + "FAILED");
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
