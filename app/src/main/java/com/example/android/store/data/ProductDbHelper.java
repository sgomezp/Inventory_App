package com.example.android.store.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.store.data.ProductContract.ProductsEntry;

/**
 * Created by sgomezp on 07/07/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    // Constructor
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductsEntry.TABLE_NAME +
                " (" + ProductsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductsEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductsEntry.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL, " +
                ProductsEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                ProductsEntry.COLUMN_PRODUCT_SHIPMENTS + " INTEGER NOT NULL DEFAULT 0, " +
                ProductsEntry.COLUMN_PRODUCT_STOCK + " INTEGER NOT NULL DEFAULT 0,  " +
                ProductsEntry.COLUMN_PRODUCT_SALES + " INTEGER NOT NULL DEFAULT 0, " +
                ProductsEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL );";


        // execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXIST " + ProductsEntry.TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
