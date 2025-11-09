package com.example.mad_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ConversionHistoryDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FileConverter.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_HISTORY = "conversion_history";

    private static final String COL_ID = "id";
    private static final String COL_USER_EMAIL = "user_email";
    private static final String COL_CONVERSION_TYPE = "conversion_type";
    private static final String COL_ORIGINAL_FILE = "original_file";
    private static final String COL_CONVERTED_FILE = "converted_file";
    private static final String COL_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_IF_NOT_EXISTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_EMAIL + " TEXT, " +
                    COL_CONVERSION_TYPE + " TEXT, " +
                    COL_ORIGINAL_FILE + " TEXT, " +
                    COL_CONVERTED_FILE + " TEXT, " +
                    COL_TIMESTAMP + " INTEGER)";

    public ConversionHistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_IF_NOT_EXISTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Ensure table exists even if this helper wasn't the one that created the DB file
        try {
            db.execSQL(CREATE_TABLE_IF_NOT_EXISTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public long addConversion(String userEmail, String conversionType,
                              String originalFile, String convertedFile) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Ensure table exists before insert (defense-in-depth)
        try {
            db.execSQL(CREATE_TABLE_IF_NOT_EXISTS);
        } catch (Exception ignored) {}

        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = "guest_user";
        }

        ContentValues values = new ContentValues();
        values.put(COL_USER_EMAIL, userEmail);
        values.put(COL_CONVERSION_TYPE, conversionType);
        values.put(COL_ORIGINAL_FILE, originalFile);
        values.put(COL_CONVERTED_FILE, convertedFile);
        values.put(COL_TIMESTAMP, System.currentTimeMillis());

        long result = db.insert(TABLE_HISTORY, null, values);
        db.close();
        return result;
    }

    public List<ConversionHistory> getConversionsByUser(String userEmail) {
        List<ConversionHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Ensure table exists before query
            db.execSQL(CREATE_TABLE_IF_NOT_EXISTS);

            if (userEmail == null || userEmail.trim().isEmpty()) {
                cursor = db.query(TABLE_HISTORY, null, null, null, null, null, COL_TIMESTAMP + " DESC");
            } else {
                String selection = COL_USER_EMAIL + " = ?";
                String[] selectionArgs = {userEmail};
                cursor = db.query(TABLE_HISTORY, null, selection, selectionArgs, null, null, COL_TIMESTAMP + " DESC");
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ConversionHistory history = new ConversionHistory(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_CONVERSION_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_ORIGINAL_FILE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_CONVERTED_FILE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                    );
                    historyList.add(history);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return historyList;
    }

    public ConversionHistory getConversionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ConversionHistory history = null;
        Cursor cursor = null;

        try {
            // Ensure table exists before query
            db.execSQL(CREATE_TABLE_IF_NOT_EXISTS);

            String selection = COL_ID + " = ?";
            String[] selectionArgs = {String.valueOf(id)};
            cursor = db.query(TABLE_HISTORY, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                history = new ConversionHistory(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONVERSION_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ORIGINAL_FILE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONVERTED_FILE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return history;
    }

    public List<ConversionHistory> getAllConversions() {
        List<ConversionHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Ensure table exists before query
            db.execSQL(CREATE_TABLE_IF_NOT_EXISTS);

            cursor = db.query(TABLE_HISTORY, null, null, null, null, null, COL_TIMESTAMP + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ConversionHistory history = new ConversionHistory(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_CONVERSION_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_ORIGINAL_FILE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_CONVERTED_FILE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                    );
                    historyList.add(history);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return historyList;
    }
}

