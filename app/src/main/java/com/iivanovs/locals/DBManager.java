package com.iivanovs.locals;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.iivanovs.locals.entity.Local;
import com.iivanovs.locals.entity.LocalImg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "Locals";
    private static final int DB_VERSION = 1;

    // Table names
    private static final String TABLE_LOCAL = "Local";
    private static final String TABLE_LOCAL_IMG = "Local_img";

    // Common column names
    private static final String COL_ID = "id";

    // LOCAL column names
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DATE = "date";
    private static final String COL_LON = "lon";
    private static final String COL_LAT = "lat";

    // LOCAL_IMG column names
    private static final String COL_LOCAL_ID = "local_id";
    private static final String COL_IMG_PATH = "img_path";

    // Table Create Statements
    // Create LOCAL table statements
    private static final String CREATE_LOCAL_TABLE = "CREATE TABLE " + TABLE_LOCAL + "( " +
            COL_ID + " integer primary key autoincrement, " +
            COL_DESCRIPTION + " text, " +
            COL_LAT + " text, " +
            COL_LON + " text, " +
            COL_DATE + " text)";

    // Create LOCAL_IMG table statements
    private static final String CREATE_LOCAL_IMG_TABLE = "create table " + TABLE_LOCAL_IMG + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_IMG_PATH + " text, " +
            COL_LOCAL_ID + " INTEGER, " +
            "FOREIGN KEY(" + COL_LOCAL_ID + ") REFERENCES " + TABLE_LOCAL + "(_ID))";


    public DBManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCAL_TABLE);
        db.execSQL(CREATE_LOCAL_IMG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_LOCAL);
        db.execSQL("drop table if exists " + TABLE_LOCAL_IMG);
        onCreate(db);
    }

    public long createLocal(Local local) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DESCRIPTION, local.getDescription());
        values.put(COL_LAT, local.getLat());
        values.put(COL_LON, local.getLon());
        values.put(COL_DATE, getDateTime());

        // insert row
        long id = db.insert(TABLE_LOCAL, null, values);

        return id;
    }

    public List<Local> getAllLocals() {
        ArrayList<Local> localList = new ArrayList<Local>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        cursor = db.rawQuery("select * from " + TABLE_LOCAL, null);

        while (cursor.moveToNext()) {
            Local aLocal = new Local(cursor.getString(1), cursor.getString(2), cursor.getString(3));
            aLocal.setId(cursor.getInt(0));
            aLocal.setDate(cursor.getString(4));

            localList.add(aLocal);
        }

        return localList;
    }

    public Local getLocalById(int id) {
        ArrayList<Local> localList = new ArrayList<Local>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        cursor = db.rawQuery("select * from " + TABLE_LOCAL + " where " + COL_ID + "=" + id, null);

        while (cursor.moveToNext()) {
            Local aLocal = new Local(cursor.getString(1), cursor.getString(2), cursor.getString(3));
            aLocal.setId(cursor.getInt(0));
            aLocal.setDate(cursor.getString(4));

            localList.add(aLocal);
        }

        if (localList.get(0) != null)
            return localList.get(0);
        else
            return null;
    }

    public int updateLocal(Local local) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DESCRIPTION, local.getDescription());
        values.put(COL_LAT, local.getLat());
        values.put(COL_LON, local.getLon());
        values.put(COL_DATE, getDateTime());

        // updating row
        return db.update(TABLE_LOCAL, values,
                COL_ID + " = ?",
                new String[]{String.valueOf(local.getId())});
    }

    public void deleteLocal(Local local) {
        SQLiteDatabase db = this.getWritableDatabase();

        // get all LocalImgs
        List<LocalImg> allLocalImgs = getAllLocalImgs(local.getId());

        // delete all LocalImgs
        for (LocalImg localImg : allLocalImgs) {
            deleteLocalImg(localImg);
        }

        db.delete(TABLE_LOCAL, COL_ID + " = ?",
                new String[]{String.valueOf(local.getId())});
    }

    public List<Local> searchLocations(String searchText) {
        ArrayList<Local> localList = new ArrayList<Local>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        cursor = db.rawQuery("select * from " + TABLE_LOCAL + " where " + COL_DESCRIPTION + " like '%" + searchText + "%'", null);

        while (cursor.moveToNext()) {
            Local aLocal = new Local(cursor.getString(1), cursor.getString(2), cursor.getString(3));
            aLocal.setId(cursor.getInt(0));
            aLocal.setDate(cursor.getString(4));

            localList.add(aLocal);
        }

        return localList;
    }

    public long createLocalImg(LocalImg localImg) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_IMG_PATH, localImg.getImg_path());
        values.put(COL_LOCAL_ID, localImg.getLocal_id());

        long id = db.insert(TABLE_LOCAL_IMG, null, values);

        return id;
    }

    public List<LocalImg> getAllLocalImgs(int localId) {
        ArrayList<LocalImg> localOmgList = new ArrayList<LocalImg>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        cursor = db.rawQuery("select * from " + TABLE_LOCAL_IMG + " where " + COL_LOCAL_ID + "=" + localId, null);

        while (cursor.moveToNext()) {
            LocalImg aLocalImg = new LocalImg(cursor.getString(1), cursor.getInt(2));
            aLocalImg.setId(cursor.getInt(0));
            localOmgList.add(aLocalImg);
        }

        return localOmgList;
    }

    public List<LocalImg> getTotalLocalImgs() {
        ArrayList<LocalImg> localOmgList = new ArrayList<LocalImg>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        cursor = db.rawQuery("select * from " + TABLE_LOCAL_IMG, null);

        while (cursor.moveToNext()) {
            LocalImg aLocalImg = new LocalImg(cursor.getString(1), cursor.getInt(2));
            aLocalImg.setId(cursor.getInt(0));
            localOmgList.add(aLocalImg);
        }

        return localOmgList;
    }

    public void deleteLocalImg(LocalImg localImg) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_LOCAL_IMG, COL_ID + " = ?",
                new String[]{String.valueOf(localImg.getId())});
    }

    public void eraseLocalTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        final String ERASE_LOCAL_TABLE = "delete from " + TABLE_LOCAL;

        db.execSQL(ERASE_LOCAL_TABLE);
        db.close();
    }

    public void eraseLocalImgTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        final String ERASE_LOCAL_TABLE = "delete from " + TABLE_LOCAL_IMG;

        db.execSQL(ERASE_LOCAL_TABLE);
        db.close();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
