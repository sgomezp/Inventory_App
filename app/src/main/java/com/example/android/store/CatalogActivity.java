package com.example.android.store;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.store.data.ProductContract.ProductsEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list);


        // The FAB button shrink when the user scroll the lisView
        productListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                fab.setSize(FloatingActionButton.SIZE_AUTO);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                fab.setSize(FloatingActionButton.SIZE_MINI);
            }
        });


        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list of item for each row of product data in the Cursor
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        // Setup item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}
                // For example, the URI would be "content://com.example.android.product/2"
                // if the product with ID 2 was clicked on.

                Uri currentProductUri = ContentUris.withAppendedId(ProductsEntry.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link EditorActivity} to display the data for the current product
                startActivity(intent);


            }
        });
    }


    /**
     * Helper method to insert hardcoded products data into the database. For debugging purposes only.
     */
    private void InsertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and Deathly Hallows's product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductsEntry.COLUMN_PRODUCT_NAME, "Deathly Hallows");
        values.put(ProductsEntry.COLUMN_PRODUCT_PRICE, 25);
        values.put(ProductsEntry.COLUMN_PRODUCT_SHIPMENTS, ProductsEntry.SHIPMENT_STOREHOUSE);
        values.put(ProductsEntry.COLUMN_PRODUCT_STOCK, 7);
        values.put(ProductsEntry.COLUMN_PRODUCT_SUPPLIER, "34 658 3654 956");
        values.put(ProductsEntry.COLUMN_PRODUCT_SALES, 10);
        values.put(ProductsEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(Uri.parse("content://com.android.providers.media.documents/document/image%3A6799")));

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.

        Uri newUri = getContentResolver().insert(ProductsEntry.CONTENT_URI, values);
        Log.i("I'M INSERT PRODUCT() ", "newUri is: " + newUri);

    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductsEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.delete_all_product_failed),
                    Toast.LENGTH_SHORT).show();

        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_all_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
        Log.i("CatalogActivity", rowsDeleted + " rows deleted from product Database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                ProductsEntry._ID,
                ProductsEntry.COLUMN_PRODUCT_NAME,
                ProductsEntry.COLUMN_PRODUCT_PRICE,
                ProductsEntry.COLUMN_PRODUCT_STOCK,
                ProductsEntry.COLUMN_PRODUCT_SALES,
                ProductsEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductsEntry.COLUMN_PRODUCT_IMAGE,
                ProductsEntry.COLUMN_PRODUCT_SHIPMENTS
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductsEntry.CONTENT_URI,  // ProviderThe content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No Selection clause
                null,                   // No Selection arguments
                null);                  // Default sort orderreturn null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Update {@link PetCursorAdapter} with this new cursor containig updated pet data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}
