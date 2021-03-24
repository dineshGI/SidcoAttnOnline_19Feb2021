package com.sidcoparking.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.content.Context.MODE_PRIVATE;

//import junit.framework.Assert;

public class Util {

    //Last edit 01March2021
    //Added online parking
    //New Printer Version

    public static boolean printversn = true;
    public static String[] url_name = {"E", "S", "L"};
    public static String app_version = "1.0.0";
    public static String[] urlarray = {"http://14.141.212.203/guindyparkingapi/", "http://115.110.148.86/sidcoparkingapi/"};

   /* Attendant:
      ----------
            900002
            9000000002
            581479

    Supervisor:
    -----------
            Selva
            9000000000
            123456*/

    public static int url_type = 0;
    public static String MOBILE_API = String.valueOf(urlarray[url_type]);
    public static String app_version_name = "V " + app_version + " " + url_name[url_type];

    //LOGIN
    public static String LOGIN = MOBILE_API + "Attendant/AttendantLogin";
    public static String SUPERVISOR_LOGIN = MOBILE_API + "MbCustomer/SupervisorLogin";
    public static String GET_TARIFF = MOBILE_API + "MbCustomer/GetAppVehicleTariff";
    public static String EXPIRY_VEHICLE = MOBILE_API + "MbCustomer/GetExpVehicle";
    public static String ADD_PARKING = MOBILE_API + "MbCustomer/AddParkingDetails_V3";
    public static String GET_FINETYPE = MOBILE_API + "MbCustomer/GetFineCharges";
    public static String CHANGEPASSWORD = MOBILE_API + "MbCustomer/UPDAttendantChangePassword";
    public static String FORGET_PASSWORD = MOBILE_API + "MbCustomer/AttendantForgetPasswordReset";
    public static String CHECK_VEHICLE = MOBILE_API + "MbCustomer/VehicleNoVerify";
    public static String STATUS_UPDATE = MOBILE_API + "MbCustomer/BasicInfo";
    public static String LOGOUT = MOBILE_API + "MbCustomer/AppLogout";
    public static String GET_SETTLEMENT = MOBILE_API + "MbCustomer/AppGETSettlement";

    public static String WORK_STATUS = MOBILE_API + "MbCustomer/INSShiftStatus";
    public static String GET_ATTENDERLIST = MOBILE_API + "MbCustomer/GETAttendantList";
    public static String SETTLEMENT_COLLECTION = MOBILE_API + "MbCustomer/UPDSettlementCollection";

    public static String ADD_FINE_ENTRY = MOBILE_API + "MbCustomer/AddFineEntry_V1";
    public static String PAYMENT_MODE = MOBILE_API + "MbCustomer/GETPaymentMode";
    public static String GET_HOURS = MOBILE_API + "MbCustomer/GetTiming";
    public static String GET_MBSETTLEMENT_DETAILS = MOBILE_API + "Parking/GetMBSettlementDetails";
    public static String GET_DASHBOARD = MOBILE_API + "MbCustomer/GetAppDashboard";
    public static String GET_ATTD_STREET_DETAILS = MOBILE_API + "MbCustomer/GetAttenderStreetDetails";
    public static String GET_CUSTOMER_VEHICLE_DETAILS = MOBILE_API + "MbCustomer/GetCustomerAppVehicleDetails";
    public static String BUILDING_MASTER = MOBILE_API + "Parking/GetBuildingMaster";
    public static String ONLINE_PARKING = MOBILE_API + "MbCustomer/AddSlotParkingDetails";
    public static String ONLINE_PRINT = MOBILE_API + "MbCustomer/GetVehicleNoReprint";
    public static String SCAN_VALUE = "";
    public static boolean FCM = false;
    private SharedPreferences preferences;

    public Util(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    public synchronized static boolean isFirstLaunch(Context context) {

        boolean launchFlag = false;
        SharedPreferences pref = context.getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("install", false);
        return launchFlag;

    }

    public static final class Operations {
        private Operations() throws InstantiationException {
            throw new InstantiationException("This class is not for instantiation");
        }

        /**
         * Checks to see if the device is online before carrying out any operations.
         *
         * @return
         */

        public static boolean isOnline(Context context) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
            return false;
        }
    }

    public static String encode(@NonNull String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            //  Assert.fail("Uri string cannot be empty!");
            return uriString;
        }
        // getQueryParameterNames is not exist then cannot iterate on queries
        if (Build.VERSION.SDK_INT < 11) {
            return uriString;
        }

        Pattern allowedUrlCharacters = Pattern.compile("([A-Za-z0-9_.~:/?\\#\\[\\]@!$&'()*+,;" +
                "=-]|%[0-9a-fA-F]{2})+");
        Matcher matcher = allowedUrlCharacters.matcher(uriString);

        String validUri = null;
        if (matcher.find()) {
            validUri = matcher.group();
        }

        if (TextUtils.isEmpty(validUri) || uriString.length() == validUri.length()) {
            return uriString;
        }

        // The uriString is not encoded. Then recreate the uri and encode it this time
        Uri uri = Uri.parse(uriString);
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority());
        for (String path : uri.getPathSegments()) {
            uriBuilder.appendPath(path);
        }
        for (String key : uri.getQueryParameterNames()) {
            uriBuilder.appendQueryParameter(key, uri.getQueryParameter(key));
        }

        String correctUrl = uriBuilder.build().toString();
        return correctUrl;

    }

    public static void hideKeypad(Context context, View view) {
        final InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void saveData(String key, String value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("smartparking", Activity.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getData(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("smartparking", Activity.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static void clearSession(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("smartparking", Activity.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches())
            return true;
        else
            return false;
    }

    public static boolean isOnline(Context con) {
        ConnectivityManager cm = (ConnectivityManager) con
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.i("netInfo", "" + netInfo);
            return true;
        }
        return false;
    }

    @SuppressLint("NewApi")
    public static String Encrypt(String data) {

        String strret = "";
        try {
            byte[] sharedkey = "A1234&ABCDE/98745#000078".getBytes();
            byte[] sharedvector = {8, 7, 5, 6, 4, 1, 2, 3, 18, 17, 15, 16, 14, 11, 12, 13};
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sharedkey, "AES"), new IvParameterSpec(sharedvector));
            byte[] encrypted = c.doFinal(data.getBytes("UTF-8"));
            // strret = Base64.getEncoder().encodeToString(encrypted);
            strret = Base64.encodeToString(encrypted, Base64.DEFAULT);
            strret = strret.replace("\n", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("Encrypt without ULR", strret);
        return strret;
    }

    @SuppressLint("NewApi")
    public static String EncryptURL(String data) {

        String strret = "";
        try {
            byte[] sharedkey = "A1234&ABCDE/98745#000078".getBytes();
            byte[] sharedvector = {8, 7, 5, 6, 4, 1, 2, 3, 18, 17, 15, 16, 14, 11, 12, 13};
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sharedkey, "AES"), new IvParameterSpec(sharedvector));
            byte[] encrypted = c.doFinal(data.getBytes("UTF-8"));
            // strret = Base64.getEncoder().encodeToString(encrypted);
            strret = Base64.encodeToString(encrypted, Base64.DEFAULT);
            strret = strret.replace("\n", "");
            strret = URLEncoder.encode(strret, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.e("Encrypted Data:", strret);
        return strret;
    }

    public static String Decrypt(String data) {
        String decrypt = "";
        byte[] sharedkey = "A1234&ABCDE/98745#000078".getBytes();
        byte[] sharedvector = {8, 7, 5, 6, 4, 1, 2, 3, 18, 17, 15, 16, 14, 11, 12, 13};

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sharedkey, "AES"), new IvParameterSpec(sharedvector));
            byte[] decrypted = c.doFinal(Base64.decode((URLDecoder.decode(data, "UTF-8")), Base64.DEFAULT));
            decrypt = new String(decrypted, "UTF-8");

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        System.out.print("DECRYPT" + decrypt);
        return decrypt;

    }

    public static String getdatetime() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String datetime = dateformat.format(c.getTime());
        return datetime.replace("-", "/");

    }

    public static String getdateonly() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
        String datetime = dateformat.format(c.getTime());
        return datetime.replace("-", "/");
    }

    public static String readjson(String filepath) throws IOException {

        File yourFile = new File(filepath);
        Log.e("readfile", String.valueOf(yourFile));
        FileInputStream stream = new FileInputStream(yourFile);
        String jString = null;
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            jString = Charset.defaultCharset().decode(bb).toString();
            Log.e("output", jString);
        } finally {
            stream.close();
        }
        return jString;
    }

    public static String GenerateTransactionId(String terminalid) {
        try {
            String key = "", temp = "";
            Calendar c = Calendar.getInstance();
            //int year = 2000;
            int year = c.get(Calendar.YEAR);
            int hr = c.get(Calendar.HOUR_OF_DAY);
            int min = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);
            int julDay = c.get(Calendar.DAY_OF_YEAR);

            temp = Integer.toString(year);
            if (temp.length() == 1) {
                key += "0";
            }
            key += temp;
            temp = Integer.toString(julDay);
            for (int i = 0; i < (3 - temp.length()); i++) {
                key += "0";
            }
            key += temp;
            int TimeinSecs = (hr * 60 * 60) + (min * 60) + sec;
            temp = Integer.toString(TimeinSecs);
            for (int i = 0; i < (5 - temp.length()); i++) {
                key += "0";
            }
            key += temp;

            long lKey = Long.parseLong(key);
            String hex1 = LongtoHexString(lKey);
            hex1 = hex1.toUpperCase(Locale.ENGLISH);

            return terminalid + hex1;
        } catch (Exception e) {
        }
        return "";
    }

    static String LongtoHexString(long dc) {
        long a = 0, b = 0;
        StringBuffer hx = new StringBuffer("");
        while (dc > 15) {
            a = dc / 16;
            b = dc % 16;
            dc = a;
            if (b < 10) {
                hx = hx.append(b);
            } else if (b > 9) {
                if (b == 10) hx = hx.append("A");
                else if (b == 11) hx = hx.append("B");
                else if (b == 12) hx = hx.append("C");
                else if (b == 13) hx = hx.append("D");
                else if (b == 14) hx = hx.append("E");
                else if (b == 15) hx = hx.append("F");
            }
        }

        if (a == 10) hx = hx.append("A");
        else if (a == 11) hx = hx.append("B");
        else if (a == 12) hx = hx.append("C");
        else if (a == 13) hx = hx.append("D");
        else if (a == 14) hx = hx.append("E");
        else if (a == 15) hx = hx.append("F");
        else {
            hx = hx.append(a);
        }
        return (hx.reverse()).toString();
    }

    public static String DECODE64(String data) {

        String decrypt = "";
        byte[] sharedkey = "A1234&ABCDE/98745#000078".getBytes();
        byte[] sharedvector = {8, 7, 5, 6, 4, 1, 2, 3, 18, 17, 15, 16, 14, 11, 12, 13};

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sharedkey, "AES"), new IvParameterSpec(sharedvector));
            byte[] decrypted = c.doFinal(Base64.decode((URLDecoder.decode(data, "UTF-8")), Base64.DEFAULT));
            decrypt = new String(decrypted, "UTF-8");

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        //  System.out.print("DECRYPT" + decrypt);
        return decrypt;
    }

    public static String parkingtime(Integer hour) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, hour);
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String datetime = dateformat.format(c.getTime());
        // Log.e("TIME::::", datetime.replace("-", "/"));
        return datetime.replace("-", "/");
    }

    public static String addgrace(String date, Integer mins) {
        String output = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.MINUTE, mins); // Adding 5 days
            output = sdf.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Log.e("exptime added estra tim", output);
        return output.replace("-", "/");
    }

    public static String showextra(String oldtime, String newtime) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(oldtime);
            d2 = format.parse(newtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long HOURS = 0;
        // Log.e("diffHours before fine", String.valueOf(HOURS));
        if (diffDays >= 1 && diffMinutes > 1) {
            HOURS = diffHours + (diffDays * 24) + 1;
            // Log.e("1diffHours after fine", String.valueOf(HOURS));
        } else if (diffDays >= 1 && diffMinutes < 1) {
            HOURS = diffHours + (diffDays * 24);
            // Log.e("2diffHours after fine", String.valueOf(HOURS));
        } else if (diffHours >= 1 && diffMinutes > 1) {
            HOURS = diffHours + 1;
            // Log.e("3diffHours after fine ", String.valueOf(HOURS));
        } else if (diffHours < 1 && diffMinutes > 1) {
            HOURS = 1;
            // Log.e("4diffHours after fine", String.valueOf(HOURS));
        } else {
            HOURS = diffHours;
            // Log.e("5diffHours after fine", String.valueOf(HOURS));
        }
        return String.valueOf(HOURS);
    }

    public static String showextrabyminutes(String oldtime, String newtime) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(oldtime);
            d2 = format.parse(newtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        //Log.e("DIFF minutes",String.valueOf(minutes));
        return String.valueOf(minutes).replaceAll("-", "");

    }

    public static String showparkingtime(String oldtime, String newtime) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(oldtime);
            d2 = format.parse(newtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        //Log.e("date",  String.format("%02d", diffHours) + ":" + String.format("%02d", diffMinutes)+ ":" + String.format("%02d", diffSeconds) + " " + "Hours");
        return String.format("%02d", diffHours) + ":" + String.format("%02d", diffMinutes) + ":" + String.format("%02d", diffSeconds);
    }

    public static String getdate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
        String datetime = dateformat.format(c.getTime());
        return datetime.replace("-", "");
    }

    public static String sumdate(String date, Integer hour) {
        // Log.e("exptime added estra tim", date);
        String output = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.HOUR, hour); // Adding 5 days
            output = sdf.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Log.e("exptime added estra tim", output);
        return output.replace("-", "/");
    }

    public static class Logcat {
        private static final String TAG = "ATTENDER";

        public static void e(String msg) {
            if (url_type == 0) {
                Log.e(TAG, msg);
            }
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static String getonlyDD() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd");
        String datetime = dateformat.format(c.getTime());
        return datetime.replace("-", "");
    }

    public static String GenerateSerial(Context con) {
        if (!Util.getData("day", con.getApplicationContext()).equalsIgnoreCase(Util.getonlyDD())) {
            Util.saveData("startSERIAL", "1000", con.getApplicationContext());
        }
        int DATE = Integer.parseInt(Util.getData("startSERIAL", con.getApplicationContext())) + 1;
        Util.saveData("startSERIAL", String.valueOf(DATE), con.getApplicationContext());
        String temp = Util.getData("terminalid", con.getApplicationContext()) + Util.getonlyDD() + DATE;
        return temp;
    }
}

