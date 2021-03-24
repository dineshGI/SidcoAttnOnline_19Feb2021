package com.sidcoparking.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sidcoparking.utils.Util;

import static com.sidcoparking.DataBase.DB_Constant.VehNo;

public class DatabaseHelper extends SQLiteOpenHelper {

//http://www.codebind.com/android-tutorials-and-examples/android-sqlite-tutorial-example/

    //TxnId~VehicleTypeId~VehicleNo~StreetId~ParkingHour~ParkingFee~IsGraceHour~GraceHour~GraceFee~IsFOC|
    public static final String DATABASE_NAME = "Transaction.db";
    public static final String TABLE_NAME = "transaction_table";

    public static final String TRANSACTION_ID = "TRANSID";
    public static final String VEHICLE_TYPE_ID = "VEHICLETYPE";
    public static final String VEHICLE_NO = "VEHICLENO";
    public static final String STREET_ID = "STREETID";
    public static final String PARKING_HOUR = "PARKINGHOUR";
    public static final String PARKING_FEE = "PARKINGFEE";
    public static final String IS_GRACE_HOUR = "ISGRACEHOUR";
    public static final String GRACE_HOUR = "GRACEHOUR";
    public static final String GRACE_FEE = "GRACEFEE";
    public static final String IS_FOC = "ISFOC";
    public static final String USER_ID = "USERID";
    public static final String ENTRY_TIME = "ENTRYTIME";
    public static final String EXPIRY_TIME = "EXPIRYTIME";
    public static final String SERVER_TRANSID = "SERVERTRANSID";
    public static final String BATCH_ID = "BATCHID";
    public static final String MOBILE_NO = "MOBILENO";
    public static final String STREET_NAME = "STREETNAME";
    public static final String DATE_TIME = "DATETIME";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (TRANSID TEXT ,VEHICLETYPE TEXT,VEHICLENO TEXT,STREETID TEXT,PARKINGHOUR TEXT,PARKINGFEE TEXT,ISGRACEHOUR TEXT,GRACEHOUR TEXT,GRACEFEE TEXT,ISFOC TEXT,USERID TEXT,ENTRYTIME TEXT,EXPIRYTIME TEXT,SERVERTRANSID TEXT,BATCHID TEXT,MOBILENO TEXT,STREETNAME TEXT,DATETIME)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String transactionid, String vehicletypeid, String vehicleno, String streetid, String parkinghour, String parkingfee, String isgracehour, String gracehour, String gracefee, String isfoc, String userid, String entrytime, String expirttime, String servertransid, String batchid, String mobileno, String streetname, String datetime) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRANSACTION_ID, Util.EncryptURL(transactionid));
        contentValues.put(VEHICLE_TYPE_ID, Util.EncryptURL(vehicletypeid));
        contentValues.put(VEHICLE_NO, Util.EncryptURL(vehicleno));
        contentValues.put(STREET_ID, Util.EncryptURL(streetid));
        contentValues.put(PARKING_HOUR, Util.EncryptURL(parkinghour));
        contentValues.put(PARKING_FEE, Util.EncryptURL(parkingfee));
        contentValues.put(IS_GRACE_HOUR, Util.EncryptURL(isgracehour));
        contentValues.put(GRACE_HOUR, Util.EncryptURL(gracehour));
        contentValues.put(GRACE_FEE, Util.EncryptURL(gracefee));
        contentValues.put(IS_FOC, Util.EncryptURL(isfoc));
        contentValues.put(USER_ID, Util.EncryptURL(userid));
        contentValues.put(ENTRY_TIME, Util.EncryptURL(entrytime));
        contentValues.put(EXPIRY_TIME, Util.EncryptURL(expirttime));
        contentValues.put(SERVER_TRANSID, Util.EncryptURL(servertransid));
        contentValues.put(BATCH_ID, Util.EncryptURL(batchid));
        contentValues.put(MOBILE_NO, Util.EncryptURL(mobileno));
        contentValues.put(STREET_NAME, Util.EncryptURL(streetname));
        contentValues.put(DATE_TIME, Util.EncryptURL(datetime));

        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    //update exit time by vehicle no
    public boolean updateExittime(String transactionid, String vehicletypeid, String vehicleno, String streetid, String parkinghour, String parkingfee, String isgracehour, String gracehour, String gracefee, String isfoc, String userid, String entrytime, String expirttime, String servertransid, String batchid, String mobileno, String streetname, String datetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRANSACTION_ID, transactionid);
        contentValues.put(VEHICLE_TYPE_ID, vehicletypeid);
        contentValues.put(VEHICLE_NO, vehicleno);
        contentValues.put(STREET_ID, streetid);
        contentValues.put(PARKING_HOUR, parkinghour);
        contentValues.put(PARKING_FEE, parkingfee);
        contentValues.put(IS_GRACE_HOUR, isgracehour);
        contentValues.put(GRACE_HOUR, gracehour);
        contentValues.put(GRACE_FEE, gracefee);
        contentValues.put(IS_FOC, isfoc);
        contentValues.put(USER_ID, userid);
        contentValues.put(ENTRY_TIME, entrytime);
        contentValues.put(EXPIRY_TIME, expirttime);
        contentValues.put(SERVER_TRANSID, servertransid);
        contentValues.put(BATCH_ID, batchid);
        contentValues.put(MOBILE_NO, mobileno);
        contentValues.put(STREET_NAME, streetname);
        contentValues.put(DATE_TIME, Util.EncryptURL(datetime));

        db.update(TABLE_NAME, contentValues, "TRANSID = ?", new String[]{transactionid});
        // db.update(TABLE_NAME,null, contentValues);
        return true;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "TRANSID = ?", new String[]{id});
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public Cursor getbyvehno(String nos) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "select * from " + TABLE_NAME + " where VEHICLENO"
                + "=?";

        Util.Logcat.e(selectQuery);

        Cursor res = db.rawQuery(selectQuery, new String[]{nos});
        if (res.getCount() > 0) {
            res.moveToFirst();
            Util.Logcat.e("sdsa:::" + Util.DECODE64(res.getString(VehNo)));
        }
        Util.Logcat.e("res:::" + res.getCount());
        return res;
    }

}