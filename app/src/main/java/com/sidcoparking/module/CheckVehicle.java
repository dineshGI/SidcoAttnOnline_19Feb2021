package com.sidcoparking.module;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CheckAlertDialog;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sidcoparking.utils.Util.CHECK_VEHICLE;

public class CheckVehicle extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //https://api.mlab.com/api/1/databases/blindapp/collections/beacons?apiKey=EVDAtEeJwaIMAwOpjOOxdN2IiMmfLDJI

    private String mParam1;
    private String mParam2;

    ProgressDialog progressDialog;
    Button BtnSubmit;
    CommonAlertDialog alert;
    CheckAlertDialog Checkalert;
    Spinner SpinStreet, SpinVehicle;
    String TARIFF, StreetId, VehicleTypeId;
    JSONArray streetarray;
    List<String> spinnerlist;
    EditText EdVehNo;

    private OnFragmentInteractionListener mListener;

    public CheckVehicle() {
    }

    public static CheckVehicle newInstance(String param1, String param2) {
        CheckVehicle fragment = new CheckVehicle();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.check_vehicle, container, false);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        return true;
                    }
                }
                return false;
            }
        });
        setHasOptionsMenu(true);

        TARIFF = Util.getData("tarifdetails", getActivity().getApplicationContext());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        alert = new CommonAlertDialog(getActivity());
        Checkalert = new CheckAlertDialog(getActivity());
        streetarray = new JSONArray();
        spinnerlist = new ArrayList<>();

        EdVehNo = rootView.findViewById(R.id.vehicleno);
        BtnSubmit = rootView.findViewById(R.id.btn_check);
        BtnSubmit.setOnClickListener(this);

        SpinVehicle = rootView.findViewById(R.id.vehicletype_spinner);
        SpinVehicle.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        String type[] = {"Car", "Two Wheeler", "Auto Rickshaw", "Cycle Rickshaw", "Bus", "HCV", "LCV", "Mini Bus"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_textview, type);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        SpinVehicle.setAdapter(adapter);

        SpinVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                SpinVehicle.getSelectedItem().toString();
                if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("Car")) {
                    VehicleTypeId = "1";
                } else if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("Two Wheeler")) {
                    VehicleTypeId = "2";
                } else if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("Auto Rickshaw")) {
                    VehicleTypeId = "3";
                } else if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("Cycle Rickshaw")) {
                    VehicleTypeId = "4";
                } else if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("Bus")) {
                    VehicleTypeId = "5";
                } else if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("HCV")) {
                    VehicleTypeId = "6";
                } else if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("LCV")) {
                    VehicleTypeId = "7";
                } else if (SpinVehicle.getSelectedItem().toString().equalsIgnoreCase("Mini Bus")) {
                    VehicleTypeId = "8";
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        SpinStreet = rootView.findViewById(R.id.street_spinner);
        SpinStreet.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        SpinStreet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                String streetname = SpinStreet.getSelectedItem().toString();
                try {
                    for (int i = 0; i < streetarray.length(); i++) {
                        JSONObject jsonobject = streetarray.getJSONObject(i);
                        if (streetname.equalsIgnoreCase(jsonobject.getString("StreetName"))) {
                            StreetId = jsonobject.getString("StreetId");
                            Util.Logcat.e("StreetId" + StreetId);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        LoadStreetSpinner();
        return rootView;
    }

    private void LoadStreetSpinner() {

        try {
            JSONObject resobject = new JSONObject(TARIFF);
            JSONArray jsonArray = resobject.getJSONArray("_VechicleTraifflist");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject imageobject = jsonArray.getJSONObject(i);
                Util.Logcat.e("StreetName" + imageobject.getString("StreetName"));
                JSONObject savedata = new JSONObject();
                savedata.put("StreetId", imageobject.getString("StreetId"));
                savedata.put("StreetName", imageobject.getString("StreetName"));
                streetarray.put(savedata);
                spinnerlist.add(imageobject.getString("StreetName"));
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                            (getActivity(), R.layout.spinner_textview,
                                    spinnerlist); //selected item will look like a spinner set from XML
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);

                    SpinStreet.setAdapter(spinnerArrayAdapter);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("ONLINE VERIFY");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                if (!EdVehNo.getEditableText().toString().isEmpty()) {
                    CheckVehicle();
                } else {
                    alert.build(getString(R.string.enter_vehno));
                }
                break;
            default:
                break;
        }
    }

    private void CheckVehicle() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("ATDId", Util.getData("UserId", getActivity().getApplicationContext()));
            obj.put("VehicleNo", EdVehNo.getEditableText().toString());
            obj.put("StreetId", StreetId);
            obj.put("strDateTime", Util.parkingtime(0));
            obj.put("VehicleTypeId", VehicleTypeId);
            Util.Logcat.e("CHECK VEHICLE:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), CHECK_VEHICLE, new VolleyResponseListener() {
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
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            alert.build(resobject.getString("StatusDesc"));
                        } else if (resobject.getString("Status").equalsIgnoreCase("2")) {
                            alert.build(resobject.getString("StatusDesc"));
                        } else if (resobject.getString("Status").equalsIgnoreCase("3")) {
                            Checkalert.build(resobject.getString("StatusDesc"),
                                    resobject.getString("Duration"),
                                    resobject.getString("FareAmount"),
                                    resobject.getString("EntryTime"),
                                    resobject.getString("ExpiryTime"),
                                    resobject.getString("HourDuration"));
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

    private void cleardata() {
        EdVehNo.setText("");
        streetarray = new JSONArray();
        spinnerlist = new ArrayList<>();
        LoadStreetSpinner();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.start_stop);
        item.setVisible(false);


    }

}