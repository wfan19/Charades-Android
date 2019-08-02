package com.example.charades;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;
    public static final String DB_NAME = "WordBank.db";
    public static final String TABLE_NAME = "WordBank";
    private static final String create_table_sql = "create table " + TABLE_NAME
            + " (word1 varchar(20) not null, word2 varchar(20), tag varchar(20));";

    //TODO: Add column for boolean (INT?) 'DEFAULT' for saving default word bank

    private static final String TAG = "DatabaseHelper";

    public static DatabaseHelper databasehelper;


    public static DatabaseHelper getInstance(Context context){
        if (databasehelper == null) {
            Log.d(TAG, "databasehelper is null; creating new DatabaseHelper right now.");
            return new DatabaseHelper(context);
        } else {
            Log.d(TAG, "Databasehelper already exists, returning right now.");
            return databasehelper;
        }
    }


    public DatabaseHelper(Context context) {
        // 传递数据库名与版本号给父类
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: ");
        Log.d(TAG, "Creating sql table");
        db.execSQL(create_table_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: ");
        if (newVersion > oldVersion) {
            //删除老表
            db.execSQL("drop table " + TABLE_NAME);
            //重新创建表
            onCreate(db);
        }
    }

    public void insertData(String word1, String word2, String tag){
        Log.d(TAG, "Tag: " + tag);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("word1",word1);
        values.put("word2",word2);
        values.put("tag",tag);
        db.insert(TABLE_NAME,null,values);
        Log.d(TAG, "insertData: succeed");
        db.close();
    }

    public Cursor query() {
        //数据库可读
        SQLiteDatabase database = getReadableDatabase();
        //查找
        Cursor query = database.query(TABLE_NAME, null, null, null, null, null, null);
        return query;
    }

    public void delete(String tag) {
        SQLiteDatabase database = getWritableDatabase();
        //当条件满足id = 传入的参数的时候,就删除那整行数据,有可能有好几行都满足这个条件,满足的全部都删除
        String where = "tag = ?";
        String[] whereArgs = {tag + ""};
        database.delete(TABLE_NAME, where, whereArgs);
        database.close();
    }


    public Cursor search (String[] return_columns, String condition_column, String searchString) {
        searchString = "%" + searchString + "%";
        String where = condition_column + " LIKE ?";
        String[]whereArgs = new String[]{searchString};

        Cursor cursor = null;

        try {
            SQLiteDatabase database = getWritableDatabase();
            cursor = database.query(TABLE_NAME, return_columns, where, whereArgs, null, null, null);
        } catch (Exception e) {
            Log.d(TAG, "SEARCH EXCEPTION! " + e);
        }

        return cursor;
    }

    public Cursor search (String[] return_columns, String condition_column, String[] whereArgs) {
        String where = condition_column + " LIKE ?";
        Cursor cursor = null;
        for(String str : whereArgs)
            str = "%" + str + "%";

        try {
            SQLiteDatabase database = getWritableDatabase();
            cursor = database.query(TABLE_NAME, return_columns, where, whereArgs, null, null, null);
        } catch (Exception e) {
            Log.d(TAG, "SEARCH EXCEPTION! " + e);
        }

        return cursor;
    }

    public Cursor getUnique(String column)
    {
        SQLiteDatabase db = getWritableDatabase();
        return db.rawQuery("SELECT DISTINCT " + column + " FROM " + TABLE_NAME, null);
    }

    public void clear()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME,null,null);
        db.close();
    }

    public void setCurrent(String tag)
    {
        Log.d(TAG, "Setting current to " + tag);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("word2",tag);
        String where = "word1 = ?";
        String[] whereArgs = {"current"};
        db.update(TABLE_NAME,cv,where, whereArgs);
        db.close();
    }

    public void createCurrent(String tag)
    {
        Log.d(TAG, "Creating current");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("word2",tag);
        cv.put("word1","current");
        db.insert(TABLE_NAME,null,cv);
        db.close();
    }

    public String getCurrent()
    {
        Log.d(TAG, "Getting current");
        SQLiteDatabase db = getWritableDatabase();
        Cursor query = db.rawQuery("SELECT word2 FROM " + TABLE_NAME + " WHERE word1 = \"current\"",null);
        if(query.moveToFirst())
            return query.getString(query.getColumnIndex("word2"));
        return null;
    }

}

