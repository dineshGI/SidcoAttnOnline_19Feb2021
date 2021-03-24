package com.sidcoparking.module;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.adapter.CustomerBookingAdapter;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sidcoparking.utils.Util.GET_ATTD_STREET_DETAILS;
import static com.sidcoparking.utils.Util.GET_ATTENDERLIST;
import static com.sidcoparking.utils.Util.GET_CUSTOMER_VEHICLE_DETAILS;


public class CustomerBooking extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private HashMap<String, String> DataHashMap;
    private List<Map<String, String>> ListCollection;
    CustomerBookingAdapter adapter;
    LinearLayout FromCalender, HideLayout;
    TextView FromDate;
    ListView listView;
    List<String> attenderspinnerlist, streetspinnerlist;
    JSONArray attenderarray, streetarray;
    CommonAlertDialog alert;
    Spinner SpinAttender, SpinStreet;
    String USERID, STREETID;
    Button BtnSubmit;
    private OnFragmentInteractionListener mListener;

    public CustomerBooking() {
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
    public static CustomerBooking newInstance(String param1, String param2) {
        CustomerBooking fragment = new CustomerBooking();
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

        View rootView = inflater.inflate(R.layout.customer_booking, container, false);
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
        SpinAttender = rootView.findViewById(R.id.spin_attender);
        SpinStreet = rootView.findViewById(R.id.spin_street);
        HideLayout = rootView.findViewById(R.id.hidespinner);

        listView = rootView.findViewById(R.id.listview);
        FromDate = rootView.findViewById(R.id.from_date);
        FromDate.setText(Util.getdateonly());
        FromCalender = rootView.findViewById(R.id.from_calendar);
        BtnSubmit = rootView.findViewById(R.id.submit);
        BtnSubmit.setOnClickListener(this);
        FromCalender.setOnClickListener(this);
        ListCollection = new ArrayList<>();
        attenderspinnerlist = new ArrayList<>();
        streetspinnerlist = new ArrayList<>();
        if (!Util.getData("RoleId", getActivity()).equalsIgnoreCase("2")) {
            HideLayout.setVisibility(View.GONE);
            USERID=Util.getData("UserId", getActivity().getApplicationContext());
            LoadStreetSpinner(USERID);
        } else {
            loadattenderlist();
        }

        SpinAttender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                String name = SpinAttender.getSelectedItem().toString();

                try {
                    for (int i = 0; i < attenderarray.length(); i++) {
                        JSONObject jsonobject = attenderarray.getJSONObject(i);
                        if (name.equalsIgnoreCase(jsonobject.getString("Name"))) {
                            USERID = jsonobject.getString("ATDId");
                             Util.Logcat.e("USERID"+ USERID);
                            LoadStreetSpinner(USERID);
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


        SpinStreet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                String name = SpinStreet.getSelectedItem().toString();

                try {
                    for (int i = 0; i < streetarray.length(); i++) {
                        JSONObject jsonobject = streetarray.getJSONObject(i);
                        if (name.equalsIgnoreCase(jsonobject.getString("StreetName"))) {
                            STREETID = jsonobject.getString("StreetId");
                             Util.Logcat.e("STREETID"+ STREETID);
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

    private void LoadStreetSpinner(String Attenderid) {
        streetarray = new JSONArray();
        streetspinnerlist.clear();
        JSONObject obj = new JSONObject();

        try {
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            obj.put("ATDId", Attenderid);
            Util.Logcat.e("INPUT:::"+ obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), GET_ATTD_STREET_DETAILS, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:"+ message);
                }

                @Override
                public void onResponse(JSONObject response) {
                     Util.Logcat.e("onResponse"+ response);
                    try {
                         Util.Logcat.e("PAYMENT:::"+ Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {

                            try {
                                JSONArray jsonArray = resobject.getJSONArray("_GetAttenderStreetList");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject imageobject = jsonArray.getJSONObject(i);
                                    Util.Logcat.e("StreetName"+ imageobject.getString("StreetName"));
                                    JSONObject savedata = new JSONObject();
                                    savedata.put("StreetId", imageobject.getString("StreetId"));
                                    savedata.put("StreetName", imageobject.getString("StreetName"));
                                    streetarray.put(savedata);
                                    streetspinnerlist.add(imageobject.getString("StreetName"));
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                                                (getActivity(), R.layout.spinner_textview,
                                                        streetspinnerlist); //selected item will look like a spinner set from XML
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

    private void loadattenderlist() {
        attenderarray = new JSONArray();
        attenderspinnerlist.clear();

        String data = "";
        JSONObject obj = new JSONObject();
        try {
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            data = Util.EncryptURL(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
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
                    Util.Logcat.e("onError:"+ message);
                }

                @Override
                public void onResponse(JSONObject response) {
                     Util.Logcat.e("onResponse"+ response);
                    try {

                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));
                         Util.Logcat.e("SETTLEMENT RESPONSE:::"+ resobject.toString());
                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            JSONArray jsonArray = resobject.optJSONArray("_getAtd");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                JSONObject savedata = new JSONObject();
                                savedata.put("Name", jsonObject.getString("Name"));
                                savedata.put("ATDId", jsonObject.getString("ATDId"));
                                attenderarray.put(savedata);
                                attenderspinnerlist.add(jsonObject.getString("Name"));
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                                            (getActivity(), R.layout.spinner_textview,
                                                    attenderspinnerlist); //selected item will look like a spinner set from XML
                                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                            .simple_spinner_dropdown_item);
                                    SpinAttender.setAdapter(spinnerArrayAdapter);
                                }
                            });

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


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("CUSTOMER BOOKING");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.submit:

                if (HideLayout.getVisibility() == View.VISIBLE) {
                    GetVehicleDetails(USERID);
                } else {
                    GetVehicleDetails(Util.getData("UserId", getActivity().getApplicationContext()));
                }
                break;

            case R.id.from_calendar:

                final Calendar c = Calendar.getInstance();
                Integer mYear = c.get(Calendar.YEAR);
                Integer mMonth = c.get(Calendar.MONTH);
                Integer mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.DatePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String _data = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        FromDate.setText(_data);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();

                break;

            default:
                break;
        }
    }

    private void GetVehicleDetails(String attenderid) {
        ListCollection.clear();
        String data = "";
        JSONObject obj = new JSONObject();
        try {
            obj.put("ATDId", attenderid);
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            obj.put("StreetId", STREETID);
            obj.put("FromDate", FromDate.getText().toString());
            obj.put("ToDate", FromDate.getText().toString());
            Util.Logcat.e("INPUT:::"+ obj.toString());
            data = Util.EncryptURL(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), GET_CUSTOMER_VEHICLE_DETAILS, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:"+ message);
                }

                @Override
                public void onResponse(JSONObject response) {
                     Util.Logcat.e("onResponse"+ response);
                    try {
                         Util.Logcat.e("REPORT:::"+ Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));
                        JSONArray jsonArray = resobject.optJSONArray("_GetCustomerVehicleDetails");
                        if (jsonArray == null || jsonArray.length() == 0) {
                             Util.Logcat.e("EMPTY | Null:::"+ String.valueOf(jsonArray.length()));
                            alert.build(getString(R.string.nodata_available));
                            if (adapter != null) {
                                listView.invalidateViews();
                            }
                        } else {
                             Util.Logcat.e("NOT Null:::"+ jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                DataHashMap = new HashMap<>();
                                DataHashMap.put("VehicleType", object.getString("VehicleType"));
                                DataHashMap.put("VehicleNo", object.getString("VehicleNo"));
                                DataHashMap.put("StreetName", object.getString("StreetName"));
                                DataHashMap.put("Amount", String.valueOf(object.getString("Amount")));
                                DataHashMap.put("EntryTime", object.getString("EntryTime"));
                                DataHashMap.put("ExpiryTime", object.getString("ExpiryTime"));
                                ListCollection.add(DataHashMap);
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter = new CustomerBookingAdapter(getActivity(), ListCollection);
                                    listView.setAdapter(adapter);
                                    setListViewHeightBasedOnItems(listView);
                                }
                            });

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