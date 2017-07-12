package com.example.android.store.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sgomezp on 06/07/2017.
 */

public class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */

    public static final String CONTENT_AUTHORITY = "com.example.android.store";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_PRODUCT = "product";
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductContract.class.getSimpleName();


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {
    }

    public static abstract class ProductsEntry implements BaseColumns {

        /**
         * The content URI to access the store data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;


        public static final String TABLE_NAME = "product";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_IMAGE = "image";
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_STOCK = "stock";
        public static final String COLUMN_PRODUCT_SALES = "sales";
        public static final String COLUMN_PRODUCT_SHIPMENTS = "shipments";


        // Possible values for Shipments

        public static final int SHIPMENT_STOREHOUSE = 0;    //The is still in the storehouse
        public static final int SHIPMENT_SENT = 1;          // The product was sent
        public static final int SHIPMENT_DELIVERED = 2;     // The Client received the product


        public static boolean isValidShipment(int shipment) {
            if (shipment == ProductsEntry.SHIPMENT_STOREHOUSE ||
                    shipment == ProductsEntry.SHIPMENT_SENT ||
                    shipment == ProductsEntry.SHIPMENT_DELIVERED) {
                return true;
            } else {
                return false;
            }
        }
    }
}
