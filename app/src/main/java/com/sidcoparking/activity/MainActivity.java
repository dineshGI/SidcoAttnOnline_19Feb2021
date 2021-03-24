package com.sidcoparking.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sidcoparking.DataBase.DatabaseHelper;
import com.sidcoparking.Http.CallApi;
import com.sidcoparking.R;
import com.sidcoparking.interfaces.VolleyResponseListener;
import com.sidcoparking.module.ChangePassword;
import com.sidcoparking.module.CheckVehicle;
import com.sidcoparking.module.CustomerBooking;
import com.sidcoparking.module.Dashboard;
import com.sidcoparking.module.Fine;
import com.sidcoparking.module.OnlineOffline;
import com.sidcoparking.module.ParkingSidco;
import com.sidcoparking.module.Report;
import com.sidcoparking.module.ReportExpiry;
import com.sidcoparking.module.Settlement;
import com.sidcoparking.module.Transaction;
import com.sidcoparking.service.TransactionUpdate;
import com.sidcoparking.utils.CommonAlertDialog;
import com.sidcoparking.utils.SupervisorLoginCheck;
import com.sidcoparking.utils.Util;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.sidcoparking.DataBase.DB_Constant.Datetime;
import static com.sidcoparking.DataBase.DB_Constant.Entrytime;
import static com.sidcoparking.DataBase.DB_Constant.Expirytime;
import static com.sidcoparking.DataBase.DB_Constant.GraceFee;
import static com.sidcoparking.DataBase.DB_Constant.GraceHour;
import static com.sidcoparking.DataBase.DB_Constant.IsGraceHour;
import static com.sidcoparking.DataBase.DB_Constant.Isfoc;
import static com.sidcoparking.DataBase.DB_Constant.MobileNo;
import static com.sidcoparking.DataBase.DB_Constant.ParkingFee;
import static com.sidcoparking.DataBase.DB_Constant.ParkingHour;
import static com.sidcoparking.DataBase.DB_Constant.PaymentMode;
import static com.sidcoparking.DataBase.DB_Constant.ServerTransid;
import static com.sidcoparking.DataBase.DB_Constant.Streetid;
import static com.sidcoparking.DataBase.DB_Constant.Streetname;
import static com.sidcoparking.DataBase.DB_Constant.TransId;
import static com.sidcoparking.DataBase.DB_Constant.Userid;
import static com.sidcoparking.DataBase.DB_Constant.VehNo;
import static com.sidcoparking.DataBase.DB_Constant.VehTypeId;
import static com.sidcoparking.utils.Util.GET_SETTLEMENT;
import static com.sidcoparking.utils.Util.LOGOUT;
import static com.sidcoparking.utils.Util.SUPERVISOR_LOGIN;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    NavigationView navigationView;
    private DrawerLayout drawer;
    public static int navItemIndex = 0;
    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String DASHBOARD = "dashboard";
    private static final String TAG_CUSTOMERBOOKING = "customerbooking";
    private static final String TAG_CHANGEPASSWORD = "changepassword";
    private static final String TAG_TRANSACTION = "transaction";
    public static String FINE = "fine";
    public static String SETTLEMNT = "settlement";
    public static String REPORT = "report";
    public static String EXPIRY_REPORT = "expiryreport";
    public static String CHECK_VEHICLE = "checkvehicle";

    public static String CURRENT_TAG = TAG_HOME;

    private String[] activityTitles;
    private View navHeader;
    private TextView txtName, txtloginid, txtbranch, txttime;
    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    private Menu menu;
    TextView name;
    // TextView tvSave;
    //Print data
    private String printVersion;
    private final int NOPAPER = 3;
    private final int LOWBATTERY = 4;
    private final int PRINTVERSION = 5;
    private final int PRINTBARCODE = 6;
    private final int PRINTQRCODE = 7;
    private final int PRINTPAPERWALK = 8;
    private final int PRINTCONTENT = 9;
    private final int CANCELPROMPT = 10;
    private final int PRINTERR = 11;
    private final int OVERHEAT = 12;
    private final int MAKER = 13;
    private final int PRINTPICTURE = 14;
    private final int NOBLACKBLOCK = 15;
    private String Result;
    private ProgressDialog progressDialog;
    ProgressDialog dialog;
    private Boolean nopaper = false;
    private boolean LowBattery = false;
    //UsbThermalPrinter mUsbThermalPrinter;
    UsbThermalPrinter mUsbThermalPrinter = new UsbThermalPrinter(this);
    MyHandler handler;
    public static String printContent;
    private static MainActivity instance;

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOPAPER:
                    noPaperDlg();
                    break;
                case LOWBATTERY:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle(R.string.operation_result);
                    alertDialog.setMessage(getString(R.string.LowBattery));
                    alertDialog.setPositiveButton(getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alertDialog.show();
                    break;
                case NOBLACKBLOCK:
                    Toast.makeText(MainActivity.this, R.string.maker_not_find, Toast.LENGTH_SHORT).show();
                    break;
                case PRINTVERSION:
                    dialog.dismiss();
                    if (msg.obj.equals("1")) {
                        //  textPrintVersion.setText(printVersion);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.operation_fail, Toast.LENGTH_LONG).show();
                    }
                    break;
                case PRINTBARCODE:
                    // new barcodePrintThread().start();
                    break;
                case PRINTQRCODE:
                    //  new qrcodePrintThread().start();
                    break;
                case PRINTPAPERWALK:
                    //  new paperWalkPrintThread().start();
                    break;
                case PRINTCONTENT:
                    new contentPrintThread().start();
                    break;
                case MAKER:
                    //   new MakerThread().start();
                    break;
                case PRINTPICTURE:
                    new printPicture().start();
                    break;
                case CANCELPROMPT:
                    if (progressDialog != null && !isFinishing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    break;
                case OVERHEAT:
                    AlertDialog.Builder overHeatDialog = new AlertDialog.Builder(MainActivity.this);
                    overHeatDialog.setTitle(R.string.operation_result);
                    overHeatDialog.setMessage(getString(R.string.overTemp));
                    overHeatDialog.setPositiveButton(getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    overHeatDialog.show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "Print Error!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Util.getData("RoleId", getApplicationContext()).equalsIgnoreCase("2")) {
            setContentView(R.layout.supervisor);
        } else {
            setContentView(R.layout.attender);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Util.printversn) {
            printinit();
        }
        //change menu
        mHandler = new Handler();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        instance = this;
        String workstatus = Util.getData("WorkStatus", getApplicationContext());
        if (!workstatus.equalsIgnoreCase("0")) {
            startService(new Intent(this, TransactionUpdate.class));
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navHeader = navigationView.getHeaderView(0);
        // name=navigationView.findViewById(R.id.username_header);

        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        //GetFineType();

        loadNavHeader();

        setUpNavigationView();

        if (savedInstanceState == null) {

            if (Util.getData("RoleId", getApplicationContext()).equalsIgnoreCase("2")) {
                navItemIndex = 2;
                CURRENT_TAG = SETTLEMNT;
            } else {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
            }

            loadHomeFragment();
        }
        // navigationView.setNavigationItemSelectedListener(this);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    private void printinit() {
        handler = new MyHandler();
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle(R.string.idcard_czz);
        dialog.setMessage(getText(R.string.watting));
        dialog.setCancelable(false);
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mUsbThermalPrinter.start(0);
                    mUsbThermalPrinter.reset();
                    printVersion = mUsbThermalPrinter.getVersion();
                    int st = mUsbThermalPrinter.checkStatus();
                    Log.e("yw", "status" + " " + st);
                } catch (TelpoException e) {
                    Log.e("yw", "status  111" + " " + e.toString());
                    e.printStackTrace();
                } finally {
                    if (printVersion != null) {
                        Message message = new Message();
                        message.what = PRINTVERSION;
                        message.obj = "1";
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = PRINTVERSION;
                        message.obj = "0";
                        handler.sendMessage(message);
                    }
                }
                mUsbThermalPrinter.stop();
            }
        }).start();

    }

    @Override
    public void onDestroy() {
        if (progressDialog != null && !this.isFinishing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        //unregisterReceiver(printReceive);
        // mUsbThermalPrinter.stop();
        super.onDestroy();
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.menu_home:

                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;


                    case R.id.fine:

                        navItemIndex = 1;
                        CURRENT_TAG = FINE;

                        break;
                    case R.id.settlement:

                        navItemIndex = 2;
                        CURRENT_TAG = SETTLEMNT;

                        break;

                    case R.id.menu_dashboard:

                        navItemIndex = 3;
                        CURRENT_TAG = DASHBOARD;

                        break;


                    case R.id.report:

                        navItemIndex = 4;
                        CURRENT_TAG = REPORT;

                        break;
                    case R.id.report_expiry:

                        navItemIndex = 5;
                        CURRENT_TAG = EXPIRY_REPORT;

                        break;
                    case R.id.verify_online:

                        navItemIndex = 6;
                        CURRENT_TAG = CHECK_VEHICLE;

                        break;
                    case R.id.cutomer_booking:
                        navItemIndex = 7;
                        CURRENT_TAG = TAG_CUSTOMERBOOKING;

                        break;

                    case R.id.transaction:
                        navItemIndex = 8;
                        CURRENT_TAG = TAG_TRANSACTION;

                        break;
                    case R.id.change_password:
                        navItemIndex = 9;
                        CURRENT_TAG = TAG_CHANGEPASSWORD;

                        break;

                    case R.id.Logout:
                        AlertDialog.Builder alertDialogBuilder;
                        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
                        alertDialogBuilder.setMessage(R.string.want_to_logout);
                        alertDialogBuilder.setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Logout();
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
                        drawer.closeDrawers();
                        return true;

                    default:
                        navItemIndex = 0;

                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }

                menuItem.setChecked(true);
                loadHomeFragment();
                return true;

            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
                txttime.setText(Util.getdatetime());
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void Logout() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("LogId", Util.getData("LogId", getApplicationContext()));
            obj.put("UserId", Util.getData("UserId", getApplicationContext()));
            Util.Logcat.e("LOGOUT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponseNopgrss(MainActivity.this, params.toString(), LOGOUT, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    if (message.contains("TimeoutError")) {
                        CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                        alert.build(getString(R.string.timeout_error));

                    } else {
                        CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                        alert.build(getString(R.string.server_error));

                    }
                    Util.Logcat.e("onError:" + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("LOGOUT RESPONSE:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            //_VechicleTraiff
                            Intent logout = new Intent(MainActivity.this, Login.class);
                            startActivity(logout);
                            finish();
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();

            return;
        }
        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();
        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        /*  if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            // show or hide the fab button
            //   toggleFab();
            return;
        }*/

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };
        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        //toggleFab();
        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {

        switch (navItemIndex) {

            case 0:
                OnlineOffline parkinghome = new OnlineOffline();
                return parkinghome;

            case 1:

                Fine fine = new Fine();
                return fine;
            case 2:

                Settlement settlement = new Settlement();
                return settlement;
            case 3:
                Dashboard dashboard = new Dashboard();
                return dashboard;
            case 4:
                Report report = new Report();
                return report;

            case 5:

                ReportExpiry expiryreport = new ReportExpiry();
                return expiryreport;

            case 6:
                CheckVehicle checkvehicle = new CheckVehicle();
                return checkvehicle;

            case 7:
                CustomerBooking customerbooking = new CustomerBooking();
                return customerbooking;
            case 8:
                Transaction transaction = new Transaction();
                return transaction;
            case 9:
                ChangePassword changepassword = new ChangePassword();
                return changepassword;

            default:
                return new ParkingSidco();

        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);

    }

    private void loadNavHeader() {
        // name, website
        txtName = navHeader.findViewById(R.id.username_header);
        txtloginid = navHeader.findViewById(R.id.loginid);
        txttime = navHeader.findViewById(R.id.showtime);
        //  Util.Logcat.e("navusername", Util.getData("LoginId", getApplicationContext()));
        if (!Util.getData("Name", getApplicationContext()).isEmpty()) {
            txtName.setText(Util.getData("Name", getApplicationContext()));
            txtloginid.setText(Util.getData("LoginId", getApplicationContext()));
            //  txtloginid.setText(Util.getData("RoleName", getApplicationContext()));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;

        MenuItem MenuItem = menu.findItem(R.id.start_stop);
        String workstatus = Util.getData("WorkStatus", getApplicationContext());
        //Util.Logcat.e("workstatus????" + workstatus);
        if (workstatus.equals("0")) {
            MenuItem.setTitle("SHIFT IN");
        } else {
            MenuItem.setTitle("SHIFT OUT");
        }
        return true;
    }

    public void CommonPrint() {

        if (Util.printversn) {

            if (LowBattery == true) {
                handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));

            } else {
                if (!nopaper) {
                    handler.sendMessage(handler.obtainMessage(PRINTPICTURE, 1, 0, null));

                    //new contentPrintThread().start();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
                }
            }
            mUsbThermalPrinter.stop();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.start_stop:
                MenuItem iastem = menu.findItem(R.id.start_stop);
                iastem.setEnabled(false);
                if (item.getTitle().equals("SHIFT OUT")) {
                    if (checkDB()) {
                        Util.Logcat.e("checkDB" + "true");
                        //  UpdateDB("SHIFT OUT", username.getText().toString(), password.getText().toString());
                        UpdateDB("SHIFT OUT");
                    } else {
                        Util.Logcat.e("checkDB" + "false");
                        ShowloginAlert("SHIFT OUT");
                    }

                } else if (item.getTitle().equals("SHIFT IN")) {
                    ShowloginAlert("SHIFT IN");
                }

                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ShowloginAlert(final String titl) {

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        dialogBuilder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_login, null);

        final EditText username = dialogView.findViewById(R.id.username);
        final EditText password = dialogView.findViewById(R.id.password);
        final Button login = dialogView.findViewById(R.id.btn_login);
        final Button cancel = dialogView.findViewById(R.id.btn_clear);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuItem iastem = menu.findItem(R.id.start_stop);
                iastem.setEnabled(true);
                dialogBuilder.dismiss();
                if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {

                    if (titl.equalsIgnoreCase("SHIFT OUT")) {

                        String statuslogin = SupervisorLoginCheck.login(MainActivity.this, username.getText().toString(), password.getText().toString());
                        try {
                            JSONObject objnew = new JSONObject(statuslogin);
                            if (objnew.getString("Status").equalsIgnoreCase("0")) {
                                GetSettlement("SHIFT OUT");

                            } else if (objnew.getString("Status").equalsIgnoreCase("1")) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
                                alertDialogBuilder.setMessage(objnew.getString("StatusDesc"));
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

                    } else if ("SHIFT IN".equalsIgnoreCase(titl)) {
                        String statuslogin = SupervisorLoginCheck.login(MainActivity.this, username.getText().toString(), password.getText().toString());
                        try {
                            JSONObject objnew = new JSONObject(statuslogin);
                            if (objnew.getString("Status").equalsIgnoreCase("0")) {
                                updatestatus("1", "0", "SHIFT IN", "");

                            } else if (objnew.getString("Status").equalsIgnoreCase("1")) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
                                alertDialogBuilder.setMessage(objnew.getString("StatusDesc"));
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
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
                    alertDialogBuilder.setMessage(getString(R.string.enter_details_login));
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


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
                MenuItem iastem = menu.findItem(R.id.start_stop);
                iastem.setEnabled(true);
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

    private void GetSettlement(final String title) {

        try {
            JSONObject obj = new JSONObject();
            obj.put("ATDId", Util.getData("UserId", getApplicationContext()));
            obj.put("UserId", Util.getData("UserId", getApplicationContext()));
            Util.Logcat.e("SETTLEMENT:::" + obj.toString());
            String data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(MainActivity.this, params.toString(), GET_SETTLEMENT, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    if (message.contains("TimeoutError")) {
                        CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                        alert.build(getString(R.string.timeout_error));

                    } else {
                        CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                        alert.build(getString(R.string.server_error));
                    }
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("SETTLEMENT:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        final StringBuffer data = new StringBuffer();
                        if (resobject.getString("Status").equalsIgnoreCase("0")) {

                            JSONArray jsonArray = resobject.optJSONArray("_SubListGetSettlement");
                            if (jsonArray.length() <= 0) {
                                final AlertDialog.Builder alertDialogBuilder;
                                alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
                                alertDialogBuilder.setTitle("Shift out");
                                alertDialogBuilder.setMessage("No Data for Settlement" + getString(R.string.r_u_sure));
                                alertDialogBuilder.setPositiveButton("YES",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {

                                                updatestatus("2", "0", title, data.toString());
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
                            }

                            final String Amount = jsonArray.getJSONObject(0).getString("SummaryAmount");
                            final String SettlementId = jsonArray.getJSONObject(0).getString("SettlementId");
                            final String FOCCount = jsonArray.getJSONObject(0).getString("FOCCount");
                            final String FEName = jsonArray.getJSONObject(0).getString("FEName");
                            final String ShiftIn = jsonArray.getJSONObject(0).getString("ShiftIn");
                            final String TicketCount = jsonArray.getJSONObject(0).getString("TicketCount");
                            final String CollectionAmount = jsonArray.getJSONObject(0).getString("CollectionAmount");
                            final String FineAmount = jsonArray.getJSONObject(0).getString("FineAmount");
                            final String CashCollection = jsonArray.getJSONObject(0).getString("CashCollection");
                            final String CardCollection = jsonArray.getJSONObject(0).getString("CardCollection");

                            data.append("Name          : " + FEName + "\n");
                            data.append("In Time :" + ShiftIn + "\n");
                            data.append("Out Time:" + Util.parkingtime(0) + "\n");
                            data.append("TicketCount   : " + TicketCount + "\n");
                            data.append("2W Amount     : " + jsonArray.getJSONObject(0).getString("TwAmount") + "\n");
                            data.append("2W Count      : " + jsonArray.getJSONObject(0).getString("TwCnt") + "\n");
                            data.append("4W Amount     : " + jsonArray.getJSONObject(0).getString("FwAmount") + "\n");
                            data.append("4W Count      : " + jsonArray.getJSONObject(0).getString("FwCnt") + "\n");
                            data.append("FineAmount    : " + "Rs." + FineAmount + "\n");
                            data.append("2W Fine Amount: " + jsonArray.getJSONObject(0).getString("TwFineAmount") + "\n");
                            data.append("4W Fine Amount: " + jsonArray.getJSONObject(0).getString("FwFineAmount") + "\n");
                            data.append("FOCCount      : " + FOCCount + "\n");
                            data.append("2W FOC Count  : " + jsonArray.getJSONObject(0).getString("TwFOCCnt") + "\n");
                            data.append("4W FOC Count  : " + jsonArray.getJSONObject(0).getString("FwFOCCnt") + "\n");
                            data.append("2W Grace Count: " + jsonArray.getJSONObject(0).getString("TwGraceCnt") + "\n");
                            data.append("Collection    : " + "Rs." + CollectionAmount + "\n");
                            data.append("CashCollection: " + "Rs." + CashCollection + "\n");
                            data.append("CardCollection: " + "Rs." + CardCollection + "\n");
                            data.append("------------------------" + "\n");
                            data.append("Total      : " + "Rs." + String.valueOf(Amount) + "\n\n");

                            final AlertDialog.Builder alertDialogBuilder;
                            alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
                            alertDialogBuilder.setTitle(getString(R.string.cash_settlement));
                            alertDialogBuilder.setMessage(data.toString() + getString(R.string.r_u_sure));
                            alertDialogBuilder.setPositiveButton("YES",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            updatestatus("2", String.valueOf(Amount), title, data.toString());
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

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.alertDialog);
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

    private void deleteDB() {
        DatabaseHelper TransactionDB = new DatabaseHelper(this);
        Cursor res = TransactionDB.getAllData();
        while (res.moveToNext()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date date1 = sdf.parse(Util.DECODE64(res.getString(Entrytime)));
                Date date2 = sdf.parse(Util.parkingtime(0));
                if (date1.compareTo(date2) < 0) {
                    if (!Util.DECODE64(res.getString(ServerTransid)).equalsIgnoreCase("0")) {
                        //TxnId~VehicleTypeId~VehicleNo~StreetId~ParkingHour~ParkingFee~IsGraceHour~GraceHour~GraceFee~IsFOC~UserId~EntryTime~ExpiryTime|
                        TransactionDB.deleteData(res.getString(TransId));
                        Util.Logcat.e("Deleted Data " + Util.DECODE64(res.getString(TransId)));

                    } else {
                        //  Util.Logcat.e("Condition ", "not matched ");
                    }
                    Util.Logcat.e("delete" + "delete");
                } else {
                    Util.Logcat.e("DONT" + "delete");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

    }

    private boolean checkDB() {
        boolean value = false;
        final DatabaseHelper TransactionDB = new DatabaseHelper(MainActivity.this);

        final ArrayList<String> ServerId = new ArrayList<String>();
        //calling
        //getdata from SQlite with serverId=0
        Cursor res = TransactionDB.getAllData();

        while (res.moveToNext()) {

            if (Util.DECODE64(res.getString(ServerTransid)).equalsIgnoreCase("0")) {
                value = true;
                break;
            } else {
                //  Util.Logcat.e("Condition ", "not matched ");
                value = false;

            }

        }
        return value;
    }

    private void UpdateDB(final String title) {

        final DatabaseHelper TransactionDB = new DatabaseHelper(MainActivity.this);

        final ArrayList<String> ServerId = new ArrayList<String>();
        //calling
        //getdata from SQlite with serverId=0

        Cursor res = TransactionDB.getAllData();
        StringBuffer buffer = new StringBuffer();

        while (res.moveToNext()) {

            if (Util.DECODE64(res.getString(ServerTransid)).equalsIgnoreCase("0")) {
                ServerId.add(Util.DECODE64(res.getString(TransId)));
                //TxnId~VehicleTypeId~VehicleNo~StreetId~ParkingHour~ParkingFee~IsGraceHour~GraceHour~GraceFee~IsFOC~UserId~EntryTime~ExpiryTime|
                buffer.append(Util.DECODE64(res.getString(TransId)) + "~" +
                        Util.DECODE64(res.getString(VehTypeId)) + "~" +
                        Util.DECODE64(res.getString(VehNo)) + "~" +
                        Util.DECODE64(res.getString(Streetid)) + "~" +
                        Util.DECODE64(res.getString(ParkingHour)) + "~" +
                        Util.DECODE64(res.getString(ParkingFee)) + "~" +
                        Util.DECODE64(res.getString(IsGraceHour)) + "~" +
                        Util.DECODE64(res.getString(GraceHour)) + "~" +
                        Util.DECODE64(res.getString(GraceFee)) + "~" +
                        Util.DECODE64(res.getString(Isfoc)) + "~" +
                        Util.DECODE64(res.getString(Userid)) + "~" +
                        Util.DECODE64(res.getString(Entrytime)) + "~" +
                        Util.DECODE64(res.getString(Expirytime)) + "~" +
                        Util.DECODE64(res.getString(PaymentMode)) + "~" +
                        Util.DECODE64(res.getString(MobileNo)) + "~" +
                        Util.DECODE64(res.getString(Datetime)) + "|");
                        /*Util.DECODE64(res.getString(SerialNo)) + "~" +
                        Util.DECODE64(res.getString(PrevTransId)) + "~" +
                        Util.DECODE64(res.getString(PrevEntryTime)) + "~" +
                        Util.DECODE64(res.getString(Day)) + "|");*/

            } else {
                //  Util.Logcat.e("Condition ", "not matched ");
            }
        }
        Util.Logcat.e("SERVER UPDATE buffer" + buffer.toString());
        Util.Logcat.e("TransId" + String.valueOf(ServerId));
        if (!buffer.toString().isEmpty()) {
            if (Util.isOnline(getApplicationContext())) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("TerminalId", Util.getData("terminalid", getApplicationContext()));
                    obj.put("TransactionInfo", buffer.toString());
                    Util.Logcat.e("BULK UPDATE REQUEST" + obj.toString());
                    //  Util.Logcat.e("INPUT:::", Util.EncryptURL(obj.toString()));
                    String data = Util.EncryptURL(obj.toString());

                    JSONObject params = new JSONObject();
                    params.put("Getrequestresponse", data);
                    CallApi.postResponseNopgrss(getApplicationContext(), params.toString(), Util.ADD_PARKING, new VolleyResponseListener() {
                        @Override
                        public void onError(String message) {
                            MenuItem iastem = menu.findItem(R.id.start_stop);
                            iastem.setEnabled(true);

                            if (message.contains("TimeoutError")) {
                                CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                                alert.build(getString(R.string.timeout_error));

                            } else {
                                CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                                alert.build(getString(R.string.server_error));

                            }
                            Util.Logcat.e("onError:" + message);
                            ServerId.clear();
                        }

                        @Override
                        public void onResponse(JSONObject response) {
                            Util.Logcat.e("onResponse" + response);
                            MenuItem iastem = menu.findItem(R.id.start_stop);
                            iastem.setEnabled(true);

                            try {
                                Util.Logcat.e("BULK UPDATE RESPONSE:::" + Util.Decrypt(response.getString("Postresponse")));
                                JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                                if (resobject.getString("Status").equalsIgnoreCase("0")) {
                                    Util.Logcat.e("BULK UPDATE" + resobject.getString("ServerGenNo"));
                                    Util.Logcat.e("BULK UPDATE" + "SUCCESS");
                                    ShowloginAlert("SHIFT OUT");
                                    Cursor res = TransactionDB.getAllData();
                                    if (res.getCount() == 0) {
                                        // show message
                                        Util.Logcat.e("Nothing:::" + "Found");
                                        return;
                                    }

                                    while (res.moveToNext()) {

                                        if (ServerId.contains(Util.Decrypt(res.getString(TransId)))) {
                                            boolean isUpdate = TransactionDB.updateExittime(res.getString(TransId), res.getString(VehTypeId), res.getString(VehNo), res.getString(Streetid), res.getString(ParkingHour),
                                                    res.getString(ParkingFee), res.getString(IsGraceHour), res.getString(GraceHour), res.getString(GraceFee), res.getString(Isfoc),
                                                    res.getString(Userid), res.getString(Entrytime), res.getString(Expirytime), Util.EncryptURL(resobject.getString("ServerGenNo")), res.getString(PaymentMode), res.getString(MobileNo), res.getString(Streetname), res.getString(Datetime));
                                            Util.Logcat.e("updated server " + Util.Decrypt(res.getString(TransId)) + ":::" + resobject.getString("ServerGenNo"));

                                            if (isUpdate == true)
                                                Toast.makeText(getApplicationContext(), "Data Update", Toast.LENGTH_LONG).show();
                                            else
                                                Toast.makeText(getApplicationContext(), "Data not Updated", Toast.LENGTH_LONG).show();
                                        } else {
                                            //   Util.Logcat.e("Nothing Matched:::", "with 0 ServerID");
                                        }
                                    }

                                    ServerId.clear();

                                } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                                    Util.Logcat.e("BULK UPDATE RESPONSE:::" + "Failed" + resobject.getString("Status"));
                                    CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                                    alert.build(resobject.getString("StatusDesc"));
                                    ServerId.clear();
                                }
                                //connect = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.app_name) + "\nPlease check your internet connection", Toast.LENGTH_SHORT).show();
            }

        } else {
            Util.Logcat.e("No Data " + "in DB");
        }

    }

    private void updatestatus(String shiftstatus, String TotalAmount, final String title, final String printdata) {

        if (Util.isOnline(getApplicationContext())) {

            try {
                JSONObject obj = new JSONObject();
                obj.put("SettlementId", Util.getData("WorkStatus", getApplicationContext()));
                obj.put("ShiftStatus", shiftstatus);
                obj.put("ShiftTime", Util.parkingtime(0));
                obj.put("TotalAmount", TotalAmount);
                obj.put("ATDId", Util.getData("UserId", getApplicationContext()));
                Util.Logcat.e("INPUT:::" + obj.toString());
                String data = Util.EncryptURL(obj.toString());
                JSONObject params = new JSONObject();
                params.put("Getrequestresponse", data);
                CallApi.postResponse(MainActivity.this, params.toString(), Util.WORK_STATUS, new VolleyResponseListener() {
                    @Override
                    public void onError(String message) {

                        if (message.contains("TimeoutError")) {
                            CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                            alert.build(getString(R.string.timeout_error));

                        } else {
                            CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
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

                            Util.saveData("WorkStatus", resobject.getString("SettlementId"), getApplicationContext());

                            if (resobject.getString("Status").equalsIgnoreCase("0")) {
                                Util.saveData("WorkStatus", resobject.getString("SettlementId"), getApplicationContext());
                                MenuItem myItem = menu.findItem(R.id.start_stop);
                                if (title.equalsIgnoreCase("SHIFT IN")) {
                                    myItem.setTitle("SHIFT OUT");
                                    startService(new Intent(getApplicationContext(), TransactionUpdate.class));
                                    Util.saveData("ShitIn", Util.parkingtime(0), getApplicationContext());
                                } else if (title.equalsIgnoreCase("SHIFT OUT")) {
                                    myItem.setTitle("SHIFT IN");
                                    stopService(new Intent(getApplicationContext(), TransactionUpdate.class));
                                    TransactionUpdate.mTimer.cancel();
                                }
                                CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
                                alert.build(resobject.getString("StatusDesc").toString());

                                if (!printdata.isEmpty()) {
                                    StringBuffer printvalues = new StringBuffer();
                                    printvalues.append("    CASH SETTLEMENT    ");
                                    printvalues.append(getString(R.string.line));
                                    printvalues.append(printdata + "\n");
                                    printContent = printvalues.toString();
                                    CommonPrint();
                                }
                                deleteDB();
                            } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                                CommonAlertDialog alert = new CommonAlertDialog(MainActivity.this);
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

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.app_name) + "\n" + getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder = new AlertDialog.Builder(this, R.style.alertDialog);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return true;
    }

    public static String login(final Context con, String username, String password) {

        try {
            Util.saveData("logincheck", "", con.getApplicationContext());
            Util.saveData("loginmsg", "", con.getApplicationContext());
            String data = "";
            JSONObject obj = new JSONObject();
            obj.put("LoginId", username);
            obj.put("Password", Util.EncryptURL(password));

            data = Util.EncryptURL(obj.toString());
            JSONObject params = new JSONObject();
            params.put("Getrequestresponse", data);
            CallApi.postResponse(con, params.toString(), SUPERVISOR_LOGIN, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                }

                @Override
                public void onResponse(JSONObject response) {
                    Util.Logcat.e("onResponse" + response);
                    try {
                        Util.Logcat.e("OUTPUT:::" + Util.Decrypt(response.getString("Postresponse")));
                        JSONObject resobject = new JSONObject(Util.Decrypt(response.getString("Postresponse")));

                        if (resobject.getString("Status").equalsIgnoreCase("0")) {
                            Util.Logcat.e("OUTPUT:::" + resobject.getString("Status"));
                            Util.Logcat.e("StatusDesc:::" + resobject.getString("StatusDesc"));
                            Util.saveData("logincheck", resobject.getString("Status"), con.getApplicationContext());
                            Util.saveData("loginmsg", resobject.getString("StatusDesc"), con.getApplicationContext());

                        } else if (resobject.getString("Status").equalsIgnoreCase("1")) {
                            Util.saveData("logincheck", resobject.getString("Status"), con.getApplicationContext());
                            Util.saveData("loginmsg", resobject.getString("StatusDesc"), con.getApplicationContext());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String status = Util.getData("logincheck", con.getApplicationContext());
        Util.Logcat.e("status return" + status);
        return status;
    }

    private class contentPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setLeftIndent(0);
                mUsbThermalPrinter.setLineSpace(0);
                mUsbThermalPrinter.setTextSize(25);
                mUsbThermalPrinter.setGray(1);
                mUsbThermalPrinter.addString(printContent);
                // Log.e("printContent:::", Util.getData("printContent", getApplicationContext()));
                mUsbThermalPrinter.printString();
                // mUsbThermalPrinter.walkPaper(10);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class printPicture extends Thread {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.start(0);
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setGray(1);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                Bitmap icon = Util.drawableToBitmap(getDrawable(R.drawable.sidco_logo));
                mUsbThermalPrinter.printLogo(icon, false);
                handler.sendMessage(handler.obtainMessage(PRINTCONTENT, 1, 0, null));

            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private void noPaperDlg() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle(getString(R.string.noPaper));
        dlg.setMessage(getString(R.string.noPaperNotice));
        dlg.setCancelable(false);
        dlg.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dlg.show();
    }
}
