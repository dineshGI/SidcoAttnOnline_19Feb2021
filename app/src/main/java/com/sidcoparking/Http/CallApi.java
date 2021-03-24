package com.sidcoparking.Http;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sidcoparking.utils.Util;
import com.sidcoparking.R;
import com.sidcoparking.interfaces.VolleyResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class CallApi {

    public static void getResponse(final Context context, String Api, final VolleyResponseListener listener) {
        RequestQueue requestQueue;
        final ProgressDialog loading = ProgressDialog.show(context, "Loading...", "Please wait...", false, false);

        JsonObjectRequest req = new JsonObjectRequest(Api, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loading.dismiss();
                        listener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                listener.onError(error.toString());

            }
        }) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(15000,
                //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Creating request queue
        requestQueue = Volley.newRequestQueue(context);

        //Adding request to the queue
        requestQueue.add(req);
    }

    public static void postResponse(final Context context, String mObject, String Api, final VolleyResponseListener listener) throws JSONException {

         Util.Logcat.e("REQUEST URL:"+ Api);
        if (Util.isOnline(context.getApplicationContext())) {
            //final ProgressDialog loading = ProgressDialog.show(context, "Loading...", "Please wait...", false, false);
            final ProgressDialog loading = new ProgressDialog(context, R.style.alertDialog);
            loading.setMessage("Loading...");
            loading.setCancelable(false);
            loading.show();
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, Api, new JSONObject(mObject),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            loading.dismiss();
                            listener.onResponse(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loading.dismiss();
                    listener.onError(error.toString());

                }
            }) {

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        String jsonString = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject(jsonString),
                                HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };

            req.setRetryPolicy(new DefaultRetryPolicy(1000 * 60 * 2,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            //Creating request queue
            RequestQueue requestQueue = Volley.newRequestQueue(context);

            //Adding request to the queue
            requestQueue.add(req);
        } else {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.alertDialog);
            alertDialogBuilder.setMessage(R.string.check_internet);
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            //  Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    public static void postResponseNopgrss(final Context context, String mObject, String Api, final VolleyResponseListener listener) throws JSONException {

         Util.Logcat.e("\n\nREQUEST URL:"+ Api);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, Api, new JSONObject(mObject),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error.toString());

            }
        }) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(1000 * 60 * 2,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Creating request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //Adding request to the queue
        requestQueue.add(req);

    }

}
