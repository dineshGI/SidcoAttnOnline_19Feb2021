package com.sidcoparking.utils;

import android.content.Context;

import com.sidcoparking.Http.HttpUtilNew;

import org.json.JSONException;
import org.json.JSONObject;

import static com.sidcoparking.utils.Util.SUPERVISOR_LOGIN;

public class SupervisorLoginCheck {


    public static String login(final Context con, String username, String password) {

        String sdsa = "";

        Util.saveData("logincheck", "", con.getApplicationContext());
        Util.saveData("loginmsg", "", con.getApplicationContext());
        try {
            String data = "";
            JSONObject obj = new JSONObject();
            obj.put("LoginId", username);
            obj.put("Password", Util.EncryptURL(password));
            data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            String result = HttpUtilNew.makeApiPOSTRequest(SUPERVISOR_LOGIN, params.toString(), con.getApplicationContext());
            JSONObject resobject = new JSONObject(result);
            Util.Decrypt(resobject.getString("Postresponse"));
            Util.Logcat.e("TWO"+ Util.Decrypt(resobject.getString("Postresponse")));
            sdsa = Util.Decrypt(resobject.getString("Postresponse"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //  return Util.getData("logincheck",con.getApplicationContext());
        return sdsa;
    }

}
