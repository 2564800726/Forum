package com.blogofyb.forum.utils.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.blogofyb.forum.utils.constant.SQLite;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private static SQLiteDatabase database;

    private MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
        super(context, name, factory, version);
    }

    private MySQLiteOpenHelper(Context context) {
        super(context, SQLite.DATABASE_NAME, null, 1);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null) {
            synchronized (MySQLiteOpenHelper.class) {
                if (database == null) {
                    database = new MySQLiteOpenHelper(context).getWritableDatabase();
                }
            }
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + SQLite.TABLE_NAME + " ("
                + SQLite.ACCOUNT + " TEXT NOT NULL, "
                + SQLite.PASSWORD + " TEXT NOT NULL, "
                + SQLite.USER_NAME + " TEXT, "
                + SQLite.GENDER + " TEXT, "
                + SQLite.BIRTHDAY + " DATE, "
                + SQLite.SIGNATURE + " TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
