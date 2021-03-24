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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.adapter.NotificationAdapter;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sidcoparking.utils.Util.GET_ATTENDERLIST;
import static com.sidcoparking.utils.Util.GET_DASHBOARD;

public class Dashboard extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    NotificationAdapter adapter;
    LinearLayout HideLayout;
    ListView listView;
    List<String> spinnerlist;
    JSONArray streetarray;
    CommonAlertDialog alert;
    Spinner SpinAttender;
    String USERID;
    Button BtnSubmit;
    TextView count;
    private OnFragmentInteractionListener mListener;

    TextView TwoWheelerCount, FourWheelerCount, TwoWheelerAmount, FourWheelerAmount, TwoWheelerFOCCount, FourWheelerFOCCount;
    TextView TwoWheelerGraceCount, FourWheelerGraceCount, TicketCount, TotalAmount, CashCollection, CardCollection;


    public Dashboard() {
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
    public static Dashboard newInstance(String param1, String param2) {
        Dashboard fragment = new Dashboard();
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

        View rootView = inflater.inflate(R.layout.dashboard, container, false);
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

        count = rootView.findViewById(R.id.count);
        TwoWheelerCount = rootView.findViewById(R.id.TwoWheelerCount);
        FourWheelerCount = rootView.findViewById(R.id.FourWheelerCount);
        TwoWheelerAmount = rootView.findViewById(R.id.TwoWheelerAmount);
        FourWheelerAmount = rootView.findViewById(R.id.FourWheelerAmount);
        TwoWheelerFOCCount = rootView.findViewById(R.id.TwoWheelerFOCCount);
        FourWheelerFOCCount = rootView.findViewById(R.id.FourWheelerFOCCount);
        TwoWheelerGraceCount = rootView.findViewById(R.id.TwoWheelerGraceCount);
        FourWheelerGraceCount = rootView.findViewById(R.id.FourWheelerGraceCount);
        TicketCount = rootView.findViewById(R.id.TicketCount);
        TotalAmount = rootView.findViewById(R.id.TotalAmount);
        CashCollection = rootView.findViewById(R.id.CashCollection);
        CardCollection = rootView.findViewById(R.id.CardCollection);


        listView = rootView.findViewById(R.id.listview);
        alert = new CommonAlertDialog(getActivity());
        SpinAttender = rootView.findViewById(R.id.spin_attender);
        HideLayout = rootView.findViewById(R.id.hidespinner);

        BtnSubmit = rootView.findViewById(R.id.submit);
        BtnSubmit.setOnClickListener(this);
        spinnerlist = new ArrayList<>();
        if (!Util.getData("RoleId", getActivity()).equalsIgnoreCase("2")) {
            HideLayout.setVisibility(View.GONE);
            USERID = Util.getData("UserId", getActivity().getApplicationContext());
            GetDashboard(USERID);
        } else {
            loadattenderlist();
        }

        SpinAttender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                String name = SpinAttender.getSelectedItem().toString();

                try {
                    for (int i = 0; i < streetarray.length(); i++) {
                        JSONObject jsonobject = streetarray.getJSONObject(i);
                        if (name.equalsIgnoreCase(jsonobject.getString("Name"))) {
                            USERID = jsonobject.getString("ATDId");
                            Util.Logcat.e("USERID" + USERID);
                            GetDashboard(USERID);
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

        try {
            JSONObject obj = new JSONObject();
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
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
                                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                                            (getActivity(), R.layout.spinner_textview,
                                                    spinnerlist); //selected item will look like a spinner set from XML
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

    private void GetDashboard(String userid) {

        try {
            JSONObject obj = new JSONObject();
            obj.put("TerminalId", Util.getData("terminalid", getActivity().getApplicationContext()));
            obj.put("ATDId", userid);
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));

           /* obj.put("TerminalId", "1300013");
            obj.put("ATDId", "15");
            obj.put("UserId", "15");*/

            Util.Logcat.e("DASHBOARD:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());

            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(getActivity(), params.toString(), GET_DASHBOARD, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("DASHBOARD:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));
                        Util.Logcat.e("DASHBOARD" + resobject);

                        TwoWheelerCount.setText(resobject.getString("TwoWheelerCount"));
                        FourWheelerCount.setText(resobject.getString("FourWheelerCount"));
                        TwoWheelerAmount.setText(getString(R.string.currency) + resobject.getString("TwoWheelerAmount"));
                        FourWheelerAmount.setText(getString(R.string.currency) + resobject.getString("FourWheelerAmount"));
                        TwoWheelerFOCCount.setText(resobject.getString("TwoWheelerFOCCount"));
                        FourWheelerFOCCount.setText(resobject.getString("FourWheelerFOCCount"));
                        TwoWheelerGraceCount.setText(resobject.getString("TwoWheelerGraceCount"));
                        FourWheelerGraceCount.setText(resobject.getString("FourWheelerGraceCount"));
                        TicketCount.setText(resobject.getString("TicketCount"));
                        TotalAmount.setText(getString(R.string.currency) + resobject.getString("TotalAmount"));
                        CashCollection.setText(getString(R.string.currency) + resobject.getString("CashCollection"));
                        CardCollection.setText(getString(R.string.currency) + resobject.getString("CardCollection"));

                        JSONArray jsonArray = resobject.optJSONArray("_lstDataset2");
                        final List<Map<String, String>> ListCollection = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            HashMap<String, String> DataHashMap = new HashMap<>();
                            DataHashMap.put("NewVehiclesArrived", object.getString("NewVehiclesArrived"));
                            ListCollection.add(DataHashMap);
                        }
                        if (ListCollection.size() > 0) {
                            count.setText("" + ListCollection.size());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter = new NotificationAdapter(getActivity(), ListCollection);
                                    listView.setAdapter(adapter);
                                    setListViewHeightBasedOnItems(listView);
                                }
                            });
                        } else {
                            count.setText("0");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Dashboard");
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
                    GetDashboard(USERID);
                } else {
                    GetDashboard(Util.getData("UserId", getActivity().getApplicationContext()));
                }

                break;


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