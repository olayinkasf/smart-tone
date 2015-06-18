package com.olayinka.smart.tone;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import lib.olayinka.smart.tone.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AppSqlHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_tone";
    private static final int DATABASE_VERSION = 9;
    private static AppSqlHelper sInstance;
    private final Context mContext;

    public AppSqlHelper(Context context) {
        super(context, AppSqlHelper.DATABASE_NAME, null, AppSqlHelper.DATABASE_VERSION);
        mContext = context;
    }

    public static AppSqlHelper instance(Context context) {
        if (sInstance == null)
            sInstance = new AppSqlHelper(context.getApplicationContext());
        return sInstance;
    }

    public static boolean hasData(Context context, String table) {
        SQLiteDatabase database = instance(context).getReadableDatabase();
        Cursor cursor = database.query(table, new String[]{"count(*)"}, null, null, null, null, null);
        if (cursor.moveToNext()) {
            return cursor.getInt(0) != 0;
        }
        cursor.close();
        throw new RuntimeException("Must return a row");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query : makeQueries(R.raw.smart_tone))
            db.execSQL(query);

    }

    private List<String> makeQueries(int resourceId) {
        InputStream inputStream = mContext.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> queries = new ArrayList<>();
        String line;
        try {
            String query = "";
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                query += line + " ";
                if (!line.isEmpty() && line.charAt(line.length() - 1) == ';') {
                    queries.add(query);
                    query = "";
                }
            }
        } catch (IOException ignored) {
        }

        return queries;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        if (oldVersion <= 7) {
            for (String query : makeQueries(R.raw.update_db_7))
                db.execSQL(query);
        }
        if (oldVersion <= 8) {
            for (String query : makeQueries(R.raw.update_db_8))
                db.execSQL(query);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
