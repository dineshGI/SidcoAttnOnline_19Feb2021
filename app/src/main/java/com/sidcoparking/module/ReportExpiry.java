package com.sidcoparking.module;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.adapter.ExpiryReportAdapter;
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

import static com.sidcoparking.utils.Util.EXPIRY_VEHICLE;
import static com.sidcoparking.utils.Util.GET_ATTENDERLIST;


public class ReportExpiry extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //https://api.mlab.com/api/1/databases/blindapp/collections/beacons?apiKey=EVDAtEeJwaIMAwOpjOOxdN2IiMmfLDJI

    private String mParam1;
    private String mParam2;

    private HashMap<String, String> DataHashMap;
    private List<Map<String, String>> ListCollection;
    List<Map<String, String>> searchResults;
    ExpiryReportAdapter adapter;
    LinearLayout FromCalender, HideLayout;
    TextView FromDate;
    ListView listView;
    EditText EdSearch;
    List<String> spinnerlist;
    JSONArray streetarray;
    CommonAlertDialog alert;
    Spinner SpinAttender;
    String USERID;
    Button BtnSubmit;
    private OnFragmentInteractionListener mListener;

    public ReportExpiry() {
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
    public static ReportExpiry newInstance(String param1, String param2) {
        ReportExpiry fragment = new ReportExpiry();
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

        View rootView = inflater.inflate(R.layout.expiry_report, container, false);
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
        SpinAttender = rootView.findViewById(R.id.spin_attender);
        HideLayout = rootView.findViewById(R.id.hidespinner);

        listView = rootView.findViewById(R.id.listview);
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
        FromDate = rootView.findViewById(R.id.from_date);
        FromDate.setText(Util.getdateonly());
        FromCalender = rootView.findViewById(R.id.from_calendar);
        BtnSubmit = rootView.findViewById(R.id.submit);
        BtnSubmit.setOnClickListener(this);
        FromCalender.setOnClickListener(this);
        ListCollection = new ArrayList<Map<String, String>>();
        spinnerlist = new ArrayList<>();
        if (!Util.getData("RoleId", getActivity()).equalsIgnoreCase("2")) {
            HideLayout.setVisibility(View.GONE);
        } else {
            loadattenderlist();
        }

        SpinAttender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                //Toast.makeText(spinner.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
                String name = SpinAttender.getSelectedItem().toString();
                 Util.Logcat.e("finetype"+ name);

                try {
                    for (int i = 0; i < streetarray.length(); i++) {
                        JSONObject jsonobject = streetarray.getJSONObject(i);
                        if (name.equalsIgnoreCase(jsonobject.getString("Name"))) {
                            USERID = jsonobject.getString("ATDId");
                             Util.Logcat.e("USERID"+ USERID);
                            //GetMBSettlementDetails(jsonobject.getString("ATDId"), FromDate.getText().toString());
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

    private void loadattenderlist() {

        streetarray = new JSONArray();
        spinnerlist.clear();
        String data = "";
        JSONObject obj = new JSONObject();
        try {
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
             Util.Logcat.e("loadattenderlist:::"+ obj.toString());
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

    private void GetMBSettlementDetails(String userid, String date) {
        EdSearch.setText("");
        ListCollection.clear();

        try {
            JSONObject obj = new JSONObject();
            obj.put("ATDId", userid);
            obj.put("Datetime", Util.parkingtime(0));
            Util.Logcat.e("INPUT:::"+ obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), EXPIRY_VEHICLE, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:"+ message);
                    EdSearch.setVisibility(View.GONE);
                }

                @Override
                public void onResponse(JSONObject response) {
                     Util.Logcat.e("onResponse"+ response);
                    try {
                         Util.Logcat.e("REPORT:::"+ Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        JSONArray jsonArray = resobject.optJSONArray("_ExpvehicleDt");
                        if (jsonArray == null || jsonArray.length() == 0) {
                            EdSearch.setVisibility(View.GONE);
                             Util.Logcat.e("EMPTY | Null:::"+ String.valueOf(jsonArray.length()));
                            alert.build(getString(R.string.nodata_available));
                            if (adapter != null) {
                                listView.invalidateViews();
                            }
                        } else {
                            EdSearch.setVisibility(View.VISIBLE);
                             Util.Logcat.e("NOT Null:::"+ jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                DataHashMap = new HashMap<>();
                                DataHashMap.put("VehicleType", object.getString("VehicleType"));
                                DataHashMap.put("VehicleNo", object.getString("VehicleNo"));
                                DataHashMap.put("ExpiryTime", object.getString("ExpiryTime"));
                                ListCollection.add(DataHashMap);
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchResults = ListCollection;
                                    adapter = new ExpiryReportAdapter(getActivity(), ListCollection);
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("EXPIRY REPORT");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:

                if (HideLayout.getVisibility() == View.VISIBLE) {
                    GetMBSettlementDetails(USERID, FromDate.getText().toString());
                } else {
                    GetMBSettlementDetails(Util.getData("UserId", getActivity().getApplicationContext()), FromDate.getText().toString());
                }

                break;
           /* case R.id.from_calendar:

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
                break;*/

            default:
                break;
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