package com.sidcoparking.module;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.activity.MainActivity;
import com.sidcoparking.adapter.TransactionAdapter;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sidcoparking.DataBase.DB_Constant.Entrytime;
import static com.sidcoparking.DataBase.DB_Constant.Expirytime;
import static com.sidcoparking.DataBase.DB_Constant.GraceFee;
import static com.sidcoparking.DataBase.DB_Constant.GraceHour;
import static com.sidcoparking.DataBase.DB_Constant.IsGraceHour;
import static com.sidcoparking.DataBase.DB_Constant.Isfoc;
import static com.sidcoparking.DataBase.DB_Constant.MobileNo;
import static com.sidcoparking.DataBase.DB_Constant.ParkingFee;
import static com.sidcoparking.DataBase.DB_Constant.ParkingHour;
import static com.sidcoparking.DataBase.DB_Constant.Streetid;
import static com.sidcoparking.DataBase.DB_Constant.Streetname;
import static com.sidcoparking.DataBase.DB_Constant.TransId;
import static com.sidcoparking.DataBase.DB_Constant.VehNo;
import static com.sidcoparking.DataBase.DB_Constant.VehTypeId;
import static com.sidcoparking.utils.Util.CHECK_VEHICLE;
import static com.sidcoparking.utils.Util.ONLINE_PRINT;


public class Transaction extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    HashMap<String, String> DataHashMap;
    List<Map<String, String>> ListCollection;
    List<Map<String, String>> searchResults;
    TransactionAdapter adapter;
    ListView listView;
    EditText EdSearch, vehno;
    CommonAlertDialog alert;
    DatabaseHelper TransactionDB;
    TextView TxtNoData;
    Button btn_print;
    private OnFragmentInteractionListener mListener;


    public Transaction() {
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
    public static Transaction newInstance(String param1, String param2) {
        Transaction fragment = new Transaction();
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

        View rootView = inflater.inflate(R.layout.transaction, container, false);
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
        TransactionDB = new DatabaseHelper(getActivity());
        alert = new CommonAlertDialog(getActivity());
        listView = rootView.findViewById(R.id.listview);
        TxtNoData = rootView.findViewById(R.id.nodata);
        ListCollection = new ArrayList<>();
        btn_print = rootView.findViewById(R.id.btn_print);
        vehno = rootView.findViewById(R.id.vehno);
        EdSearch = rootView.findViewById(R.id.search);
        EdSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vehno.getEditableText().toString().isEmpty()) {
                    alert.build(getString(R.string.enter_vehno));
                } else if (vehno.getEditableText().toString().length() != 4) {
                    alert.build("Enter Valid Vehicle No");
                } else {
                    CheckVehicleOnline();
                }
            }
        });


        GetVehicelFromDB();
        return rootView;
    }

    private void CheckVehicleOnline() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("ATDId", Util.getData("UserId", getActivity().getApplicationContext()));
            obj.put("VehicleNo", vehno.getEditableText().toString());

            Util.Logcat.e("ONLINE PRINT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), ONLINE_PRINT, new VolleyResponseListener() {
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
                        Util.Logcat.e("ONLINE PRINT:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            // alert.build(resobject.getString("StatusDesc"));
                            PrintAlert(resobject);
                            //  vehno.setText("");
                        } else {
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

    private void PrintAlert(final JSONObject jsonObject) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.onlineprint);
        TextView TxnId = dialog.findViewById(R.id.TxnId);
        TextView VehicleNo = dialog.findViewById(R.id.VehicleNo);
        //TextView Tvaddition = dialog.findViewById(R.id.addition);
        TextView Tventry = dialog.findViewById(R.id.entry);
        TextView Tvexit = dialog.findViewById(R.id.exit);
        TextView Tvduration = dialog.findViewById(R.id.duration);
        TextView ParkingFee = dialog.findViewById(R.id.ParkingFee);

        try {
            StringBuffer showdata = new StringBuffer();
            showdata.append("               "+getString(R.string.onstreet_parking));
            showdata.append("\n"+"                  "+getString(R.string.reprint_ticket));
            showdata.append("\n" + getString(R.string.line_big));
            showdata.append("\n" + jsonObject.getString("StreetName"));
            showdata.append("\n" + "V.No   : " + jsonObject.getString("VehicleNo"));
            showdata.append("\n" + "V.Type: " + jsonObject.getString("VehicleType"));
            if (!jsonObject.getString("Slot").isEmpty() && !jsonObject.getString("Slot").equalsIgnoreCase("null")) {
                showdata.append("\n" + "Slot     : " + jsonObject.getString("Slot"));
            }
            if (!jsonObject.getString("MobileNumber").isEmpty() && !jsonObject.getString("MobileNumber").equalsIgnoreCase("null")) {
                showdata.append("\n" + "Mobile No: " + jsonObject.getString("MobileNumber"));
            }
            if (jsonObject.getString("IsFOC").equalsIgnoreCase("1")) {
                showdata.append("\n" + "FOC   : " + "Yes");
            }
            showdata.append("\n" + "Txn ID: " + jsonObject.getString("TxnId"));
            showdata.append("\n" + "Entry : " + jsonObject.getString("EntryTime"));
            showdata.append("\n" + "Expiry: " + jsonObject.getString("ExpiryTime"));
            if (jsonObject.getString("IsFOC").equalsIgnoreCase("2")) {
                String StrHour = Util.showextrabyminutes(jsonObject.getString("ExpiryTime"), jsonObject.getString("EntryTime") + "\n");
                showdata.append("\n"+"Hours    : " + StrHour + " Mins Grace Time");
            } else {
                showdata.append("\n" + "Hours : " + jsonObject.getString("DurationHours"));
            }
            showdata.append("\n" + "Amount   : " + "Rs." + jsonObject.getString("ParkingFee"));
            TxnId.setText(showdata);
            /*TxnId.setText("TxnId :" + jsonObject.getString("TxnId"));
            VehicleNo.setText("V.No :" + jsonObject.getString("VehicleNo"));
            Tventry.setText("Entry :" + jsonObject.getString("EntryTime"));
            Tvexit.setText("Expiry:" + jsonObject.getString("ExpiryTime"));
            Tvduration.setText("Duration :" + jsonObject.getString("DurationHours"));
            ParkingFee.setText("Amount   :" + jsonObject.getString("ParkingFee"));*/
        } catch (JSONException e) {
            Util.Logcat.e("JSONException:" + e.toString());
        }

        Button BtnPrint = dialog.findViewById(R.id.print);
        BtnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    dialog.dismiss();
                    vehno.setText("");

                    StringBuffer printvalues = new StringBuffer();

                    printvalues.append(getString(R.string.onstreet_parking));
                    printvalues.append("\n"+getString(R.string.reprint_ticket) );
                    printvalues.append("\n" + getString(R.string.line));
                    printvalues.append("\n" + jsonObject.getString("StreetName"));
                    printvalues.append("\n" + "V.No   :" + jsonObject.getString("VehicleNo"));
                    printvalues.append("\n" + "V.Type:" + jsonObject.getString("VehicleType"));

                    if (!jsonObject.getString("Slot").isEmpty() && !jsonObject.getString("Slot").equalsIgnoreCase("null")) {
                        printvalues.append("\n" + "Slot     :" + jsonObject.getString("Slot"));
                    }

                    if (!jsonObject.getString("MobileNumber").isEmpty() && !jsonObject.getString("MobileNumber").equalsIgnoreCase("null")) {
                        printvalues.append("\n" + "Mobile No:" + jsonObject.getString("MobileNumber"));
                    }

                    if (jsonObject.getString("IsFOC").equalsIgnoreCase("1")) {
                        printvalues.append("\n" + "FOC   :" + "Yes");
                    }

                    printvalues.append("\n" + "Txn ID:" + jsonObject.getString("TxnId"));
                    printvalues.append("\n" + "Entry :" + jsonObject.getString("EntryTime"));
                    printvalues.append("\n" + "Expiry:" + jsonObject.getString("ExpiryTime"));

                    if (jsonObject.getString("IsFOC").equalsIgnoreCase("2")) {
                        String StrHour = Util.showextrabyminutes(jsonObject.getString("ExpiryTime"), jsonObject.getString("EntryTime") + "\n");

                        printvalues.append("\n"+"Hours    :" + StrHour + " Mins Grace Time");
                    } else {
                        printvalues.append("\n" + "Hours :" + jsonObject.getString("DurationHours"));
                    }

                    printvalues.append("\n" + "Amount   :" + "Rs." + jsonObject.getString("ParkingFee"));
                    printvalues.append("\n" + getString(R.string.line));
                    printvalues.append("\n" + "Pwd by GI Retail Pvt.Ltd");
                    printvalues.append("\n" + "  Mgd by GRGK Pvt. Ltd");
                    printvalues.append("\n" + " PARKING AT OWNERS RISK" + "\n\n\n\n");

                    Util.Logcat.e("PRINT:" + "TRUE");

                    MainActivity.printContent = printvalues.toString();
                    Util.Logcat.e("PRINT" + printvalues.toString());
                    MainActivity.getInstance().CommonPrint();

                } catch (JSONException e) {
                    Util.Logcat.e("JSONException:" + e.toString());
                }
            }
        });
        Button BtnCancel = dialog.findViewById(R.id.cancel);
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        dialog.setCancelable(false);
        dialog.show();


    }

    private void GetVehicelFromDB() {
        final Cursor res = TransactionDB.getAllData();
        if (res.getCount() == 0) {
            listView.setVisibility(View.GONE);
            EdSearch.setVisibility(View.GONE);
            TxtNoData.setVisibility(View.VISIBLE);
            return;
        }
        //pos.add(0);
        for (res.moveToLast(); !res.isBeforeFirst(); res.moveToPrevious()) {
            DataHashMap = new HashMap<>();
            DataHashMap.put("TransId", Util.DECODE64(res.getString(TransId)));
            if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("1")) {
                DataHashMap.put("VehicleType", "Car");
            } else if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("2")) {
                DataHashMap.put("VehicleType", "Two Wheeler");
            } else if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("3")) {
                DataHashMap.put("VehicleType", "Auto Rickshaw");
            } else if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("4")) {
                DataHashMap.put("VehicleType", "Cycle Rickshaw");
            } else if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("5")) {
                DataHashMap.put("VehicleType", "Bus");
            } else if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("6")) {
                DataHashMap.put("VehicleType", "HCV");
            } else if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("7")) {
                DataHashMap.put("VehicleType", "LCV");
            } else if (Util.DECODE64(res.getString(VehTypeId)).equalsIgnoreCase("8")) {
                DataHashMap.put("VehicleType", "Mini Bus");
            }

            DataHashMap.put("VehicleNo", Util.DECODE64(res.getString(VehNo)));
            DataHashMap.put("Streetid", Util.DECODE64(res.getString(Streetid)));
            DataHashMap.put("ParkingHour", Util.DECODE64(res.getString(ParkingHour)));
            DataHashMap.put("ParkingFee", Util.DECODE64(res.getString(ParkingFee)));
            DataHashMap.put("IsGraceHour", Util.DECODE64(res.getString(IsGraceHour)));
            DataHashMap.put("GraceHour", Util.DECODE64(res.getString(GraceHour)));
            DataHashMap.put("GraceFee", Util.DECODE64(res.getString(GraceFee)));
            DataHashMap.put("Isfoc", Util.DECODE64(res.getString(Isfoc)));
            DataHashMap.put("Entrytime", Util.DECODE64(res.getString(Entrytime)));
            DataHashMap.put("Expirytime", Util.DECODE64(res.getString(Expirytime)));
            DataHashMap.put("MobileNo", Util.DECODE64(res.getString(MobileNo)));
            DataHashMap.put("StreetName", Util.DECODE64(res.getString(Streetname)));
            ListCollection.add(DataHashMap);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                searchResults = ListCollection;
                adapter = new TransactionAdapter(getActivity(), ListCollection);
                listView.setAdapter(adapter);
            }
        });


       /* final List<Map<String, String>> hai = ListCollection;
       // Collections.reverse(hai);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                long viewId = view.getId();
                if (viewId == R.id.btn_reprint) {


                    for(Map<String,String> dfas:ListCollection){

                    }

                    Toast.makeText(getActivity(), position + "\nHAI", Toast.LENGTH_SHORT).show();
                    Log.e("VEHNO", hai.get(position).get("VehicleNo"));
                    StringBuffer printvalues = new StringBuffer();
                    if (hai.get(position).get(
                            "Isfoc").equalsIgnoreCase("2")) {
                        printvalues.append(getString(R.string.onstreet_parking)+ "\n");
                        printvalues.append(getString(R.string.reprint_ticket)+ "\n");
                        printvalues.append(getString(R.string.line) + "\n");
                        printvalues.append(hai.get(position).get(
                                "StreetName") + "\n");
                        printvalues.append("V.No     :" + hai.get(position).get(
                                "VehicleNo") + "\n");
                        printvalues.append("V.Type   :" + hai.get(position).get(
                                "VehicleType") + "\n");
                        if (!hai.get(position).get(
                                "MobileNo").isEmpty()) {
                            printvalues.append("Mobile No:" + hai.get(position).get(
                                    "MobileNo") + "\n");
                        }
                        if (hai.get(position).get(
                                "Isfoc").equalsIgnoreCase("1")) {
                            printvalues.append("FOC      :" + "Yes" + "\n");
                        }

                        printvalues.append("Txn ID   :" + hai.get(position).get(
                                "TransId") + "\n");

                        printvalues.append("Entry    :" + hai.get(position).get(
                                "Entrytime") + "\n");

                        String StrHour = Util.showextrabyminutes(hai.get(position).get(
                                "Expirytime"), hai.get(position).get(
                                "Entrytime") + "\n");

                        printvalues.append("Hours    :" + StrHour + " Mins Grace Time" + "\n");

                        printvalues.append("Amount   :" + "Rs." + hai.get(position).get(
                                "ParkingFee") + "\n");
                        printvalues.append(getString(R.string.line) + "\n");
                        printvalues.append(getString(R.string.mdg_by_gerek) + "\n");
                        printvalues.append(getString(R.string.pwd_by_giretail)+ "\n");
                        printvalues.append(getString(R.string.parking_ownrisk)+ "\n");

                    } else {
                        printvalues.append(getString(R.string.onstreet_parking)+ "\n");
                        printvalues.append(getString(R.string.reprint_ticket)+ "\n");
                        printvalues.append(getString(R.string.line) + "\n");
                        printvalues.append(hai.get(position).get(
                                "StreetName") + "\n");
                        printvalues.append("V.No     :" + hai.get(position).get(
                                "VehicleNo") + "\n");
                        printvalues.append("V.Type   :" + hai.get(position).get(
                                "VehicleType") + "\n");

                        if (!hai.get(position).get(
                                "MobileNo").isEmpty()) {
                            printvalues.append("Mobile No:" + hai.get(position).get(
                                    "MobileNo") + "\n");
                        }
                        if (hai.get(position).get(
                                "Isfoc").equalsIgnoreCase("1")) {
                            printvalues.append("FOC      :" + "Yes" + "\n");
                        }
                        printvalues.append("Txn ID   :" + hai.get(position).get(
                                "TransId") + "\n");
                        printvalues.append("Entry:" + hai.get(position).get(
                                "Entrytime") + "\n");

                        printvalues.append("Expiry:" + hai.get(position).get(
                                "Expirytime") + "\n");
                        printvalues.append("Hours    :" + hai.get(position).get(
                                "ParkingHour") + "\n");
                        printvalues.append("Amount   :" + "Rs." + hai.get(position).get(
                                "ParkingFee") + "\n");
                        printvalues.append(getString(R.string.line) + "\n");
                        printvalues.append(getString(R.string.mdg_by_gerek) + "\n");
                        printvalues.append(getString(R.string.pwd_by_giretail)+ "\n");
                        printvalues.append(getString(R.string.parking_ownrisk)+ "\n");
                    }
                    MainActivity.printContent = printvalues.toString();
                    MainActivity.getInstance().CommonPrint();
                }

            }
        });*/
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("TRANSACTION");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.start_stop);
        item.setVisible(false);
    }


}