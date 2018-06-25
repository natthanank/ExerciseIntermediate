package com.example.exercise2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    protected  static final String DATABASE_NAME = "SportsDatabase";

    public static final String SPORT = "SPORT";
    public static final String NAME = "Name";

    public static final int CRAETE = 0;
    public static final int UPDATE = 1;
    public static final int DELETE = 2;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + SPORT +
                " ( " + NAME + " TEXT) ";

        sqLiteDatabase.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql = "DROP TABLE IF EXISTS " + SPORT;
        sqLiteDatabase.execSQL(sql);

        onCreate(sqLiteDatabase);
    }

    public boolean create(String sport) {
        ContentValues values = new ContentValues();

        values.put(NAME, sport);

        SQLiteDatabase db = this.getWritableDatabase();

        long row = db.insertOrThrow(SPORT, null, values);
        Log.i("row of user" , Long.toString(row) + " : " + sport);

        return (row > 0) ? true : false;
    }

    public ArrayList<String> read(String query) {

        ArrayList<String> sports = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Filter results WHERE "title" = 'My Title'
        String selection = NAME + " LIKE  ? ";
        String[] selectionArgs = { "Badminton" };
        String rawQuery = "SELECT * FROM SPORT WHERE upper(Name) LIKE '%" + query.toUpperCase() + "%'";

        Cursor cursor = db.rawQuery(rawQuery, null );

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(NAME));
                sports.add(name);
            } while (cursor.moveToNext());
        }


        cursor.close();
        db.close();

        return sports;
    }

}
