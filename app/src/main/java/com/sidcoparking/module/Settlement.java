package com.sidcoparking.module;

import android.content.Context;
import android.content.pm.ActivityInfo;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.adapter.SettlementAdapter;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sidcoparking.utils.Util.GET_ATTENDERLIST;
import static com.sidcoparking.utils.Util.GET_SETTLEMENT;
import static com.sidcoparking.utils.Util.SETTLEMENT_COLLECTION;


public class Settlement extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //https://api.mlab.com/api/1/databases/blindapp/collections/beacons?apiKey=EVDAtEeJwaIMAwOpjOOxdN2IiMmfLDJI

    private String mParam1;
    private String mParam2;

    Spinner SpinAttender;
    // LinearLayout TxtAmount;

    Button BtnCollect;
    List<String> spinnerlist;
    JSONArray streetarray;
    String ATDId;
    CommonAlertDialog alert;
    String SettlementId;
    SettlementAdapter adapter;
    ListView listView;
    private HashMap<String, String> DataHashMap;
    private List<Map<String, String>> ListCollection;
    private OnFragmentInteractionListener mListener;

    public Settlement() {
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
    public static Settlement newInstance(String param1, String param2) {
        Settlement fragment = new Settlement();
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

        View rootView = inflater.inflate(R.layout.settlement, container, false);
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
        spinnerlist = new ArrayList<>();

        SpinAttender = rootView.findViewById(R.id.spin_attender);
        listView = rootView.findViewById(R.id.listview);
        ListCollection = new ArrayList<Map<String, String>>();
        BtnCollect = rootView.findViewById(R.id.btn_collect);

        BtnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettlementCollection();

            }
        });
        loadattenderlist();
        SpinAttender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                //Toast.makeText(spinner.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
                String name = SpinAttender.getSelectedItem().toString();
                Util.Logcat.e("finetype" + name);

                try {
                    for (int i = 0; i < streetarray.length(); i++) {
                        JSONObject jsonobject = streetarray.getJSONObject(i);
                        if (name.equalsIgnoreCase(jsonobject.getString("Name"))) {
                            GetSettlement(jsonobject.getString("ATDId"));
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

        return rootView;
    }

    private void SettlementCollection() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            obj.put("ATDId", ATDId);
            obj.put("SettlementId", SettlementId);
            Util.Logcat.e("INPUT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());

            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), SETTLEMENT_COLLECTION, new VolleyResponseListener() {
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
                        Util.Logcat.e("TARIFF:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            //_VechicleTraiff
                            ATDId = "";
                            alert.build(resobject.getString("StatusDesc"));
                            loadattenderlist();

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            alert.build(resobject.getString("StatusDesc"));
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

    private void GetSettlement(String attenderid) {
        ListCollection.clear();
        ATDId = attenderid;

        try {
            JSONObject obj = new JSONObject();
            obj.put("ATDId", attenderid);
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            Util.Logcat.e("SETTLEMENT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());

            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), GET_SETTLEMENT, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    if (message.contains("TimeoutError")) {
                        alert.build(getString(R.string.timeout_error));

                    } else {
                        alert.build(getString(R.string.server_error));
                    }
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {

                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));
                        Util.Logcat.e("SETTLEMENT with ID:::" + resobject.toString());

                        StringBuffer data = new StringBuffer();
                        if (resobject.getString("Status").equalsIgnoreCase("0")) {

                            JSONArray jsonArray = resobject.optJSONArray("_SubListGetSettlement");
                            // JSONArray Amountarray = resobject.optJSONArray("_SubListGetSettlement");

                            if (jsonArray == null || jsonArray.length() == 0) {
                                Util.Logcat.e("EMPTY | Null:::" + String.valueOf(jsonArray.length()));
                                alert.build(getString(R.string.nodata_available));
                                BtnCollect.setVisibility(View.INVISIBLE);
                                if (adapter != null) {
                                    listView.invalidateViews();
                                }
                            } else {
                                BtnCollect.setVisibility(View.VISIBLE);
                                final String Amount = jsonArray.getJSONObject(0).getString("SummaryAmount");
                                SettlementId = jsonArray.getJSONObject(0).getString("SettlementId");

                                Util.Logcat.e("SettlementId" + SettlementId);
                                Util.Logcat.e("amount" + Amount);
                                DataHashMap = new HashMap<>();
                                DataHashMap.put("FEName", ": " + jsonArray.getJSONObject(0).getString("FEName"));
                                DataHashMap.put("ShiftIn", ": " + jsonArray.getJSONObject(0).getString("ShiftIn"));
                                DataHashMap.put("ShiftOut", ": " + jsonArray.getJSONObject(0).getString("ShiftOut"));
                                DataHashMap.put("FOCCount", ": " + jsonArray.getJSONObject(0).getString("FOCCount"));
                                DataHashMap.put("TicketCount", ": " + jsonArray.getJSONObject(0).getString("TicketCount"));
                                DataHashMap.put("CollectionAmount", ": " + "Rs." + jsonArray.getJSONObject(0).getString("CollectionAmount"));
                                DataHashMap.put("FineAmount", ": " + "Rs." + jsonArray.getJSONObject(0).getString("FineAmount"));
                                DataHashMap.put("CashCollection", ": " + "Rs." + jsonArray.getJSONObject(0).getString("CashCollection"));
                                DataHashMap.put("CardCollection", ": " + "Rs." + jsonArray.getJSONObject(0).getString("CardCollection"));
                                DataHashMap.put("SummaryAmount", ": " + "Rs." + Amount);
                                //new fields
                                DataHashMap.put("TwFineAmount", ": " + "Rs." + jsonArray.getJSONObject(0).getString("TwFineAmount"));
                                DataHashMap.put("TwAmount", ": " + "Rs." + jsonArray.getJSONObject(0).getString("TwAmount"));
                                DataHashMap.put("FwFineAmount", ": " + "Rs." + jsonArray.getJSONObject(0).getString("FwFineAmount"));
                                DataHashMap.put("FwAmount", ": " + "Rs." + jsonArray.getJSONObject(0).getString("FwAmount"));
                                DataHashMap.put("TwCnt", ": " + jsonArray.getJSONObject(0).getString("TwCnt"));
                                DataHashMap.put("FwCnt", ": " + jsonArray.getJSONObject(0).getString("FwCnt"));
                                DataHashMap.put("TwFOCCnt", ": " + jsonArray.getJSONObject(0).getString("TwFOCCnt"));
                                DataHashMap.put("FwFOCCnt", ": " + jsonArray.getJSONObject(0).getString("FwFOCCnt"));
                                DataHashMap.put("TwGraceCnt", ": " + jsonArray.getJSONObject(0).getString("TwGraceCnt"));
                                ListCollection.add(DataHashMap);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter = new SettlementAdapter(getActivity(), ListCollection);
                                        listView.setAdapter(adapter);
                                /*for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONArray Amountarray = jsonObject.optJSONArray("_GetSettlelist");
                                    for (int j = 0; j < Amountarray.length(); j++) {
                                        JSONObject object = Amountarray.getJSONObject(j);

                                        if (object.getString("VehicleType").equalsIgnoreCase("Car")) {
                                            DataHashMap.put("Car", ": "+ "Rs." + object.getString("SettlementAmount").toString());
                                        } else if (object.getString("VehicleType").equalsIgnoreCase("Two Wheeler")) {
                                            DataHashMap.put("Two Wheeler", ": "+ "Rs." + object.getString("SettlementAmount").toString());
                                        } else if (object.getString("VehicleType").equalsIgnoreCase("Auto Rickshaw")) {
                                            DataHashMap.put("Auto Rickshaw", ": " + "Rs."+ object.getString("SettlementAmount").toString());
                                        } else if (object.getString("VehicleType").equalsIgnoreCase("Cycle Rickshaw")) {
                                            DataHashMap.put("Cycle Rickshaw", ": " + "Rs."+ object.getString("SettlementAmount").toString());
                                        } else if (object.getString("VehicleType").equalsIgnoreCase("Bus")) {
                                            DataHashMap.put("Bus", ": " + "Rs."+ object.getString("SettlementAmount").toString());
                                        } else if (object.getString("VehicleType").equalsIgnoreCase("HCV")) {
                                            DataHashMap.put("HCV", ": "+ "Rs." + object.getString("SettlementAmount").toString());
                                        } else if (object.getString("VehicleType").equalsIgnoreCase("LCV")) {
                                            DataHashMap.put("LCV", ":" + "Rs."+ object.getString("SettlementAmount").toString());
                                        } else if (object.getString("VehicleType").equalsIgnoreCase("Mini Bus")) {
                                            DataHashMap.put("Mini Bus", ": "+ "Rs." + object.getString("SettlementAmount").toString());
                                        }

                                    }
                                }*/
                                        //setListViewHeightBasedOnItems(listView);
                                    }
                                });

                            }

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

    private void loadattenderlist() {
        streetarray = new JSONArray();
        spinnerlist.clear();

        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            Util.Logcat.e("loadattenderlist:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), GET_ATTENDERLIST, new VolleyResponseListener() {
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

                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));
                        Util.Logcat.e("SETTLEMENT RESPONSE:::" + resobject.toString());
                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            JSONArray jsonArray = resobject.optJSONArray("_getAtd");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                JSONObject savedata = new JSONObject();
                                savedata.put("Name", jsonObject.getString("Name"));
                                savedata.put("ATDId", jsonObject.getString("ATDId"));
                                streetarray.put(savedata);
                                spinnerlist.add(jsonObject.getString("Name"));

                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                                            (getActivity(), R.layout.spinner_textview,
                                                    spinnerlist); //selected item will look like a spinner set from XML
                                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                            .simple_spinner_dropdown_item);
                                    SpinAttender.setAdapter(spinnerArrayAdapter);
                                }
                            });

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {

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


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SETTLEMENT");
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
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.start_stop);
        item.setVisible(false);


    }

    private boolean setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                float px = 500 * (listView.getResources().getDisplayMetrics().density);
                item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);
            // Get padding
            int totalPadding = listView.getPaddingTop() + listView.getPaddingBottom();

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight + totalPadding;
            listView.setLayoutParams(params);
            listView.requestLayout();
            //setDynamicHeight(listView);
            return true;

        } else {
            return false;
        }
    }
}