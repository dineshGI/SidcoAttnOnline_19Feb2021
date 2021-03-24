package com.sidcoparking.module;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
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
import android.widget.TextView;

import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.activity.MainActivity;
import com.sidcoparking.adapter.VehicleListAdapter;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.ExpandableHeightGridView;
import com.sidcoparking.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sidcoparking.utils.Util.ADD_FINE_ENTRY;

public class Fine extends Fragment implements View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //https://api.mlab.com/api/1/databases/blindapp/collections/beacons?apiKey=EVDAtEeJwaIMAwOpjOOxdN2IiMmfLDJI

    private String mParam1;
    private String mParam2;
    String VehicleType = "", VehicleTypeId, StreetId, StreetName, TARIFF, TRANSID, FineTypeID, FineType, StrPayment;
    JSONArray streetarray, Paymentarray;
    EditText EdVehicleNo, EdAmount;
    Button BtnSubmit;
    Spinner SpinFineType, SpinStreet, SpinPayment;
    List<String> spinnerlist, Paymentlist;
    List<String> spinnerfinelist;

    private HashMap<String, String> DataHashMap;
    private List<Map<String, String>> ListCollection;
    VehicleListAdapter adapter;
    //List<String> VehTypeArray;
    ExpandableHeightGridView gridView;
    CommonAlertDialog alert;
    private OnFragmentInteractionListener mListener;


    public Fine() {
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

    public static Fine newInstance(String param1, String param2) {
        Fine fragment = new Fine();
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

        View rootView = inflater.inflate(R.layout.fine, container, false);
        // Inflate the layout for this fragment
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
        alert = new CommonAlertDialog(getActivity());
        TARIFF = Util.getData("tarifdetails", getActivity().getApplicationContext());
        gridView = rootView.findViewById(R.id.gridview);
        gridView.setExpanded(true);
        ListCollection = new ArrayList<>();

        spinnerlist = new ArrayList<>();
        Paymentlist = new ArrayList<>();
        Paymentarray = new JSONArray();
        spinnerfinelist = new ArrayList<>();
        streetarray = new JSONArray();

        EdVehicleNo = rootView.findViewById(R.id.vehicleno);
        EdAmount = rootView.findViewById(R.id.amount);

        BtnSubmit = rootView.findViewById(R.id.btn_fine);
        BtnSubmit.setOnClickListener(this);
        SpinFineType = rootView.findViewById(R.id.fine_spinner);

        SpinStreet = rootView.findViewById(R.id.street_spinner);
        SpinStreet.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        SpinStreet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                //Toast.makeText(spinner.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
                String streetname = SpinStreet.getSelectedItem().toString();
                Util.Logcat.e("streetname" + streetname);
                try {
                    for (int i = 0; i < streetarray.length(); i++) {
                        JSONObject jsonobject = streetarray.getJSONObject(i);
                        if (streetname.equalsIgnoreCase(jsonobject.getString("StreetName"))) {
                            StreetId = jsonobject.getString("StreetId");
                            StreetName = jsonobject.getString("StreetName");
                            Util.Logcat.e("streetid" + StreetId);
                            LoadVehicle(StreetId);
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //VehicleType = "";
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        SpinPayment = rootView.findViewById(R.id.payment_spinner);
        SpinPayment.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        SpinPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                //Toast.makeText(spinner.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
                String payment = SpinPayment.getSelectedItem().toString();
                Util.Logcat.e("selected payment" + payment);

                try {
                    for (int i = 0; i < Paymentarray.length(); i++) {
                        JSONObject jsonobject = Paymentarray.getJSONObject(i);
                        if (payment.equalsIgnoreCase(jsonobject.getString("PaymentMode"))) {
                            StrPayment = jsonobject.getString("PaymentModeId");
                            Util.Logcat.e("PaymentModeId" + StrPayment);
                            Util.Logcat.e("PaymentMode" + jsonobject.getString("PaymentMode"));
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

        SpinFineType.getBackground().setColorFilter(getActivity().getResources().getColor(
                R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

        SpinFineType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                //Toast.makeText(spinner.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
                String finetype = SpinFineType.getSelectedItem().toString();
                Util.Logcat.e("finetype" + finetype);

                try {
                    JSONObject data = new JSONObject(Util.getData("finedetails", getActivity().getApplicationContext()));
                    Util.Logcat.e("data" + String.valueOf(data));
                    JSONArray jsonArray = data.optJSONArray("_GetGetFineCharges");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject innerOBj = jsonArray.getJSONObject(i);
                        if (innerOBj.getString("FineType").equalsIgnoreCase(finetype)) {
                            EdAmount.setText(innerOBj.getString("FineAmount"));
                            FineTypeID = innerOBj.getString("FineTypeId");
                            FineType = innerOBj.getString("FineType");
                            Util.saveData("saveamountvalue", innerOBj.getString("FineAmount"), getActivity());
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fine:

                if (!(Util.getData("WorkStatus", getActivity().getApplicationContext()).equalsIgnoreCase("0"))) {
                    if (Integer.parseInt(EdAmount.getEditableText().toString()) > Integer.parseInt(Util.getData("saveamountvalue", getActivity()))) {
                        alert.build(getString(R.string.value_not_greater) + " " + Util.getData("saveamountvalue", getActivity()));
                    } else if (Integer.parseInt(EdAmount.getEditableText().toString()) == 0) {
                        alert.build(getString(R.string.not_equalto_zero));
                    } else if (VehicleType.isEmpty()) {
                        alert.build(getString(R.string.select_vehicle_type));
                    } else if (EdVehicleNo.getEditableText().toString().isEmpty()) {
                        alert.build(getString(R.string.enter_vehno));
                    } else {
                        if (EdVehicleNo.getEditableText().toString().length() == 1) {
                            EdVehicleNo.setText("000" + EdVehicleNo.getEditableText().toString());
                        } else if (EdVehicleNo.getEditableText().toString().length() == 2) {
                            EdVehicleNo.setText("00" + EdVehicleNo.getEditableText().toString());
                        } else if (EdVehicleNo.getEditableText().toString().length() == 3) {
                            EdVehicleNo.setText("0" + EdVehicleNo.getEditableText().toString());
                        }
                        if (!EdVehicleNo.getEditableText().toString().equalsIgnoreCase("0000")) {
                            FineEntry();
                        } else {
                            alert.build(getString(R.string.invalid_vehno));
                        }
                    }
                } else {
                    CommonAlertDialog alert = new CommonAlertDialog(getActivity());
                    alert.build(getString(R.string.start_msg));
                }
                break;
            default:
                break;
        }
    }

    private void FineEntry() {
        try {
            JSONObject obj = new JSONObject();
            TRANSID = Util.GenerateTransactionId(Util.getData("terminalid", getActivity().getApplicationContext()));
            obj.put("TerminalId", Util.getData("terminalid", getActivity().getApplicationContext()));
            obj.put("TransactionInfo", TRANSID + "~" + VehicleTypeId + "~" + EdVehicleNo.getEditableText().toString() + "~" + StreetId + "~" + FineTypeID + "~" + EdAmount.getEditableText().toString() + "~" + "0" + "~" + Util.getData("UserId", getActivity().getApplicationContext()) + "~" + StrPayment + "|");
            Util.Logcat.e("INPUT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), ADD_FINE_ENTRY, new VolleyResponseListener() {
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

                            PrintFinedetails();

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.alertDialog);
                            alertDialogBuilder.setMessage(resobject.getString("StatusDesc"));
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
                            alertDialogBuilder.setMessage(resobject.getString("StatusDesc").toString());
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

    private void PrintFinedetails() {
        //streetname
        StringBuffer printvalues = new StringBuffer();
        printvalues.append(getString(R.string.onstreet_parking));
        printvalues.append("\n" + getString(R.string.line));
        printvalues.append("\n" + StreetName);
        printvalues.append("\n" + "V.No     :" + EdVehicleNo.getEditableText().toString());
        printvalues.append("\n" + "V.Type  :" + VehicleType);
        printvalues.append("\n" + "Txn ID   :" + TRANSID);
        printvalues.append("\n" + "Date:" + Util.parkingtime(0));
        printvalues.append("\n" + "Fine Type:" + FineType);
        printvalues.append("\n" + "Amount   :" + "Rs." + EdAmount.getEditableText().toString());
        printvalues.append("\n" + getString(R.string.line));
        printvalues.append("\n" + "  Pwd by GI Retail Pvt.Ltd");
        printvalues.append("\n" + "   Mgd by GRGK Pvt. Ltd");
        printvalues.append("\n" + "  PARKING AT OWNERS RISK");
        MainActivity.printContent = printvalues.toString();
        MainActivity.getInstance().CommonPrint();
        cleardata();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("FINE");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getFineSpinnner();
        LoadStreetSpinner();
        LoadPaymentSpinner();
    }

    private void LoadPaymentSpinner() {

        try {
            JSONObject data = new JSONObject(Util.getData("SavePaymentMode", getActivity().getApplicationContext()));
            JSONArray jsonArray = data.optJSONArray("_GetPaymentMode");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject imageobject = jsonArray.getJSONObject(i);
                Util.Logcat.e("PaymentMode" + imageobject.getString("PaymentMode"));
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
        }


    }

    private void getFineSpinnner() {
        try {
            JSONObject data = new JSONObject(Util.getData("finedetails", getActivity().getApplicationContext()));
            Util.Logcat.e("data" + String.valueOf(data));
            JSONArray jsonArray = data.optJSONArray("_GetGetFineCharges");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject innerOBj = jsonArray.getJSONObject(i);
                spinnerfinelist.add(innerOBj.getString("FineType"));
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                            (getActivity(), android.R.layout.simple_spinner_item,
                                    spinnerfinelist); //selected item will look like a spinner set from XML
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);

                    SpinFineType.setAdapter(spinnerArrayAdapter);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.start_stop);
        item.setVisible(false);


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
                                Util.Logcat.e("VehicleType" + object.getString("VehicleType"));
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
                        //  selectedGroupBy = getHeaders().get(position);
                        ((TextView) view.findViewById(R.id.vehicle_type)).getText().toString();
                        VehicleType = ((TextView) view.findViewById(R.id.vehicle_type)).getText().toString();
                        for (Map<String, String> data : ListCollection) {
                            if (data.get("VehicleType").contains(VehicleType)) {
                                // color selection select item
                                VehicleTypeId = data.get("VehicleTypeId");
                                EdVehicleNo.requestFocus();
                                Util.Logcat.e("VehicleTypeId" + VehicleTypeId);
                                break;
                            }
                        }
                       /* if (!VehicleType.isEmpty() && !StrHour.isEmpty()) {
                            updateamount();
                        }*/
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

    private void cleardata() {
        streetarray = new JSONArray();
        Paymentarray = new JSONArray();
        Paymentlist.clear();
        spinnerlist.clear();
        spinnerfinelist.clear();
        EdAmount.setText("");
        EdVehicleNo.setText("");
        TRANSID = "";
        VehicleTypeId = "";
        VehicleType = "";
        LoadStreetSpinner();
        getFineSpinnner();
        LoadPaymentSpinner();
    }

}