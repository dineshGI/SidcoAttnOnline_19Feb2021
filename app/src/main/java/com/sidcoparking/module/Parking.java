package com.sidcoparking.module;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.activity.MainActivity;
import com.sidcoparking.adapter.VehicleListAdapter;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.ExpandableHeightGridView;
import com.sidcoparking.utils.OnlineVerifyAlertDialog;
import com.sidcoparking.utils.Util;
import com.sidcoparking.utils.VerifyAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sidcoparking.DataBase.DB_Constant.Entrytime;
import static com.sidcoparking.DataBase.DB_Constant.Expirytime;
import static com.sidcoparking.DataBase.DB_Constant.Isfoc;
import static com.sidcoparking.DataBase.DB_Constant.Streetid;
import static com.sidcoparking.DataBase.DB_Constant.VehNo;
import static com.sidcoparking.DataBase.DB_Constant.VehTypeId;
import static com.sidcoparking.utils.Util.ADD_PARKING;
import static com.sidcoparking.utils.Util.CHECK_VEHICLE;
import static com.sidcoparking.utils.Util.GET_TARIFF;
import static com.sidcoparking.utils.Util.PAYMENT_MODE;


public class Parking extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    Spinner SpinHour, SpinStreet, SpinPayment;
    String StrHour, TARIFF, STREETID, StreetName, VehicleTypeID = "", VehicleType = "", IsFOC = "0", TRANSID, Entry, Expiry, StrPayment, MobileNumber = "";
    Switch labeledSwitch;
    String NCGraceTo, Parktime = "";
    LinearLayout LayoutAmount, LayoutPayment;
    ExpandableHeightGridView gridView;
    Button AddVehicle, Verify, OnlineVerify, ClearData;
    EditText EdVehicleNo, EdAmount, EdMobileNo;
    DatabaseHelper TransactionDB;
    JSONArray streetarray, Paymentarray;
    int totalhour;
    List<String> spinnerlist, Paymentlist, Hourslist;
    CommonAlertDialog alert;
    OnlineVerifyAlertDialog OnlineVerifyAlert;
    VerifyAlertDialog verifyalert;
    boolean booleanvehicle = false;
    boolean extraamount = false;
    boolean ExtraFOC = false;
    private HashMap<String, String> DataHashMap;
    private List<Map<String, String>> ListCollection;
    VehicleListAdapter adapter;
    List<String> VehTypeArray;
    TextView TxtGraceHour;
    int CheckGraceHour = 0;
    private OnFragmentInteractionListener mListener;

    public Parking() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static Parking newInstance(String param1, String param2) {
        Parking fragment = new Parking();
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

        View rootView = inflater.inflate(R.layout.parking_home_new, container, false);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        AlertDialog.Builder alertDialogBuilder;
                        alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.alertDialog);
                        alertDialogBuilder.setMessage(R.string.want_to_exit);
                        alertDialogBuilder.setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        getActivity().finish();
                                    }
                                });

                        alertDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                        return true;
                    }
                }
                return false;
            }
        });

        setHasOptionsMenu(true);
        streetarray = new JSONArray();
        Paymentarray = new JSONArray();
        //Hoursarray = new JSONArray();

        spinnerlist = new ArrayList<>();
        Paymentlist = new ArrayList<>();
        Hourslist = new ArrayList<>();
        VehTypeArray = new ArrayList<>();
        TransactionDB = new DatabaseHelper(getActivity());

        TARIFF = Util.getData("tarifdetails", getActivity().getApplicationContext());
        ListCollection = new ArrayList<>();
        alert = new CommonAlertDialog(getActivity());
        OnlineVerifyAlert = new OnlineVerifyAlertDialog(getActivity());
        verifyalert = new VerifyAlertDialog(getActivity());
        TxtGraceHour = rootView.findViewById(R.id.grace_hour);

        SpinHour = rootView.findViewById(R.id.hour_spinner);
        SpinHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                StrHour = SpinHour.getSelectedItem().toString();
                Util.Logcat.e("OMG::" + StrHour);

                if (!VehicleType.isEmpty()) {
                    updateamount();
                }

                if (NCGraceTo == null) {
                    TxtGraceHour.setText("HOUR");
                } else if (Integer.parseInt(NCGraceTo) > 0) {
                    TxtGraceHour.setText("HOUR " + "(" + "Grace Time: " + NCGraceTo + " Mins" + ")");
                }

            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        SpinPayment = rootView.findViewById(R.id.payment_spinner);
        SpinPayment.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        SpinPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                String payment = SpinPayment.getSelectedItem().toString();
                //Util.Logcat.e("payment" + payment);
                try {
                    for (int i = 0; i < Paymentarray.length(); i++) {
                        JSONObject jsonobject = Paymentarray.getJSONObject(i);
                        if (payment.equalsIgnoreCase(jsonobject.getString("PaymentMode"))) {
                            StrPayment = jsonobject.getString("PaymentModeId");
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


        SpinStreet = rootView.findViewById(R.id.street_spinner);
        SpinStreet.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        //LoadHourspinner();
        LoadHourspinnerNew();
        SpinStreet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {

                if (!VehicleType.isEmpty()) {
                    Util.Logcat.e("calling" + "cleardata");
                    cleardata();
                }

                String streetname = SpinStreet.getSelectedItem().toString();
                Util.Logcat.e("streetname" + streetname);

                try {
                    for (int i = 0; i < streetarray.length(); i++) {
                        JSONObject jsonobject = streetarray.getJSONObject(i);
                        if (streetname.equalsIgnoreCase(jsonobject.getString("StreetName"))) {
                            STREETID = jsonobject.getString("StreetId");
                            StreetName = jsonobject.getString("StreetName");
                            Util.Logcat.e("STREETID" + STREETID);
                            Util.Logcat.e("STREETNAME" + StreetName);
                            LoadVehicle(STREETID);
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

        labeledSwitch = rootView.findViewById(R.id.toggle);
        EdMobileNo = rootView.findViewById(R.id.mobileno);
        gridView = rootView.findViewById(R.id.gridview);
        gridView.setExpanded(true);
        LayoutAmount = rootView.findViewById(R.id.lyAmount);
        LayoutPayment = rootView.findViewById(R.id.paymently);
        AddVehicle = rootView.findViewById(R.id.btn_addvehicle);
        ClearData = rootView.findViewById(R.id.btn_clear);
        Verify = rootView.findViewById(R.id.btn_verify);
        OnlineVerify = rootView.findViewById(R.id.btn_onlineverify);
        AddVehicle.setOnClickListener(this);
        ClearData.setOnClickListener(this);
        Verify.setOnClickListener(this);
        OnlineVerify.setOnClickListener(this);
        EdVehicleNo = rootView.findViewById(R.id.vehicleno);
        EdAmount = rootView.findViewById(R.id.amount);

        labeledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Util.Logcat.e("isChecked" + "true");
                    IsFOC = "1";
                    LayoutAmount.setVisibility(View.GONE);
                    LayoutPayment.setVisibility(View.GONE);
                } else {
                    LayoutAmount.setVisibility(View.VISIBLE);
                    LayoutPayment.setVisibility(View.VISIBLE);
                    IsFOC = "0";
                    if (!VehicleType.isEmpty() && !StrHour.isEmpty()) {
                        updateamount();
                    }
                    Util.Logcat.e("isChecked" + "false");
                }
            }
        });
        LoadStreetSpinner();
        LoadPaymentSpinner();

        return rootView;
    }

    private void LoadHourspinnerNew() {
        Hourslist.clear();

        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            Util.Logcat.e("HOURS INPUT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), GET_TARIFF, new VolleyResponseListener() {
            //CallApi.postResponse(getActivity(), params.toString(), GET_HOURS, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("HOURS:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            //Util.saveData("SavePaymentMode", resobject.toString(), getActivity());
                            try {
                                JSONArray jsonArray = resobject.getJSONArray("_lstHours");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    totalhour = jsonArray.length();
                                    JSONObject imageobject = jsonArray.getJSONObject(i);
                                    //Util.Logcat.e("PaymentMode" + imageobject.getString("PaymentMode"));
                                    // JSONObject savedata = new JSONObject();
                                    // savedata.put("Hours", imageobject.getString("Hours"));
                                    //Hoursarray.put(savedata);
                                    Hourslist.add(imageobject.getString("Hours"));
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                                                (getActivity(), R.layout.spinner_textview,
                                                        Hourslist); //selected item will look like a spinner set from XML
                                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                                .simple_spinner_dropdown_item);
                                        SpinHour.setAdapter(spinnerArrayAdapter);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {

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
   /* private void LoadHourspinner() {
        String[] type = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"};
        ArrayAdapter aa = new ArrayAdapter(getActivity(), R.layout.spinner_textview, type);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinHour.setAdapter(aa);
        SpinHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                StrHour = SpinHour.getSelectedItem().toString();
                if (!VehicleType.isEmpty()) {
                    updateamount();
                }

                if (NCGraceTo == null) {
                    TxtGraceHour.setText("HOUR");
                } else if (Integer.parseInt(NCGraceTo) > 0) {
                    TxtGraceHour.setText("HOUR " + "(" + "Grace Time: " + NCGraceTo + " Mins" + ")");
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }*/

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("PARKING");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addvehicle:
                if (!(Util.getData("WorkStatus", getActivity().getApplicationContext()).equalsIgnoreCase("0"))) {
                    if (VehicleType.isEmpty()) {
                        alert.build(getString(R.string.select_vehicle_type));
                    } else if (EdVehicleNo.getEditableText().toString().isEmpty()) {
                        alert.build(getString(R.string.enter_vehno));
                    } else if (!EdMobileNo.getEditableText().toString().isEmpty() && EdMobileNo.getEditableText().toString().length() != 10) {
                        alert.build(getString(R.string.enter_valid_mobileno));
                    } else {
                        //StrHour
                        if (EdVehicleNo.getEditableText().toString().length() == 1) {
                            EdVehicleNo.setText("000" + EdVehicleNo.getEditableText().toString());
                        } else if (EdVehicleNo.getEditableText().toString().length() == 2) {
                            EdVehicleNo.setText("00" + EdVehicleNo.getEditableText().toString());
                        } else if (EdVehicleNo.getEditableText().toString().length() == 3) {
                            EdVehicleNo.setText("0" + EdVehicleNo.getEditableText().toString());
                        }
                        updateamount();
                        if (ExtraFOC) {
                            IsFOC = "0";
                        }
                        SaveData();

                    }
                } else {
                    CommonAlertDialog alert = new CommonAlertDialog(getActivity());
                    alert.build(getString(R.string.start_msg));
                }

                break;

            case R.id.btn_clear:
                cleardata();
                break;

            case R.id.btn_onlineverify:
                OnlineVerify.setClickable(false);
                v.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        OnlineVerify.setClickable(true);
                    }
                }, 1000);

                if (EdVehicleNo.getEditableText().toString().isEmpty()) {
                    alert.build(getString(R.string.enter_vehno));
                } else if (VehicleType.isEmpty()) {
                    alert.build(getString(R.string.select_vehicle_type));
                } else {
                    getvehicleid(VehicleType);
                    CheckVehicleOnline();
                }
                break;

            case R.id.btn_verify:
                //check vehicle number
                int extratime = 0;
                booleanvehicle = false;
                //int ExtraHour=0;
                if (EdVehicleNo.getEditableText().toString().isEmpty()) {
                    alert.build(getString(R.string.vehno_verify));
                } else if (VehicleType.isEmpty()) {
                    alert.build(getString(R.string.select_vehicle_type));
                } else {

                    Cursor res = TransactionDB.getAllData();
                    for (res.moveToLast(); !res.isBeforeFirst(); res.moveToPrevious()) {

                        Util.Logcat.e("loop::" + Util.DECODE64(res.getString(Isfoc)));
                        if (Util.DECODE64(res.getString(VehNo)).equalsIgnoreCase(EdVehicleNo.getEditableText().toString()) && Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase(getvehicleid(VehicleType)) && Util.DECODE64(res.getString(Streetid)).equalsIgnoreCase(STREETID) && Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("0")) {
                            //TxnId~VehicleTypeId~VehicleNo~StreetId~ParkingHour~ParkingFee~IsGraceHour~GraceHour~GraceFee~IsFOC~UserId~EntryTime~ExpiryTime|
                            Util.Logcat.e("if");
                            booleanvehicle = true;
                            EdVehicleNo.setEnabled(false);
                            String oldtime = Util.DECODE64(res.getString(Expirytime));
                            extratime = Integer.parseInt(Util.showextra(oldtime, Util.parkingtime(0)));
                            Parktime = Util.showparkingtime(Util.DECODE64(res.getString(Entrytime)), Util.parkingtime(0));
                            break;
                        } else if (Util.DECODE64(res.getString(VehNo)).equalsIgnoreCase(EdVehicleNo.getEditableText().toString()) && Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase(getvehicleid(VehicleType)) && Util.DECODE64(res.getString(Streetid)).equalsIgnoreCase(STREETID) && Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("2")) {
                            Util.Logcat.e("else if");
                            booleanvehicle = true;
                            EdVehicleNo.setEnabled(false);
                            String diff = Util.showextrabyminutes(Util.DECODE64(res.getString(Entrytime)), Util.parkingtime(0));

                            if (Integer.parseInt(diff) <= Integer.valueOf(NCGraceTo)) {
                                extratime = 0;
                                Parktime = Util.showparkingtime(Util.DECODE64(res.getString(Entrytime)), Util.parkingtime(0));
                                Util.Logcat.e("with grace:foc:" + extratime);
                                break;

                            } else {
                                extratime = Integer.parseInt(Util.showextra(Util.DECODE64(res.getString(Entrytime)), Util.parkingtime(0)));
                                Parktime = Util.showparkingtime(Util.DECODE64(res.getString(Entrytime)), Util.parkingtime(0));
                                Util.Logcat.e("without grace:foc:" + extratime);
                                ExtraFOC = true;
                                break;
                            }
                        }
                    }

                    if (booleanvehicle) {
                        if (Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("2")) {
                            Util.Logcat.e("Isfoc:true" + Util.DECODE64(res.getString(Isfoc)));
                            Util.Logcat.e("Grace time:" + NCGraceTo);
                            int foctotal = 0;
                            if (extratime <= 0) {
                                Util.Logcat.e("extratime time:false" + extratime);
                                // alert.build(getString(R.string.Vehicle_msg) + "\nAdditional Charges :" + "NIL");
                                verifyalert.build(getString(R.string.Vehicle_msg), "NIL", "");
                                cleardata();
                                EdVehicleNo.setEnabled(true);
                                Util.Logcat.e("zero 1" + EdAmount.getEditableText().toString());
                                EdAmount.setText("0");
                            } else {
                                Util.Logcat.e("extratime time:true:" + extratime);
                                try {
                                    JSONObject jsonObject = new JSONObject(TARIFF);
                                    JSONArray jsonArray = jsonObject.optJSONArray("_VechicleTraifflist");
                                    if (jsonArray != null) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject innerOBj = jsonArray.getJSONObject(i);
                                            if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                                                JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                                                if (innerArray != null) {
                                                    for (int j = 0; j < innerArray.length(); j++) {
                                                        JSONObject object = innerArray.getJSONObject(j);
                                                        Util.Logcat.e("VehicleType" + VehicleType);
                                                        Util.Logcat.e("VehicleType" + object.getString("VehicleType"));
                                                        if (VehicleType.equalsIgnoreCase(object.getString("VehicleType"))) {
                                                            VehicleTypeID = object.getString("VehicleTypeId");
                                                            int amount = Integer.parseInt(object.getString("FlatTariff")) * extratime;
                                                            foctotal = amount;
                                                            Util.Logcat.e("FOC total" + amount);
                                                            extraamount = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                verifyalert.build(getString(R.string.Vehicle_msg), String.valueOf(extratime), String.valueOf(foctotal));
                                //  Util.Logcat.e("verifyalert", "2");

                                EdAmount.setText(String.valueOf(foctotal));
                                StrHour = String.valueOf(extratime);
                                CheckGraceHour = extratime;

                                //  Util.Logcat.e("spin:extratime-1", String.valueOf(extratime - 1));
                                if ((extratime - 1) <= totalhour) {
                                    SpinHour.setSelection(extratime - 1);
                                }
                            }

                        } else {
                            Util.Logcat.e("Isfoc:false" + Isfoc);
                            int total = 0;
                            if (extratime <= 0) {
                                // alert.build(getString(R.string.Vehicle_msg) + "\nAdditional Charges :" + "NIL");
                                verifyalert.build(getString(R.string.Vehicle_msg), "NIL", "");
                                cleardata();
                                EdVehicleNo.setEnabled(true);
                                EdAmount.setText("0");
                            } else {
                                try {
                                    JSONObject jsonObject = new JSONObject(TARIFF);
                                    JSONArray jsonArray = jsonObject.optJSONArray("_VechicleTraifflist");

                                    if (jsonArray != null) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject innerOBj = jsonArray.getJSONObject(i);
                                            if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                                                JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                                                if (innerArray != null) {
                                                    for (int j = 0; j < innerArray.length(); j++) {
                                                        JSONObject object = innerArray.getJSONObject(j);
                                                        //Util.Logcat.e("VehicleType" + VehicleType);
                                                        //Util.Logcat.e("VehicleType" + object.getString("VehicleType"));
                                                        if (VehicleType.equalsIgnoreCase(object.getString("VehicleType"))) {
                                                            VehicleTypeID = object.getString("VehicleTypeId");
                                                            int amount = Integer.parseInt(object.getString("FlatTariff"));
                                                            total = amount * extratime;
                                                            Util.Logcat.e("total/total" + total);
                                                            extraamount = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //  alert.build(getString(R.string.Vehicle_msg) + "\nAdditional Charges :" + extratime + "H - " + String.valueOf(total));
                                verifyalert.build(getString(R.string.Vehicle_msg), String.valueOf(extratime), String.valueOf(total));
                                //  Util.Logcat.e("verifyalert", "2");
                                EdAmount.setText(String.valueOf(total));
                                StrHour = String.valueOf(extratime);
                                //  Util.Logcat.e("spin:extratime-1", String.valueOf(extratime - 1));
                                if ((extratime - 1) <= totalhour) {
                                    SpinHour.setSelection(extratime - 1);
                                }
                            }
                        }
                    } else {
                        alert.build(getString(R.string.Vehicle_not_msg));

                    }
                    break;
                }

            default:
                break;
        }
    }


    private void CheckVehicleOnline() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("ATDId", Util.getData("UserId", getActivity().getApplicationContext()));
            obj.put("VehicleNo", EdVehicleNo.getEditableText().toString());
            obj.put("StreetId", STREETID);
            obj.put("strDateTime", Util.parkingtime(0));
            obj.put("VehicleTypeId", VehicleTypeID);
            Util.Logcat.e("CHECK VEHICLE:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponseNopgrss(getActivity(), params.toString(), CHECK_VEHICLE, new VolleyResponseListener() {
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
                            EdVehicleNo.setText("");
                        } else if (resobject.getString("Status").equalsIgnoreCase("3")) {
                            String Transid = Util.GenerateTransactionId(Util.getData("terminalid", getActivity().getApplicationContext()));
                            JSONObject data = new JSONObject();
                            data.put("Transid", Transid);
                            data.put("VehicleTypeID", VehicleTypeID);
                            data.put("VehicleNo", EdVehicleNo.getEditableText().toString());
                            data.put("StreetId", STREETID);
                            data.put("Duration", resobject.getString("Duration"));
                            data.put("FareAmount", resobject.getString("FareAmount"));
                            data.put("IsGraceHour", "0");
                            data.put("GraceHour", "0");
                            data.put("GraceFee", "0");
                            data.put("IsFOC", resobject.getString("FOCType"));
                            data.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
                            data.put("EntryTime", resobject.getString("EntryTime"));
                            data.put("ExpiryTime", resobject.getString("ExpiryTime"));
                            data.put("ServerTransid", "0");
                            data.put("PaymentMode", StrPayment);
                            data.put("MobileNumber", resobject.getString("MobileNo"));
                            data.put("StreetName", StreetName);
                            data.put("StatusDesc", resobject.getString("StatusDesc"));
                            data.put("VehicleType", VehicleType);
                            data.put("Datetime", Util.parkingtime(0));
                            data.put("HourDuration", resobject.getString("HourDuration"));
                            BuildAlert(data);
                            // OnlineVerifyAlert.build(data);
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

    private void updateamount() {
        try {
            JSONObject jsonObject = new JSONObject(TARIFF);
            JSONArray jsonArray = jsonObject.optJSONArray("_VechicleTraifflist");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject innerOBj = jsonArray.getJSONObject(i);
                    if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                        JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                        if (innerArray != null) {
                            for (int j = 0; j < innerArray.length(); j++) {
                                JSONObject object = innerArray.getJSONObject(j);
                                if (VehicleType.equalsIgnoreCase(object.getString("VehicleType"))) {
                                    VehicleTypeID = object.getString("VehicleTypeId");
                                    NCGraceTo = object.getString("NCGraceTo");
                                    int amount = Integer.parseInt(object.getString("FlatTariff"));
                                    int total = amount * Integer.parseInt(StrHour);

                                    Util.Logcat.e("NCGraceTo>>" + NCGraceTo);
                                    Util.Logcat.e("total>>" + total);
                                    if (Integer.parseInt(NCGraceTo) > 0 && IsFOC.equalsIgnoreCase("0")) {
                                        IsFOC = "2";
                                        TxtGraceHour.setText("HOUR " + "(" + "Grace Time: " + NCGraceTo + " Mins" + ")");
                                        EdAmount.setText("0");
                                    } else {
                                        TxtGraceHour.setText("HOUR");

                                    }
                                    if (!extraamount) {
                                        EdAmount.setText(String.valueOf(total));
                                        Util.Logcat.e("zero 2" + EdAmount.getEditableText().toString());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (IsFOC.equalsIgnoreCase("1")) {
            Util.Logcat.e("zero 3" + EdAmount.getEditableText().toString());
            EdAmount.setText("0");
        } else if (IsFOC.equalsIgnoreCase("2") && CheckGraceHour == 0) {
            Util.Logcat.e("zero 4" + EdAmount.getEditableText().toString());
            EdAmount.setText("0");
        }

    }


    private void SaveData() {

        if (booleanvehicle) {
            Cursor res = TransactionDB.getAllData();
            for (res.moveToLast(); !res.isBeforeFirst(); res.moveToPrevious()) {
                if (Util.DECODE64(res.getString(VehNo)).equalsIgnoreCase(EdVehicleNo.getEditableText().toString())) {
                    if (Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("2")) {
                        Util.Logcat.e("ISFOC 1: " + Util.DECODE64(res.getString(Isfoc)));
                        Entry = Util.DECODE64(res.getString(Entrytime));
                    } else {
                        Util.Logcat.e("ISFOC 2: " + Util.DECODE64(res.getString(Isfoc)));
                        Entry = Util.DECODE64(res.getString(Expirytime));
                    }
                    Expiry = Util.sumdate(Entry, Integer.valueOf(StrHour));
                    break;
                }
            }

            Expiry = Util.sumdate(Entry, Integer.valueOf(StrHour));
        } else {
            Entry = Util.parkingtime(0);
            if (IsFOC.equalsIgnoreCase("2")) {
                Expiry = Util.addgrace(Entry, Integer.valueOf(NCGraceTo));
            } else {
                Expiry = Util.parkingtime(Integer.valueOf(StrHour));
            }
        }

        TRANSID = Util.GenerateTransactionId(Util.getData("terminalid", getActivity().getApplicationContext()));
        MobileNumber = EdMobileNo.getEditableText().toString();
        Util.Logcat.e("IsFOC 3:" + IsFOC);

        if (!Entry.equalsIgnoreCase("") && !Expiry.equalsIgnoreCase("")) {

            /*if(TxtGraceHour.getText().toString().equalsIgnoreCase("HOURS")){
                boolean isInserted = TransactionDB.insertData(TRANSID, VehicleTypeID, EdVehicleNo.getEditableText().toString(), STREETID, StrHour, EdAmount.getEditableText().toString(), "0", "0", "0", IsFOC, Util.getData("UserId", getActivity().getApplicationContext()), Entry, Expiry, "0", StrPayment, MobileNumber, StreetName, Entry);
                if (isInserted == true) {
                    Toast.makeText(getActivity(), "Data Inserted", Toast.LENGTH_LONG).show();
                }
            }else {
                boolean isInserted = TransactionDB.insertData(TRANSID, VehicleTypeID, EdVehicleNo.getEditableText().toString(), STREETID, NCGraceTo, EdAmount.getEditableText().toString(), "0", "0", "0", IsFOC, Util.getData("UserId", getActivity().getApplicationContext()), Entry, Expiry, "0", StrPayment, MobileNumber, StreetName, Entry);
                if (isInserted == true) {
                    Toast.makeText(getActivity(), "Grace Hour Data Inserted", Toast.LENGTH_LONG).show();
                }
            }*/

            Util.Logcat.e("INSERT Expiry ::" + Expiry);
            boolean isInserted = TransactionDB.insertData(TRANSID, VehicleTypeID, EdVehicleNo.getEditableText().toString(), STREETID, StrHour, EdAmount.getEditableText().toString(), "0", "0", "0", IsFOC, Util.getData("UserId", getActivity().getApplicationContext()), Entry, Expiry, "0", StrPayment, MobileNumber, StreetName, Util.parkingtime(0));
            if (isInserted == true) {
                Toast.makeText(getActivity(), "Data Inserted", Toast.LENGTH_LONG).show();
            }
            PrintReciept();
        } else {
            alert.build("Please Try Again");
            cleardata();
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void LoadPaymentSpinner() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            Util.Logcat.e("INPUT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), PAYMENT_MODE, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("PAYMENT:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            Util.saveData("SavePaymentMode", resobject.toString(), getActivity());
                            try {
                                JSONArray jsonArray = resobject.getJSONArray("_GetPaymentMode");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject imageobject = jsonArray.getJSONObject(i);
                                    //Util.Logcat.e("PaymentMode" + imageobject.getString("PaymentMode"));
                                    JSONObject savedata = new JSONObject();
                                    savedata.put("PaymentModeId", imageobject.getString("PaymentModeId"));
                                    savedata.put("PaymentMode", imageobject.getString("PaymentMode"));
                                    Paymentarray.put(savedata);
                                    Paymentlist.add(imageobject.getString("PaymentMode"));
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                                                (getActivity(), R.layout.spinner_textview,
                                                        Paymentlist); //selected item will look like a spinner set from XML
                                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                                .simple_spinner_dropdown_item);
                                        SpinPayment.setAdapter(spinnerArrayAdapter);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {

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

    private void LoadVehicle(String streetId) {
        ListCollection.clear();
        try {
            JSONObject jsonObject = new JSONObject(TARIFF);
            JSONArray jsonArray = jsonObject.optJSONArray("_VechicleTraifflist");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject innerOBj = jsonArray.getJSONObject(i);
                    if (streetId.equalsIgnoreCase(innerOBj.getString("StreetId"))) {
                        JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                        if (innerArray != null) {
                            for (int j = 0; j < innerArray.length(); j++) {
                                JSONObject object = innerArray.getJSONObject(j);
                                DataHashMap = new HashMap<>();
                                DataHashMap.put("VehicleTypeId", object.getString("VehicleTypeId"));
                                DataHashMap.put("VehicleType", object.getString("VehicleType"));
                                DataHashMap.put("FlatTariff", object.getString("FlatTariff"));
                                //Util.Logcat.e("VehicleType" + object.getString("VehicleType"));
                                ListCollection.add(DataHashMap);
                            }
                        }
                        break;
                    }
                }

                adapter = new VehicleListAdapter(getActivity(), ListCollection);
                gridView.setAdapter(adapter);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        ((TextView) view.findViewById(R.id.vehicle_type)).getText().toString();
                        labeledSwitch.setChecked(false);
                        IsFOC = "0";
                        VehicleType = ((TextView) view.findViewById(R.id.vehicle_type)).getText().toString();
                        EdVehicleNo.requestFocus();

                        if (!VehicleType.isEmpty() && !StrHour.isEmpty()) {
                            updateamount();
                        }
                        //added on 31Oct19
                        if (NCGraceTo == null) {
                            TxtGraceHour.setText("HOUR");
                        } else if (Integer.parseInt(NCGraceTo) > 0) {
                            TxtGraceHour.setText("HOUR " + "(" + "Grace Time: " + NCGraceTo + " Mins" + ")");
                        }
                        adapter.makeAllUnselect(position);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void LoadStreetSpinner() {
        try {
            JSONObject resobject = new JSONObject(TARIFF);
            JSONArray jsonArray = resobject.getJSONArray("_VechicleTraifflist");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject imageobject = jsonArray.getJSONObject(i);
                JSONObject savedata = new JSONObject();
                savedata.put("StreetId", imageobject.getString("StreetId"));
                savedata.put("StreetName", imageobject.getString("StreetName"));
                streetarray.put(savedata);
                spinnerlist.add(imageobject.getString("StreetName"));
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                            (getActivity(), R.layout.spinner_textview,
                                    spinnerlist);
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.start_stop);
        item.setVisible(true);
    }

    private void cleardata() {
        //StrHour = "1";
        //EdAmount.setText("");
        EdVehicleNo.setText("");
        EdMobileNo.setText("");
        TRANSID = "";
        Entry = "";
        Expiry = "";
        MobileNumber = "";
        IsFOC = "0";
        VehicleTypeID = "";
        VehicleType = "";
        //StrPayment = "";
        TxtGraceHour.setText("HOUR");
        CheckGraceHour = 0;
        booleanvehicle = false;
        ExtraFOC = false;
        Parktime = "";
        extraamount = false;
        labeledSwitch.setChecked(false);
        // LoadStreetSpinner();
        EdVehicleNo.setEnabled(true);
        SpinHour.setSelection(0);
        //LoadHourspinnerNew();
        if (!STREETID.isEmpty())
            LoadVehicle(STREETID);

    }

    private void ParkNowOnline() {

        String data = "";
        JSONObject obj = new JSONObject();
        //TxnId~VehicleTypeId~VehicleNo~StreetId~ParkingHour~ParkingFee~IsGraceHour~GraceHour~GraceFee~IsFOC|
        try {
            obj.put("TerminalId", Util.getData("terminalid", getActivity().getApplicationContext()));
            obj.put("TransactionInfo", TRANSID + "~" + VehicleTypeID + "~" + EdVehicleNo.getEditableText().toString() + "~" + STREETID + "~" + StrHour + "~" + EdAmount.getEditableText().toString() + "~" + "0" + "~" + "0" + "~" + "0" + "~" + IsFOC + "~" + Util.getData("UserId", getActivity().getApplicationContext()) + "~" + Entry + "~" + Expiry + "~" + StrPayment + "|");
            Util.Logcat.e("INPUT:::" + obj.toString());
            data = Util.EncryptURL(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), ADD_PARKING, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("ADD_PARKING:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            //  UpdateServerId(resobject.getString("ServerGenNo"));
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.alertDialog);
                            alertDialogBuilder.setMessage(resobject.getString("StatusDesc").toString());
                            alertDialogBuilder.setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {

                                            cleardata();
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCancelable(false);
                            alertDialog.show();

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.alertDialog);
                            alertDialogBuilder.setMessage(resobject.getString("StatusDesc"));
                            alertDialogBuilder.setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {

                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCancelable(false);
                            alertDialog.show();
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

    private void PrintReciept() {

        StringBuffer printvalues = new StringBuffer();
        printvalues.append(getString(R.string.onstreet_parking)+ "\n");
        printvalues.append("\n" + getString(R.string.line));
        printvalues.append("\n" + StreetName);
        printvalues.append("\n" + "V.No       :" + EdVehicleNo.getEditableText().toString());
        printvalues.append("\n" + "V.Type     :" + VehicleType);
        Util.Logcat.e("VehicleType" + VehicleType);
        Util.Logcat.e("VehicleTypeID" + VehicleTypeID);
        if (!MobileNumber.isEmpty()) {
            printvalues.append("\n" + "Mobile No:" + MobileNumber);
        }
        if (IsFOC.equalsIgnoreCase("1")) {
            printvalues.append("\n" + "FOC      :" + "Yes");
        }
        Util.Logcat.e("PRint FOC " + IsFOC);
        printvalues.append("\n" + "Txn ID   :" + TRANSID);
        printvalues.append("\n" + "Entry :" + Entry);

        if (!IsFOC.equalsIgnoreCase("2")) {
            printvalues.append("\n" + "Expiry:" + Expiry);
            printvalues.append("\n" + "Hours   :" + StrHour);
        } else {
            Util.Logcat.e("Printing grace::" + NCGraceTo + " Min Grace Hours");
            printvalues.append("\n" + "Hours    :" + NCGraceTo + " Mins Grace Time");
        }

        Util.Logcat.e("PRint:" + IsFOC);
        //printvalues.append("Hours    :" + StrHour);
        if (!Parktime.equalsIgnoreCase("")) {
            printvalues.append("\n" + "Duration :" + Parktime);
            Util.Logcat.e("Duration::" + Parktime);
        } else {
            Util.Logcat.e("Duration::" + "EMPTY");
        }

        printvalues.append("\n" + "Amount   :" + "Rs." + EdAmount.getEditableText().toString());
        Util.Logcat.e("Amount:" + EdAmount.getEditableText().toString());
        printvalues.append("\n" +  getString(R.string.line));
        printvalues.append("\n" + "Pwd by GI Retail Pvt.Ltd");
        printvalues.append("\n" + "  Mgd by GRGK Pvt. Ltd");
        printvalues.append("\n" + " PARKING AT OWNERS RISK");

        MainActivity.printContent = printvalues.toString();
        MainActivity.getInstance().CommonPrint();
        cleardata();

    }

    private String getvehicleid(String vehtype) {
        try {
            JSONObject jsonObject = new JSONObject(TARIFF);
            JSONArray jsonArray = jsonObject.optJSONArray("_VechicleTraifflist");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject innerOBj = jsonArray.getJSONObject(i);
                    if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                        JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                        if (innerArray != null) {
                            for (int j = 0; j < innerArray.length(); j++) {
                                JSONObject object = innerArray.getJSONObject(j);
                                if (vehtype.equalsIgnoreCase(object.getString("VehicleType"))) {
                                    VehicleTypeID = object.getString("VehicleTypeId");
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return VehicleTypeID;
    }

    public void BuildAlert(final JSONObject data) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.check_alert_online);
        // set the custom dialog components - text, image and button

        TextView Tvstatus = dialog.findViewById(R.id.status);
        TextView Tvextrahour = dialog.findViewById(R.id.extrahour);
        TextView Tvaddition = dialog.findViewById(R.id.addition);
        TextView Tventry = dialog.findViewById(R.id.entry);
        TextView Tvexit = dialog.findViewById(R.id.exit);
        TextView Tvduration = dialog.findViewById(R.id.duration);

        try {
            Tvstatus.setText(data.getString("StatusDesc"));
            Tvextrahour.setText("Extra Hour :" + data.getString("Duration"));
            Tvaddition.setText("Additional Charges :" + data.getString("FareAmount"));
            Tventry.setText("Entry :" + data.getString("EntryTime"));
            Tvexit.setText("Expiry :" + data.getString("ExpiryTime"));
            Tvduration.setText("Duration :" + data.getString("HourDuration"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // image.setImageResource(R.mipmap.ic_launcher);
        Button BtnPrint = dialog.findViewById(R.id.print);
        BtnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    boolean isInserted = TransactionDB.insertData(data.getString("Transid"),
                            data.getString("VehicleTypeID"),
                            data.getString("VehicleNo"),
                            data.getString("StreetId"),
                            data.getString("Duration"),
                            data.getString("FareAmount"),
                            data.getString("IsGraceHour"),
                            data.getString("GraceHour"),
                            data.getString("GraceFee"),
                            data.getString("IsFOC"),
                            data.getString("UserId"),
                            data.getString("EntryTime"),
                            data.getString("ExpiryTime"),
                            data.getString("ServerTransid"),
                            data.getString("PaymentMode"),
                            data.getString("MobileNumber"),
                            data.getString("StreetName"),
                            data.getString("Datetime"));
                    if (isInserted == true) {
                        Toast.makeText(getActivity(), "Data Inserted", Toast.LENGTH_LONG).show();
                    }
                    StringBuffer printvalues = new StringBuffer();
                    printvalues.append(getString(R.string.onstreet_parking));
                    printvalues.append("\n" + getString(R.string.line));
                    printvalues.append("\n" +data.getString("StreetName"));
                    printvalues.append("\n" +"V.No   :" + data.getString("VehicleNo"));
                    printvalues.append("\n" +"V.Type:" + data.getString("VehicleType"));
                    if (!data.getString("MobileNumber").isEmpty()) {
                        printvalues.append("\n" +"Mobile No:" + data.getString("MobileNumber"));
                    }
                    if (data.getString("IsFOC").equalsIgnoreCase("1")) {
                        printvalues.append("\n" +"FOC   :" + "Yes");
                    }
                    printvalues.append("\n" +"Txn ID:" + data.getString("Transid"));
                    //printer.printText("Date     :" + Util.parkingtime(0));
                    printvalues.append("\n" +"Entry :" + data.getString("EntryTime"));
                    printvalues.append("\n" +"Expiry:" + data.getString("ExpiryTime"));
                    printvalues.append("\n" +"Hours    :" + data.getString("Duration"));
                    printvalues.append("\n" +"Duration :" + data.getString("HourDuration"));
                    Util.Logcat.e("Duration::" + Util.showparkingtime(data.getString("EntryTime"), data.getString("ExpiryTime")));
                    printvalues.append("\n" +"Amount   :" + "Rs." + data.getString("FareAmount"));
                    printvalues.append("\n" + getString(R.string.line));
                    printvalues.append("\n" + "Pwd by GI Retail Pvt.Ltd");
                    printvalues.append("\n" + "  Mgd by GRGK Pvt. Ltd");
                    printvalues.append("\n" + " PARKING AT OWNERS RISK");
                    MainActivity.printContent = printvalues.toString();
                    MainActivity.getInstance().CommonPrint();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cleardata();
                // LoadVehicle(STREETID);
            }
        });
        Button BtnCancel = dialog.findViewById(R.id.cancel);
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                EdVehicleNo.setText("");
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}