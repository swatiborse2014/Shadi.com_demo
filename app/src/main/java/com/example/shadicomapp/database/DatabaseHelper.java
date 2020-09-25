package com.example.shadicomapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.shadicomapp.model.Result;
import com.example.shadicomapp.model.UserAddress;
import com.example.shadicomapp.model.UserDOB;
import com.example.shadicomapp.model.UserName;
import com.example.shadicomapp.model.UserPicture;
import com.example.shadicomapp.model.UserStreetAdd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

@SuppressLint("SdCardPath")
public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "";
    private static final String DB_NAME = "Database.sqlite";
    private SQLiteDatabase myDataBase;
    private final Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static DatabaseHelper mDBConnection;
    public static int version_val = 1;

    private static final String TBL_RESULT = "tbl_result";
    private static final String TBL_STATUS = "tbl_status";

    private static final String id = "id";
    private static final String title = "title";
    private static final String first = "first";
    private static final String last = "last";
    private static final String gender = "gender";
    private static final String street_no = "street_no";
    private static final String street_name = "street_name";
    private static final String city = "city";
    private static final String state = "state";
    private static final String country = "country";
    private static final String postcode = "postcode";
    private static final String email = "email";
    private static final String dob_date = "date";
    private static final String age = "age";
    private static final String phone = "phone";
    private static final String cell = "cell";
    private static final String pic_large = "pic_large";
    private static final String status = "status";

    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
        DB_PATH = "/data/data/"
                + context.getApplicationContext().getPackageName()
                + "/databases/";
    }

    public static synchronized DatabaseHelper getDBAdapterInstance(
            Context context) {
        if (mDBConnection == null) {
            mDBConnection = new DatabaseHelper(context, DB_NAME, null,
                    version_val);
        }
        return mDBConnection;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String TABLE_RESULTS = "CREATE TABLE IF NOT EXISTS " + TBL_RESULT + "("
                + id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + title + " TEXT ,"
                + first + " TEXT ,"
                + last + " TEXT ,"
                + gender + " TEXT ,"
                + street_no + " TEXT ,"
                + street_name + " TEXT ,"
                + city + " TEXT ,"
                + state + " TEXT ,"
                + country + " TEXT ,"
                + postcode + " TEXT ,"
                + email + " TEXT ,"
                + dob_date + " DATETIME NOT NULL ,"
                + age + " TEXT ,"
                + phone + " TEXT ,"
                + cell + " TEXT ,"
                + pic_large + " TEXT " + ")";
        db.execSQL(TABLE_RESULTS);

        String TABLE_STATUS = "CREATE TABLE IF NOT EXISTS " + TBL_STATUS + "("
                + id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + email + " TEXT ,"
                + status + " TEXT " + ")";
        db.execSQL(TABLE_STATUS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
        } else {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = mContext.getDatabasePath(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        Log.d("DB : ", DB_NAME + " DB PATH :" + DB_PATH + " ASSETS : " + mContext.getAssets());
        InputStream myInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    public void insertData(ArrayList<Result> dataArrayList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        for (int i = 0; i < dataArrayList.size(); i++) {

            cv.put(title, dataArrayList.get(i).getName().getTitle());
            cv.put(first, dataArrayList.get(i).getName().getFirst());
            cv.put(last, dataArrayList.get(i).getName().getLast());
            cv.put(gender, dataArrayList.get(i).getGender());
            cv.put(street_no, dataArrayList.get(i).getLocation().getStreet().getNumber());
            cv.put(street_name, dataArrayList.get(i).getLocation().getStreet().getName());
            cv.put(city, dataArrayList.get(i).getLocation().getCity());
            cv.put(state, dataArrayList.get(i).getLocation().getState());
            cv.put(country, dataArrayList.get(i).getLocation().getCountry());
            cv.put(postcode, dataArrayList.get(i).getLocation().getPostcode());
            cv.put(email, dataArrayList.get(i).getEmail());
            cv.put(dob_date, dataArrayList.get(i).getDob().getDate());
            cv.put(age, dataArrayList.get(i).getDob().getAge());
            cv.put(phone, dataArrayList.get(i).getPhone());
            cv.put(cell, dataArrayList.get(i).getCell());
            cv.put(pic_large, dataArrayList.get(i).getPicture().getLarge());

            db.insert(TBL_RESULT, null, cv);
        }
    }

    public void addStatus(String emailID, String statuss) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(email, emailID);
        cv.put(status, statuss);

        db.insert(TBL_STATUS, null, cv);
    }

    public String retrieveStatus(String email, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String emailId = "";
//        Cursor cursor = db.rawQuery(
//                "Select * from tbl_status where email='" + email + "'", null);

        Cursor cursor = db.rawQuery("Select count(*) from tbl_status where " +
                "email='" + email + "' AND " +
                "status='" + status + "'", null);

        if (cursor.moveToFirst()) {
            do {
                emailId = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return emailId;
    }

    public ArrayList<Result> getAllData() {
        ArrayList<Result> data = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TBL_RESULT + " ORDER BY " + id;

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Result result = new Result();
                    UserName userName = new UserName();
                    UserDOB userDOB = new UserDOB();
                    UserAddress address = new UserAddress();
                    UserStreetAdd streetAdd = new UserStreetAdd();
                    UserPicture picture = new UserPicture();

                    userName.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    userName.setFirst(cursor.getString(cursor.getColumnIndex("first")));
                    userName.setLast(cursor.getString(cursor.getColumnIndex("last")));
                    result.setName(userName);

                    result.setGender(cursor.getString(cursor.getColumnIndex("gender")));

                    streetAdd.setNumber(cursor.getString(cursor.getColumnIndex("street_no")));
                    streetAdd.setName(cursor.getString(cursor.getColumnIndex("street_name")));
                    address.setStreet(streetAdd);

                    address.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    address.setState(cursor.getString(cursor.getColumnIndex("state")));
                    address.setCountry(cursor.getString(cursor.getColumnIndex("country")));
                    address.setPostcode(cursor.getString(cursor.getColumnIndex("postcode")));
                    result.setLocation(address);

                    result.setEmail(cursor.getString(cursor.getColumnIndex("email")));

                    userDOB.setDate(cursor.getString(cursor.getColumnIndex("date")));
                    userDOB.setAge(cursor.getString(cursor.getColumnIndex("age")));
                    result.setDob(userDOB);

                    result.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
                    result.setCell(cursor.getString(cursor.getColumnIndex("cell")));

                    picture.setLarge(cursor.getString(cursor.getColumnIndex("pic_large")));
                    result.setPicture(picture);

                    data.add(result);

                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }
        return data;
    }
}