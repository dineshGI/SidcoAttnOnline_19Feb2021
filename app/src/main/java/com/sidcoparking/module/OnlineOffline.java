package com.sidcoparking.module;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.sidcoparking.R;
import com.sidcoparking.utils.Util;


public class OnlineOffline extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //https://api.mlab.com/api/1/databases/blindapp/collections/beacons?apiKey=EVDAtEeJwaIMAwOpjOOxdN2IiMmfLDJI


    private String mParam1;
    private String mParam2;
    private FragmentTabHost mTabHost;

    TextView offline, online;

    ProgressDialog progressDialog;

    private OnFragmentInteractionListener mListener;

    public OnlineOffline() {
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

    public static OnlineOffline newInstance(String param1, String param2) {
        OnlineOffline fragment = new OnlineOffline();
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

        View rootView = inflater.inflate(R.layout.online_offline, container, false);
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
                return false;
            }
        });

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        mTabHost = rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);


        mTabHost.addTab(mTabHost.newTabSpec("Normal").setIndicator("Normal"),
                ParkingOffline.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("Slot").setIndicator("Slot"),
                ParkingOnline.class, null);

        offline = mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        offline.setTextSize(14);
        offline.setAllCaps(false);

        online = mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        online.setTextSize(14);
        online.setAllCaps(false);

        // online.setTextColor(getResources().getColor(R.color.black));

        // two.setTextColor(getResources().getColor(R.color.black));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabChanged(String tabId) {
                Util.Logcat.e("HAI" + tabId);
                if (tabId.equalsIgnoreCase("Normal")) {
                    online.setTextColor(getResources().getColor(R.color.colorAccent));
                    offline.setTextColor(getResources().getColor(R.color.darkgray));
                } else {
                    offline.setTextColor(getResources().getColor(R.color.colorAccent));
                    online.setTextColor(getResources().getColor(R.color.darkgray));
                }
            }
        });


        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.getData("tab", getActivity().getApplicationContext()).equalsIgnoreCase("0")) {
            mTabHost.setCurrentTab(0);

            offline.setTextColor(getResources().getColor(R.color.colorAccent));
            online.setTextColor(getResources().getColor(R.color.darkgray));

        } else if (Util.getData("tab", getActivity().getApplicationContext()).equalsIgnoreCase("1")) {
            mTabHost.setCurrentTab(1);
            online.setTextColor(getResources().getColor(R.color.colorAccent));
            offline.setTextColor(getResources().getColor(R.color.darkgray));
        } else {
            mTabHost.setCurrentTab(0);
            offline.setTextColor(getResources().getColor(R.color.colorAccent));
            online.setTextColor(getResources().getColor(R.color.darkgray));
        }


        // online.setTextColor(getResources().getColor(R.color.black));

        // two.setTextColor(getResources().getColor(R.color.black));
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.start_stop);
        item.setVisible(true);
    }

}