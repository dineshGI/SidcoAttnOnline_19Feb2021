package com.sidcoparking.module;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.widget.Button;
import android.widget.EditText;

import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import static com.sidcoparking.DataBase.DB_Constant.MobileNo;
import static com.sidcoparking.DataBase.DB_Constant.PaymentMode;
import static com.sidcoparking.DataBase.DB_Constant.Entrytime;
import static com.sidcoparking.DataBase.DB_Constant.Expirytime;
import static com.sidcoparking.DataBase.DB_Constant.GraceFee;
import static com.sidcoparking.DataBase.DB_Constant.GraceHour;
import static com.sidcoparking.DataBase.DB_Constant.IsGraceHour;
import static com.sidcoparking.DataBase.DB_Constant.Isfoc;
import static com.sidcoparking.DataBase.DB_Constant.ParkingFee;
import static com.sidcoparking.DataBase.DB_Constant.ParkingHour;
import static com.sidcoparking.DataBase.DB_Constant.ServerTransid;
import static com.sidcoparking.DataBase.DB_Constant.Streetid;
import static com.sidcoparking.DataBase.DB_Constant.TransId;
import static com.sidcoparking.DataBase.DB_Constant.Userid;
import static com.sidcoparking.DataBase.DB_Constant.VehNo;
import static com.sidcoparking.DataBase.DB_Constant.VehTypeId;
import static com.sidcoparking.utils.Util.CHANGEPASSWORD;

public class ChangePassword extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //https://api.mlab.com/api/1/databases/blindapp/collections/beacons?apiKey=EVDAtEeJwaIMAwOpjOOxdN2IiMmfLDJI

    private String mParam1;
    private String mParam2;

    ProgressDialog progressDialog;
    EditText OldPassword, NewPassword, ConfirmPassword;
    Button BtnSubmit,BtnShowDB;
    CommonAlertDialog alert;
    DatabaseHelper TransactionDB;

    private OnFragmentInteractionListener mListener;

    public ChangePassword() {
        // Required empty public constructor
    }

    public static ChangePassword newInstance(String param1, String param2) {
        ChangePassword fragment = new ChangePassword();
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

        View rootView = inflater.inflate(R.layout.change_password, container, false);
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
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        TransactionDB = new DatabaseHelper(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        alert = new CommonAlertDialog(getActivity());
        OldPassword = rootView.findViewById(R.id.old_pass);
        NewPassword = rootView.findViewById(R.id.new_pass);
        ConfirmPassword = rootView.findViewById(R.id.confirm_pass);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AutofillManager autofillManager = getActivity().getSystemService(AutofillManager.class);
            autofillManager.disableAutofillServices();
            OldPassword.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            NewPassword.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            ConfirmPassword.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }
        BtnSubmit = rootView.findViewById(R.id.change_pass);
        BtnSubmit.setOnClickListener(this);

        BtnShowDB = rootView.findViewById(R.id.showdb);
        BtnShowDB.setOnClickListener(this);
        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("CHANGE PASSWORD");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_pass:
                //OldPassword, NewPassword, ConfirmPassword
                if (OldPassword.getEditableText().toString().isEmpty()) {
                    alert.build("Enter Old Password");
                } else if (NewPassword.getEditableText().toString().isEmpty()) {
                    alert.build("Enter New Password");
                } else if (ConfirmPassword.getEditableText().toString().isEmpty()) {
                    alert.build("Enter Confirm Password");
                } else if (!NewPassword.getEditableText().toString().equalsIgnoreCase(ConfirmPassword.getEditableText().toString())) {
                    alert.build("New Password & Confirm Password should be same");
                } else {
                    callApi();
                }
                break;

            case R.id.showdb:
                Cursor res = TransactionDB.getAllData();
                if (res.getCount() == 0) {
                    // show message
                    showMessage("Error", "Nothing found");
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                    buffer.append("TransId :" + Util.DECODE64(res.getString(TransId)) + "\n");
                    buffer.append("vehtype :" + Util.DECODE64(res.getString(VehTypeId)) + "\n");
                    buffer.append("vehno :" + Util.DECODE64(res.getString(VehNo)) + "\n");
                    buffer.append("Streetid :" + Util.DECODE64(res.getString(Streetid)) + "\n");
                    buffer.append("ParkingHour :" + Util.DECODE64(res.getString(ParkingHour)) + "\n");
                    buffer.append("ParkingFee :" + Util.DECODE64(res.getString(ParkingFee)) + "\n");
                    buffer.append("IsGraceHour :" + Util.DECODE64(res.getString(IsGraceHour)) + "\n");
                    buffer.append("GraceHour :" + Util.DECODE64(res.getString(GraceHour)) + "\n");
                    buffer.append("GraceFee :" + Util.DECODE64(res.getString(GraceFee)) + "\n");
                    buffer.append("Isfoc :" + Util.DECODE64(res.getString(Isfoc)) + "\n");
                    buffer.append("Userid :" + Util.DECODE64(res.getString(Userid)) + "\n");
                    buffer.append("Entrytime :" + Util.DECODE64(res.getString(Entrytime)) + "\n");
                    buffer.append("Expirytime :" + Util.DECODE64(res.getString(Expirytime)) + "\n");
                    buffer.append("ServerTransid :" + Util.DECODE64(res.getString(ServerTransid)) + "\n");
                    buffer.append("PaymentMode :" + Util.DECODE64(res.getString(PaymentMode)) + "\n");
                    buffer.append("MobileNo :" + Util.DECODE64(res.getString(MobileNo)) + "\n\n");

                }
                // Show all data
                showMessage("Data", buffer.toString());
                break;
            default:
                break;
        }
    }

    private void callApi() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("LoginId", Util.getData("LoginId", getActivity().getApplicationContext()));
            obj.put("OldPassword", Util.EncryptURL(OldPassword.getEditableText().toString()));
            obj.put("NewPassword", Util.EncryptURL(NewPassword.getEditableText().toString()));
            obj.put("UserId", Util.getData("UserId", getActivity().getApplicationContext()));
            Util.Logcat.e("INPUT:::"+ obj.toString());
            String data = Util.EncryptURL(obj.toString());

            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponseNopgrss(getActivity(), params.toString(), CHANGEPASSWORD, new VolleyResponseListener() {
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
                         Util.Logcat.e("OUTPUT:::"+ Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            alert.build(resobject.getString("StatusDesc"));
                            OldPassword.setText("");
                            NewPassword.setText("");
                            ConfirmPassword.setText("");

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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.start_stop);
        item.setVisible(false);
    }

    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

}