package com.example.android.store.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.store.data.ProductContract.ProductsEntry;

/**
 * Created by sgomezp on 10/07/2017.
 */

public class ProductProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the product table */
    private static final int PRODUCT = 100;

    /** URI matcher code for the content URI for a single product in the product table */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.


        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY,ProductContract.PATH_PRODUCT, PRODUCT);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY,ProductContract.PATH_PRODUCT + "/#", PRODUCT_ID);

    }

    // Database helper object
    private ProductDbHelper mDbHelper;

    // Track the number of rows that were deleted
    int rowsDeleted;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }


    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                // For the PRODUCT code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the product table.
                cursor = database.query(ProductsEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.product/product/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the product table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;


    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        Context context = getContext();
        // Check that the name is not null
        String name = values.getAsString(ProductsEntry.COLUMN_PRODUCT_NAME);
        if (name == null){
            throw  new IllegalArgumentException("Product requires a name");
        }

        // Check that the shipment is valid
        Integer shipments = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_SHIPMENTS);
        if (shipments == null || !ProductsEntry.isValidShipment(shipments)){
            throw  new IllegalArgumentException("Product requires valid shipment");
        }

        // If the price is provided, check that it's greater than or equal to 0 €

        Integer price = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0){
            throw new IllegalArgumentException("Product requires valid price");
        }

        // If the stock is provided, check that it's greater than or equal to 0

        Integer stock = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_STOCK);
        if (stock != null && stock < 0){
            throw new IllegalArgumentException("Product requires a valid Stock value");
        }

        // If the sales is provided, check that it's greater than or equal to 0

        Integer sales = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_SALES);
        if (sales != null && sales < 0){
            throw new IllegalArgumentException("Product requires a valid Sales value");
        }

        // Check that the supplier is not null
        String supplier = values.getAsString(ProductsEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier == null){
            throw  new IllegalArgumentException("Product requires a valid supplier");
        }

        // Check that the image is not null
        String image = values.getAsString(ProductsEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null){
            throw  new IllegalArgumentException("Product requires a valid image");
        }

        // Get a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductsEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted !=0){
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rowsDeleted;

                }
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted !=0){
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rowsDeleted;

                }
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    /**
     * Update product in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ProductsEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductsEntry.COLUMN_PRODUCT_NAME)) {
            // Check that the name is not null
            String name = values.getAsString(ProductsEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the {@link ProductsEntry#COLUMN_PRODUCT_SHIPMENTS} key is present,
        // check that the shipments value is valid.
        if (values.containsKey(ProductsEntry.COLUMN_PRODUCT_SHIPMENTS)) {

            // Check that the shipments is valid
            Integer shipments = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_SHIPMENTS);
            if (shipments == null || !ProductsEntry.isValidShipment(shipments)) {
                throw new IllegalArgumentException("Product requires valid shipments");
            }
        }

        // If the {@link ProductsEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(ProductsEntry.COLUMN_PRODUCT_PRICE)) {

            // If the price is provided, check that it's greater than or equal to 0 €
            Integer price = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // If the {@link ProductsEntry#COLUMN_PRODUCT_STOCK} key is present,
        // check that the stock value is valid.
        if (values.containsKey(ProductsEntry.COLUMN_PRODUCT_STOCK)) {

            // If the stock is provided, check that it's greater than or equal to 0
            Integer stock = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_STOCK);
            if (stock != null && stock < 0) {
                throw new IllegalArgumentException("Product requires valid stock");
            }
        }

        // If the {@link ProductsEntry#COLUMN_PRODUCT_SALES} key is present,
        // check that the sales value is valid.
        if (values.containsKey(ProductsEntry.COLUMN_PRODUCT_SALES)) {

            // If the sales is provided, check that it's greater than or equal to 0
            Integer sales = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_SALES);
            if (sales != null && sales < 0) {
                throw new IllegalArgumentException("Product requires valid sales value");
            }
        }

        // If the {@link ProductsEntry#COLUMN_PRODUCT_SUPPLIER} key is present,
        // check that the supplier value is not null.
        if (values.containsKey(ProductsEntry.COLUMN_PRODUCT_SUPPLIER)) {
            // Check that the supplier is not null
            String supplier = values.getAsString(ProductsEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Product requires a supplier phone");
            }
        }

        // If the {@link ProductsEntry#COLUMN_PRODUCT_IMAGE} key is present,
        // check that the image value is not null.
        if (values.containsKey(ProductsEntry.COLUMN_PRODUCT_IMAGE)) {
            // Check that the image is not null
            String image = values.getAsString(ProductsEntry.COLUMN_PRODUCT_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Product requires a image value");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProductsEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return ProductsEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
