package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Varzden on 15.7.2017..
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String TAG = InventoryDbHelper.class.getSimpleName();


     // Name of the database file

    private static final String DATABASE_NAME = "inventory.db";


     // Database version. If you change the database schema, you must increment the database version.

    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


     // This is called when the database is created for the first time.


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_PRICE + " REAL NOT NULL DEFAULT 0.0, "
                + InventoryEntry.COLUMN_PICTURE + " TEXT NOT NULL DEFAULT 'No images', "
                + InventoryEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL DEFAULT 'New Product ', "
                + InventoryEntry.COLUMN_ITEMS_SOLD + " INTEGER NOT NULL DEFAULT 0 "
                + ");";

        db.execSQL(SQL_CREATE_INVENTORY);
    }


     // This is called when the database needs to be upgraded.

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(" DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
