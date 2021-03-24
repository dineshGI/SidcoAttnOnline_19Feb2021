package com.sidcoparking.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.sidcoparking.DataBase.DB_Constant.Datetime;
import static com.sidcoparking.DataBase.DB_Constant.Entrytime;
import static com.sidcoparking.DataBase.DB_Constant.Expirytime;
import static com.sidcoparking.DataBase.DB_Constant.GraceFee;
import static com.sidcoparking.DataBase.DB_Constant.GraceHour;
import static com.sidcoparking.DataBase.DB_Constant.IsGraceHour;
import static com.sidcoparking.DataBase.DB_Constant.Isfoc;
import static com.sidcoparking.DataBase.DB_Constant.MobileNo;
import static com.sidcoparking.DataBase.DB_Constant.ParkingFee;
import static com.sidcoparking.DataBase.DB_Constant.ParkingHour;
import static com.sidcoparking.DataBase.DB_Constant.PaymentMode;
import static com.sidcoparking.DataBase.DB_Constant.ServerTransid;
import static com.sidcoparking.DataBase.DB_Constant.Streetid;
import static com.sidcoparking.DataBase.DB_Constant.Streetname;
import static com.sidcoparking.DataBase.DB_Constant.TransId;
import static com.sidcoparking.DataBase.DB_Constant.Userid;
import static com.sidcoparking.DataBase.DB_Constant.VehNo;
import static com.sidcoparking.DataBase.DB_Constant.VehTypeId;

public class TransactionUpdate extends Service {

    //https://deepshikhapuri.wordpress.com/2016/11/25/service-in-android/
    private Handler mHandler = new Handler();
    public static Timer mTimer = null;
    DatabaseHelper TransactionDB;
    ArrayList<String> ServerId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Util.Logcat.e("Start" + "service");

        TransactionDB = new DatabaseHelper(TransactionUpdate.this);
        ServerId = new ArrayList<>();

        long intervaltime = 1000 * 60 * Integer.parseInt(Util.getData("ServerCallTiming", getApplicationContext()));
        //   long intervaltime = 1000 * 60 * 1;
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, intervaltime);
        Toast.makeText(getApplicationContext(), getString(R.string.app_name) + "\nService Started", Toast.LENGTH_SHORT).show();
    }

    private void callserviceapi(String TransInfo) {

        if (Util.isOnline(getApplicationContext())) {

            try {
                JSONObject obj = new JSONObject();
                obj.put("TerminalId", Util.getData("terminalid", getApplicationContext()));
                obj.put("TransactionInfo", TransInfo);
                Util.Logcat.e("BULK UPDATE REQUEST" + obj.toString());
                String data = Util.EncryptURL(obj.toString());

                JSONObject params = new JSONObject();
                params.put("Getrequestresponse", data);
                CallApi.postResponseNopgrss(getApplicationContext(), params.toString(), Util.ADD_PARKING, new VolleyResponseListener() {
                    @Override
                    public void onError(String message) {
                        Util.Logcat.e("onError:" + message);
                        ServerId.clear();
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        Util.Logcat.e("onResponse" + response);
                        try {
                            Util.Logcat.e("BULK UPDATE RESPONSE:::" + Util.Decrypt(response.getString("Postresponse")));
                            JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));
                            if (resobject.getString("Status").equalsIgnoreCase("0")) {
                                Util.Logcat.e("BULK UPDATE" + "SUCCESS");
                                //updates
                                UpdateServerID(resobject.getString("ServerGenNo"));

                            } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                                Util.Logcat.e("BULK UPDATE" + "FAILED");
                                ServerId.clear();
                            }
                            //connect = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.app_name) + "\nPlease check your internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    private void UpdateServerID(String serverGenNo) {
        Cursor res = TransactionDB.getAllData();
        if (res.getCount() == 0) {
            // show message
            Util.Logcat.e("Nothing:::" + "Found");
            return;
        }

        while (res.moveToNext()) {

            if (ServerId.contains(Util.Decrypt(res.getString(TransId)))) {

                boolean isUpdate = TransactionDB.updateExittime(res.getString(TransId), res.getString(VehTypeId), res.getString(VehNo), res.getString(Streetid), res.getString(ParkingHour),
                        res.getString(ParkingFee), res.getString(IsGraceHour), res.getString(GraceHour), res.getString(GraceFee), res.getString(Isfoc),
                        res.getString(Userid), res.getString(Entrytime), res.getString(Expirytime), Util.EncryptURL(serverGenNo), res.getString(PaymentMode), res.getString(MobileNo), res.getString(Streetname), res.getString(Datetime));
                Util.Logcat.e("updated server " + Util.Decrypt(res.getString(TransId)) + ":::" + serverGenNo);
                Util.Logcat.e("PaymentModeID:::" + Util.Decrypt(res.getString(PaymentMode)));
                Util.Logcat.e("MobileNo:::" + Util.Decrypt(res.getString(MobileNo)));

                if (isUpdate == true)
                    Toast.makeText(getApplicationContext(), "Data Update", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Data not Updated", Toast.LENGTH_LONG).show();
            } else {
                //  Util.Logcat.e("Nothing Matched:::", "with 0 ServerID");
            }
        }

        ServerId.clear();
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    servicecall();
                }
            });
        }
    }

    public void servicecall() {
        Cursor res = TransactionDB.getAllData();
        StringBuffer buffer = new StringBuffer();

        while (res.moveToNext()) {

            if (Util.DECODE64(res.getString(ServerTransid)).equalsIgnoreCase("0")) {
                ServerId.add(Util.DECODE64(res.getString(TransId)));
                buffer.append(Util.DECODE64(res.getString(TransId)) + "~" +
                        Util.DECODE64(res.getString(VehTypeId)) + "~" +
                        Util.DECODE64(res.getString(VehNo)) + "~" +
                        Util.DECODE64(res.getString(Streetid)) + "~" +
                        Util.DECODE64(res.getString(ParkingHour)) + "~" +
                        Util.DECODE64(res.getString(ParkingFee)) + "~" +
                        Util.DECODE64(res.getString(IsGraceHour)) + "~" +
                        Util.DECODE64(res.getString(GraceHour)) + "~" +
                        Util.DECODE64(res.getString(GraceFee)) + "~" +
                        Util.DECODE64(res.getString(Isfoc)) + "~" +
                        Util.DECODE64(res.getString(Userid)) + "~" +
                        Util.DECODE64(res.getString(Entrytime)) + "~" +
                        Util.DECODE64(res.getString(Expirytime)) + "~" +
                        Util.DECODE64(res.getString(PaymentMode)) + "~" +
                        Util.DECODE64(res.getString(MobileNo)) + "~" +
                        Util.DECODE64(res.getString(Datetime)) + "|");
                        /*Util.DECODE64(res.getString(SerialNo)) + "~" +
                        Util.DECODE64(res.getString(PrevTransId)) + "~" +
                        Util.DECODE64(res.getString(PrevEntryTime)) + "~" +
                        Util.DECODE64(res.getString(Day)) + "|");*/
                // Util.Logcat.e("Servicecall PaymentMode"+res.getString(PaymentMode));
                // Util.Logcat.e("MobileNo"+res.getString(MobileNo));
                // Util.Logcat.e("TIMESTAMP"+res.getString(Datetime));
            } else {
                // Util.Logcat.e("Condition ", "not matched ");
            }
        }

        Util.Logcat.e("SERVER UPDATE buffer" + buffer.toString());
        Util.Logcat.e("TransId:" + ServerId);

        if (!buffer.toString().isEmpty()) {
            callserviceapi(buffer.toString());
        } else {
            Util.Logcat.e("No Data " + "in DB");
        }
    }
}
