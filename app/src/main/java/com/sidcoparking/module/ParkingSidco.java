package com.sidcoparking.module;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.activity.MainActivity;
import com.sidcoparking.activity.ScanPlate;
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
import static com.sidcoparking.DataBase.DB_Constant.MobileNo;
import static com.sidcoparking.DataBase.DB_Constant.Streetid;
import static com.sidcoparking.DataBase.DB_Constant.TransId;
import static com.sidcoparking.DataBase.DB_Constant.VehNo;
import static com.sidcoparking.DataBase.DB_Constant.VehTypeId;
import static com.sidcoparking.utils.Util.CHECK_VEHICLE;
import static com.sidcoparking.utils.Util.PAYMENT_MODE;


public class ParkingSidco extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Spinner SpinTimeSlab, SpinPayment, SpinDay, SpinStreet;
    String StrHour, TARIFF, STREETID, StreetName, VehicleTypeID = "", VehicleType = "", IsFOC = "0", TRANSID, Entry, Expiry, StrPayment, MobileNumber = "";
    Switch labeledSwitch;
    String NCGraceTo, Parktime = "", StrTimeSlab, StrDay = "0";
    // PrevTrasnID = "", PrevEntryTimeStr = "", Noof12 = "0";
    int DAYAMT = 0;
    LinearLayout LayoutAmount;
    LinearLayout LayoutPayment;
    ExpandableHeightGridView gridView;
    Button AddVehicle, Verify, OnlineVerify, ClearData;
    EditText EdVehicleNo, EdAmount, EdMobileNo;
    DatabaseHelper TransactionDB;
    JSONArray streetarray, Paymentarray;
    int totalhour;
    List<String> spinnerlist, Paymentlist, Slablist;
    CommonAlertDialog alert;
    OnlineVerifyAlertDialog OnlineVerifyAlert;
    VerifyAlertDialog verifyalert;
    boolean booleanvehicle = false, VIP = false;

    boolean extraamount = false;
    boolean ExtraFOC = false;
    private HashMap<String, String> DataHashMap;
    private List<Map<String, String>> ListCollection;
    VehicleListAdapter adapter;
    List<String> VehTypeArray;
    TextView TxtGraceHour;
    int CheckGraceHour = 0;
    String Noof12 = "0";
    private OnFragmentInteractionListener mListener;
    ImageView scan;

    public ParkingSidco() {
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
    public static ParkingSidco newInstance(String param1, String param2) {
        ParkingSidco fragment = new ParkingSidco();
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

        View rootView = inflater.inflate(R.layout.parking_sidco, container, false);
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
        Slablist = new ArrayList<>();
        VehTypeArray = new ArrayList<>();
        TransactionDB = new DatabaseHelper(getActivity());

        TARIFF = Util.getData("tarifdetails", getActivity().getApplicationContext());
        ListCollection = new ArrayList<>();
        alert = new CommonAlertDialog(getActivity());
        OnlineVerifyAlert = new OnlineVerifyAlertDialog(getActivity());
        verifyalert = new VerifyAlertDialog(getActivity());
        scan = rootView.findViewById(R.id.scan);
        TxtGraceHour = rootView.findViewById(R.id.grace_hour);
        SpinDay = rootView.findViewById(R.id.day_spinner);
        SpinTimeSlab = rootView.findViewById(R.id.hour_spinner);
        SpinTimeSlab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                StrTimeSlab = SpinTimeSlab.getSelectedItem().toString();
                Util.Logcat.e("StrTimeSlab::" + StrTimeSlab);
                if (!VehicleType.isEmpty())

                    try {
                        String amount = getamount(VehicleType, StrTimeSlab);
                        DAYAMT = Integer.parseInt(amount);
                    } catch (NumberFormatException ex) { // handle your exception
                        Util.Logcat.e("NumberFormatException" + String.valueOf(ex));
                    }
                int day = Integer.parseInt(StrTimeSlab.substring(5, 7));
                if (day > 24) {
                    LayoutPayment.setVisibility(View.VISIBLE);
                    /*SpinDay.setSelection(0);
                    StrDay=SpinDay.getSelectedItem().toString();*/
                    LoadDaySpinner();
                } else {
                    LayoutPayment.setVisibility(View.VISIBLE);
                    StrDay = "0";
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

        SpinStreet = rootView.findViewById(R.id.street_spinner);
        SpinStreet.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
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
        scan.setOnClickListener(this);
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
                    EdAmount.setText("0");
                } else {
                    IsFOC = "0";
                    LayoutAmount.setVisibility(View.VISIBLE);
                    LayoutPayment.setVisibility(View.VISIBLE);

                    if (!VehicleType.isEmpty() && !StrTimeSlab.isEmpty()) {
                        getamount(VehicleType, StrTimeSlab);

                    }
                    Util.Logcat.e("isChecked" + "false");
                }
            }
        });
        LoadStreetSpinner();

        LoadPaymentSpinner();

        return rootView;
    }

    private void LoadDaySpinner() {

        String[] type = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter aa = new ArrayAdapter(getActivity(), R.layout.spinner_textview, type);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinDay.setAdapter(aa);
        SpinDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {

                StrDay = SpinDay.getSelectedItem().toString();

                if (DAYAMT != 0) {
                    int temp = Integer.parseInt(StrDay) * DAYAMT;
                    EdAmount.setText(String.valueOf(temp));
                }

            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Util.SCAN_VALUE.isEmpty()) {
            EdVehicleNo.setText(Util.SCAN_VALUE);
            Util.SCAN_VALUE = "";
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

    private void LoadHourspinnerNew(String vehtype) {
        Util.Logcat.e("LoadHourspinnerNew:" + vehtype);
        Slablist.clear();

        try {
            JSONObject resobject = new JSONObject(TARIFF);
            JSONArray jsonArray = resobject.optJSONArray("_VechicleTraifflist");
            //   JSONArray slabobject = jsonArray.getJSONObject(0).getJSONArray("_VechicleTraiff");

            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject innerOBj = jsonArray.getJSONObject(i);
                    if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                        JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                        if (innerArray != null) {
                            for (int j = 0; j < innerArray.length(); j++) {
                                JSONObject object = innerArray.getJSONObject(j);
                                if (VehicleType.equalsIgnoreCase(object.getString("VehicleType"))) {
                                    JSONArray tariffarray = object.optJSONArray("Tariff");
                                    for (int k = 0; k < tariffarray.length(); k++) {
                                        JSONObject getlabel = tariffarray.getJSONObject(k);
                                        Slablist.add(getlabel.getString("FareLabel"));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                                (getActivity(), R.layout.spinner_textview,
                                        Slablist); //selected item will look like a spinner set from XML
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                .simple_spinner_dropdown_item);
                        SpinTimeSlab.setAdapter(spinnerArrayAdapter);
                    }
                });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getamount(String vehtype, String timeslab) {

        StrTimeSlab = timeslab;
        String finalamt = "";

        try {
            JSONObject resobject = new JSONObject(TARIFF);
            JSONArray jsonArray = resobject.optJSONArray("_VechicleTraifflist");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject innerOBj = jsonArray.getJSONObject(i);
                    if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                        JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                        if (innerArray != null) {
                            for (int j = 0; j < innerArray.length(); j++) {
                                JSONObject object = innerArray.getJSONObject(j);
                                if (vehtype.equalsIgnoreCase(object.getString("VehicleType"))) {

                                    JSONArray tariffarray = object.optJSONArray("Tariff");
                                    for (int k = 0; k < tariffarray.length(); k++) {
                                        JSONObject getlabel = tariffarray.getJSONObject(k);
                                        if (timeslab.equalsIgnoreCase(getlabel.getString("FareLabel"))) {
                                            finalamt = getlabel.getString("FlatTariff");
                                            Util.Logcat.e("finalamt{}" + finalamt);

                                            EdAmount.setText(finalamt);

                                            if (IsFOC.equalsIgnoreCase("1")) {
                                                Util.Logcat.e("zero 3" + EdAmount.getEditableText().toString());
                                                EdAmount.setText("0");
                                            } else if (IsFOC.equalsIgnoreCase("2") && CheckGraceHour == 0) {
                                                Util.Logcat.e("zero 4" + EdAmount.getEditableText().toString());
                                                EdAmount.setText("0");
                                            }

                                            StrHour = getlabel.getString("FlatTo");
                                            if (Integer.parseInt(StrHour) > 24) {
                                                StrDay = String.valueOf(Integer.parseInt(StrHour) / 24);
                                            }
                                            Util.Logcat.e("StrDay>7" + StrDay);
                                            VehicleTypeID = object.getString("VehicleTypeId");
                                            NCGraceTo = object.getString("NCGraceTo");
                                            if (Integer.parseInt(NCGraceTo) > 0 && IsFOC.equalsIgnoreCase("0")) {
                                                IsFOC = "2";
                                                TxtGraceHour.setText("HOUR " + "(" + "Grace Time: " + NCGraceTo + " Mins" + ")");
                                                EdAmount.setText("0");
                                            } else {
                                                TxtGraceHour.setText("HOUR");
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        Util.Logcat.e("AMT:::" + finalamt);

        return finalamt;
    }

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
                        //updateamount();
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

            case R.id.scan:
                Intent ScanPlate = new Intent(getActivity(), ScanPlate.class);
                startActivity(ScanPlate);
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

                //int ExtraHour=0;
                if (EdVehicleNo.getEditableText().toString().isEmpty()) {
                    alert.build(getString(R.string.vehno_verify));
                } else if (VehicleType.isEmpty()) {
                    alert.build(getString(R.string.select_vehicle_type));
                } else {
                    offlineVerify();
                }
                break;

            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void offlineVerify() {

        int extratime = 0;
        booleanvehicle = false;
        VIP = false;

        Cursor res = TransactionDB.getAllData();
        for (res.moveToLast(); !res.isBeforeFirst(); res.moveToPrevious()) {

           /* if (Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("1")) {
                Util.Logcat.e("Isfoc" + Util.DECODE64(res.getString(VehNo)) + "FINAL" + Util.DECODE64(res.getString(Isfoc)));
            }*/

            if (Util.DECODE64(res.getString(VehNo)).equalsIgnoreCase(EdVehicleNo.getEditableText().toString()) && Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase(VehicleTypeID) && Util.DECODE64(res.getString(Streetid)).equalsIgnoreCase(STREETID) && Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("0")) {
                booleanvehicle = true;
                String oldtime = Util.DECODE64(res.getString(Expirytime));
                Util.Logcat.e("oldtime" + oldtime);
                extratime = Integer.parseInt(Util.showextra(oldtime, Util.parkingtime(0)));
                Parktime = Util.showparkingtime(Util.DECODE64(res.getString(Expirytime)), Util.parkingtime(0));
                Entry = Util.DECODE64(res.getString(Expirytime));
                Expiry = Util.DECODE64(res.getString(Expirytime));
                Util.Logcat.e("Parktime" + Parktime + "FINAL");
                //PrevTrasnID = Util.DECODE64(res.getString(TransId));
                //Util.Logcat.e("PrevTrasnID" + PrevTrasnID);
                //PrevEntryTimeStr = Util.DECODE64(res.getString(Entrytime));
                MobileNumber = Util.DECODE64(res.getString(MobileNo));
                break;
            } else if (Util.DECODE64(res.getString(VehNo)).equalsIgnoreCase(EdVehicleNo.getEditableText().toString()) && Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("1")) {
                VIP = true;
                booleanvehicle = true;
                //Util.Logcat.e("VIP" + Util.DECODE64(res.getString(VehNo)) + "FINAL");
                break;
            } else if (Util.DECODE64(res.getString(VehNo)).equalsIgnoreCase(EdVehicleNo.getEditableText().toString()) && Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase(getvehicleid(VehicleType)) && Util.DECODE64(res.getString(Streetid)).equalsIgnoreCase(STREETID) && Util.DECODE64(res.getString(Isfoc)).equalsIgnoreCase("2")) {
                booleanvehicle = true;
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
            // if (extratime > 0 && VehicleType.equalsIgnoreCase("Two Wheeler") || VehicleType.equalsIgnoreCase("Cycles")) {

            if (VIP) {
                alert.build("Vehicle Already Available No Extra Charges");
            } else if (extratime > 0 && extratime <= 12) {
                String amt = getfineamount(VehicleType, extratime);
                //vehicle available extraamount is X
                Util.Logcat.e("IF:::" + amt);
                SaveDataLessthanTWOFOUR(extratime);
                JSONObject data = new JSONObject();
                try {
                    data.put("Transid", TRANSID);
                    data.put("Duration", StrHour);
                    //data.put("Duration", Parktime);
                    data.put("FareAmount", amt);
                    data.put("EntryTime", Entry);
                    data.put("ExpiryTime", Expiry);
                    data.put("VehicleTypeID", VehicleTypeID);
                    data.put("VehicleNo", EdVehicleNo.getEditableText().toString());
                    data.put("StreetId", STREETID);
                    // data.put("Duration", extratime + " Hours");
                    data.put("HourDuration", Parktime);
                    data.put("IsGraceHour", "0");
                    data.put("GraceHour", "0");
                    data.put("GraceFee", "0");
                    data.put("IsFOC", "0");
                    data.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
                    data.put("ServerTransid", "0");
                    data.put("PaymentMode", StrPayment);
                    data.put("MobileNumber", MobileNumber);
                    data.put("StreetName", StreetName);
                    data.put("Datetime", Util.getdatetime());
                    data.put("StatusDesc", "Vehicle Already Available");

                    if (amt.equalsIgnoreCase("0")) {
                        alert.build("Vehicle Already Available. Additional Changes : 0 ");
                    } else {
                        BuildAlert(data, "from < 12", true);
                    }

                    Util.Logcat.e(">1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (extratime > 12) {
                String amt = above12hours(VehicleType, extratime);
                SaveDataGreaterthanTWOFOUR(extratime);
                JSONObject data = new JSONObject();
                try {
                    data.put("Transid", TRANSID);
                    data.put("Duration", StrHour);
                    // data.put("Duration", Parktime);
                    data.put("FareAmount", amt);
                    data.put("EntryTime", Entry);
                    data.put("ExpiryTime", Expiry);
                    //data.put("ExpiryTime", Expiry);
                    data.put("VehicleTypeID", VehicleTypeID);
                    data.put("VehicleNo", EdVehicleNo.getEditableText().toString());
                    data.put("StreetId", STREETID);

                    /*if (Integer.parseInt(StrDay) > 0) {
                        data.put("HourDuration", StrDay + " " + Parktime);
                    } else {

                    }*/

                    data.put("HourDuration", Parktime);
                    data.put("IsGraceHour", "0");
                    data.put("GraceHour", "0");
                    data.put("GraceFee", "0");
                    data.put("IsFOC", "0");
                    data.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
                    data.put("ServerTransid", "0");
                    data.put("PaymentMode", StrPayment);
                    data.put("MobileNumber", MobileNumber);
                    data.put("StreetName", StreetName);
                    data.put("Datetime", Util.getdatetime());
                    data.put("StatusDesc", "Vehicle Already Available");
                    BuildAlert(data, "from > 12", true);
                    Util.Logcat.e(">2");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                alert.build("Vehicle Already Available No Extra Charges");
                // vehicle available No extra Amount
                // SaveDataNoExtraCharges();
               /* JSONObject data = new JSONObject();
                try {
                    data.put("Transid", TRANSID);
                    data.put("Duration", "0");
                    // data.put("Duration", Parktime);
                    data.put("FareAmount", "0");

                    // data.put("EntryTime", PrevEntryTimeStr);
                    data.put("ExpiryTime", Util.parkingtime(0));
                    //data.put("ExpiryTime", Expiry);
                    data.put("VehicleTypeID", VehicleTypeID);
                    data.put("VehicleNo", EdVehicleNo.getEditableText().toString());
                    data.put("StreetId", STREETID);
                    data.put("HourDuration", "0");
                    data.put("IsGraceHour", "0");
                    data.put("GraceHour", "0");
                    data.put("GraceFee", "0");
                    data.put("IsFOC", "0");
                    data.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
                    data.put("ServerTransid", "0");
                    data.put("PaymentMode", StrPayment);
                    data.put("MobileNumber", MobileNumber);
                    data.put("StreetName", StreetName);
                    data.put("Datetime", Util.getdatetime());
                    data.put("StatusDesc", "Vehicle Already Available");
                    BuildAlert(data, "Vehicle Already Available No Extra Charges", false);

                    Util.Logcat.e(">2");
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

            }
        } else {
            alert.build("Vehicle Not Available");
        }

    }

    //Servicecall
    private void SaveDataLessthanTWOFOUR(Integer extratime) {
        Expiry = Util.parkingtime(0);
        Util.Logcat.e("NEW Entry" + Entry);
        Util.Logcat.e("NEW Expiry" + Expiry);
    }

    private void SaveDataGreaterthanTWOFOUR(Integer extratime) {

        Util.Logcat.e("ENTRY:" + Entry);

        int time = Integer.valueOf(Noof12) * 12;
        //Expiry = Util.sumdate(Entry, time);
        //Expiry = Util.sumdate(Entry, Integer.valueOf(extratime));
        Expiry = Util.parkingtime(0);
        StrHour = String.valueOf(time);
        Util.Logcat.e("Duration" + StrHour);
        Util.Logcat.e("parktime" + Parktime);
        Util.Logcat.e("Expiry:1" + Expiry);
        Util.Logcat.e("StrDay>8" + StrDay);
    }

    private String above12hours(String vehicleType, int extratime) {
        Util.Logcat.e("EXTRA:" + extratime);
        String fineamt = "";
        try {
            JSONObject resobject = new JSONObject(TARIFF);
            JSONArray jsonArray = resobject.optJSONArray("_VechicleTraifflist");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject innerOBj = jsonArray.getJSONObject(i);
                    if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                        JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                        if (innerArray != null) {
                            for (int j = 0; j < innerArray.length(); j++) {
                                JSONObject object = innerArray.getJSONObject(j);
                                if (VehicleType.equalsIgnoreCase(object.getString("VehicleType"))) {
                                    JSONArray tariffarray = object.optJSONArray("Tariff");
                                    for (int k = 0; k < tariffarray.length(); k++) {
                                        JSONObject getlabel = tariffarray.getJSONObject(k);
                                        int from = Integer.parseInt(getlabel.getString("FlatFrom"));
                                        int to = Integer.parseInt(getlabel.getString("FlatTo"));
                                        Util.Logcat.e("12 RANGE:" + from);
                                        //Time slab comparison
                                        // if (from >= time && time <= to) {
                                        if (from >= 8) {
                                            Util.Logcat.e("12 INSIDE:" + from);
                                            int tempday = extratime / 12;
                                            int tempday2 = extratime % 12;
                                            if (tempday2 > 0) {
                                                tempday++;
                                            }

                                            Util.Logcat.e("tempday:" + tempday);
                                            int day = (int) Math.ceil(tempday);
                                            int tempday24 = extratime / 24;
                                            int tempday224 = extratime % 24;
                                            if (tempday224 > 0) {
                                                tempday24++;
                                            }

                                            StrDay = String.valueOf(tempday24);
                                            Noof12 = String.valueOf(tempday);
                                            //StrDay = String.valueOf(tempday);
                                            Util.Logcat.e("StrDay>5" + StrDay);
                                            Util.Logcat.e("day:" + day);
                                            Util.Logcat.e("From:" + from);
                                            Util.Logcat.e("To:" + to);
                                            // String amt = tariffarray.getJSONObject(0).getString("FlatTariff");
                                            String amt = getlabel.getString("FlatTariff");
                                            StrHour = getlabel.getString("TariffDetailId");
                                            // StrHour = getlabel.getString("FlatTo");
                                            Util.Logcat.e("IF:" + StrHour);
                                            StrTimeSlab = getlabel.getString("FareLabel");
                                            Util.Logcat.e("IF:" + StrTimeSlab);
                                            fineamt = String.valueOf(day * Integer.parseInt(amt));
                                            Util.Logcat.e("IF:" + fineamt);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        EdAmount.setText(fineamt);
        Util.Logcat.e("fineamt:" + fineamt);
        return fineamt;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String getfineamount(String vehtype, Integer time) {

        Util.Logcat.e("EXTRA:" + time);
        String fineamt = "";
        try {
            JSONObject resobject = new JSONObject(TARIFF);
            JSONArray jsonArray = resobject.optJSONArray("_VechicleTraifflist");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject innerOBj = jsonArray.getJSONObject(i);
                    if (jsonArray.getJSONObject(i).getString("StreetId").equalsIgnoreCase(STREETID)) {
                        JSONArray innerArray = innerOBj.optJSONArray("_VechicleTraiff");
                        if (innerArray != null) {
                            for (int j = 0; j < innerArray.length(); j++) {
                                JSONObject object = innerArray.getJSONObject(j);
                                if (vehtype.equalsIgnoreCase(object.getString("VehicleType"))) {
                                    JSONArray tariffarray = object.optJSONArray("Tariff");
                                    for (int k = 0; k < tariffarray.length(); k++) {
                                        JSONObject getlabel = tariffarray.getJSONObject(k);
                                        int from = Integer.parseInt(getlabel.getString("FlatFrom"));
                                        int to = Integer.parseInt(getlabel.getString("FlatTo"));
                                        Util.Logcat.e("RANGE:" + from + time + to);
                                        //Time slab comparison
                                        // if (from >= time && time <= to) {
                                        if (time >= from && time <= to) {
                                            Util.Logcat.e("From:" + from);
                                            Util.Logcat.e("To:" + to);
                                            fineamt = getlabel.getString("FlatTariff");
                                            //StrHour = getlabel.getString("TariffDetailId");
                                            StrHour = getlabel.getString("FlatTo");
                                            StrTimeSlab = getlabel.getString("FareLabel");
                                            getamount(VehicleType, StrTimeSlab);
                                            break;
                                        }
                                    }

                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return fineamt;
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
                            BuildAlert(data, "Vehicle Available", true);
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

    private void SaveData() {

       /* if (booleanvehicle) {
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
        }*/

        Entry = Util.parkingtime(0);
        if (IsFOC.equalsIgnoreCase("2")) {
            Expiry = Util.addgrace(Entry, Integer.valueOf(NCGraceTo));
        } else {
            if (Integer.parseInt(StrTimeSlab.substring(5, 7)) > 24) {
                int time = Integer.valueOf(StrDay) * 24;
                Log.e("time", ":" + time);
                Expiry = Util.parkingtime(time);
                StrHour = String.valueOf(time);
                Log.e("+++++", StrHour);
                Util.Logcat.e("Duration" + StrHour);
                Util.Logcat.e("parktime" + Parktime);
                Util.Logcat.e("Expiry:1" + Expiry);
                Util.Logcat.e("StrDay>8:" + StrDay);
            } else {
                Expiry = Util.parkingtime(Integer.valueOf(StrTimeSlab.substring(5, 7)));
                Util.Logcat.e("Expiry:2" + Expiry);
            }

            //Expiry = Util.parkingtime(Integer.valueOf(StrHour));

        }

        TRANSID = Util.GenerateTransactionId(Util.getData("terminalid", getActivity().getApplicationContext()));
        MobileNumber = EdMobileNo.getEditableText().toString();
        Util.Logcat.e("IsFOC 3:" + IsFOC);
        Util.Logcat.e("INSERT Expiry ::" + Expiry);
        boolean isInserted = TransactionDB.insertData(TRANSID, VehicleTypeID, EdVehicleNo.getEditableText().toString(), STREETID,
                StrHour, EdAmount.getEditableText().toString(), "0", "0", "0", IsFOC,
                Util.getData("UserId", getActivity().getApplicationContext()), Entry, Expiry, "0",
                StrPayment, MobileNumber, StreetName, Util.parkingtime(0));
        if (isInserted == true) {
            Toast.makeText(getActivity(), "Data Inserted", Toast.LENGTH_LONG).show();
        }
        PrintReciept();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void LoadPaymentSpinner() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            obj.put("SourceType", "1");
            Util.Logcat.e("PAYMENT:::" + obj.toString());
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

    //Encrypted Data:
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
                        // IsFOC = "0";
                        VehicleType = ((TextView) view.findViewById(R.id.vehicle_type)).getText().toString();
                        getvehicleid(VehicleType);
                        LoadHourspinnerNew(VehicleType);
                        EdVehicleNo.requestFocus();

                       /* if (!VehicleType.isEmpty() && !StrTimeSlab.isEmpty()) {
                            getamount(VehicleType, StrTimeSlab);
                        }*/
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
        StrTimeSlab = "";
        TRANSID = "";
        Entry = "";
        Expiry = "";
        Noof12 = "0";
        VehicleTypeID = "";
        VehicleType = "";
        Parktime = "";
        StrHour = "";
        StrDay = "0";
        //PrevTrasnID = "";
        // PrevEntryTimeStr = "";
        TRANSID = "";
        Noof12 = "0";
        Entry = "";
        Expiry = "";
        MobileNumber = "";
        IsFOC = "0";
        VIP = false;
        VehicleTypeID = "";
        VehicleType = "";
        //StrPayment = "";
        TxtGraceHour.setText("HOUR");
        CheckGraceHour = 0;
        booleanvehicle = false;
        ExtraFOC = false;
        Parktime = "";
        extraamount = false;
        LayoutPayment.setVisibility(View.VISIBLE);
        labeledSwitch.setChecked(false);
        // LoadStreetSpinner();
        EdVehicleNo.setEnabled(true);
        //SpinHour.setSelection(0);
        //LoadHourspinnerNew();
        if (!STREETID.isEmpty())
            LoadVehicle(STREETID);

    }

    private void PrintReciept() {

        StringBuffer printvalues = new StringBuffer();
        printvalues.append(getString(R.string.onstreet_parking));
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
        printvalues.append("\n" + getString(R.string.line));
        printvalues.append("\n" + "Pwd by GI Retail Pvt.Ltd");
        printvalues.append("\n" + "  Mgd by GRGK Pvt. Ltd");
        printvalues.append("\n" + " PARKING AT OWNERS RISK" + "\n\n\n\n");

        MainActivity.printContent = printvalues.toString();
        Util.Logcat.e(printvalues.toString());
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

    public void BuildAlert(final JSONObject data, String from, final boolean print) {
        Util.Logcat.e("from" + from);
        Util.Logcat.e("data" + data.toString());
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.chck_alert_new_dialog);
        // set the custom dialog components - text, image and button

        TextView Tvstatus = dialog.findViewById(R.id.status);
        TextView Tvextrahour = dialog.findViewById(R.id.extrahour);
        TextView Tvaddition = dialog.findViewById(R.id.addition);
        TextView Tventry = dialog.findViewById(R.id.entry);
        TextView Tvexit = dialog.findViewById(R.id.exit);
        TextView Tvduration = dialog.findViewById(R.id.duration);

        final Spinner payment = dialog.findViewById(R.id.payment_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (getActivity(), R.layout.spinner_textview,
                        Paymentlist); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        payment.setAdapter(spinnerArrayAdapter);
        payment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {

                try {
                    for (int i = 0; i < Paymentarray.length(); i++) {
                        JSONObject jsonobject = Paymentarray.getJSONObject(i);
                        if (payment.getSelectedItem().toString().equalsIgnoreCase(jsonobject.getString("PaymentMode"))) {
                            StrPayment = jsonobject.getString("PaymentModeId");
                            Util.Logcat.e("spinner" + StrPayment);
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
        String duration = "";
        // image.setImageResource(R.mipmap.ic_launcher);
        Button BtnPrint = dialog.findViewById(R.id.print);
        if (print == false) {
            BtnPrint.setText("Exit");
        }

        try {
            if ("online".equalsIgnoreCase(from)) {
                duration = data.getString("timeslab");
            } else {
                //duration = data.getString("HourDuration");
                duration = data.getString("Duration").replaceAll("Days", "").replaceAll("Hours", "").trim();
            }
        } catch (JSONException e) {

        }
        final String finalDuration = duration;
        Util.Logcat.e("finalDuration::::" + finalDuration);

        // image.setImageResource(R.mipmap.ic_launcher);

        BtnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                TRANSID = Util.GenerateTransactionId(Util.getData("terminalid", getActivity().getApplicationContext()));
                try {
                    boolean isInserted = TransactionDB.insertData(TRANSID,
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
                    printvalues.append("\n" + data.getString("StreetName"));
                    printvalues.append("\n" + "V.No   :" + data.getString("VehicleNo"));
                    printvalues.append("\n" + "V.Type:" + VehicleType);
                    if (!data.getString("MobileNumber").isEmpty()) {
                        printvalues.append("\n" + "Mobile No:" + data.getString("MobileNumber"));
                    }
                    if (data.getString("IsFOC").equalsIgnoreCase("1")) {
                        printvalues.append("\n" + "FOC   :" + "Yes");
                    }
                    printvalues.append("\n" + "Txn ID:" + TRANSID);
                    //printer.printText("Date     :" + Util.parkingtime(0));
                    printvalues.append("\n" + "Entry :" + data.getString("EntryTime"));
                    printvalues.append("\n" + "Expiry:" + data.getString("ExpiryTime"));
                    printvalues.append("\n" + "Hours    :" + data.getString("Duration"));
                    printvalues.append("\n" + "Duration :" + data.getString("HourDuration"));
                    printvalues.append("\n" + "Amount   :" + "Rs." + data.getString("FareAmount"));
                    printvalues.append("\n" + getString(R.string.line));
                    printvalues.append("\n" + "Pwd by GI Retail Pvt.Ltd");
                    printvalues.append("\n" + "  Mgd by GRGK Pvt. Ltd");
                    printvalues.append("\n" + " PARKING AT OWNERS RISK" + "\n\n\n\n");
                    if (print == true) {
                        Util.Logcat.e("PRINT:" + "TRUE");
                        MainActivity.printContent = printvalues.toString();
                        Util.Logcat.e("PRINT" + printvalues.toString());
                        MainActivity.getInstance().CommonPrint();
                    } else {
                        Util.Logcat.e("PRINT:" + "FALSE");
                    }
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