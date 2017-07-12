package com.example.android.store;

/**
 * Created by sgomezp on 07/07/2017.
 */


import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.store.data.ProductContract.ProductsEntry;
import com.squareup.picasso.Picasso;


// Allows user to create a new product or edit an existing one.

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    // Tag for the log messages
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 0;

    int mStock = 0;
    String phone = "";


    // Content URI for the existing product (null if it's a new product)
    private Uri mCurrentProductUri;

    // EditText field to enter the product's name
    private EditText mNameEditText;

    //ImageView field to enter the product's image
    private ImageView mImageEditText;

    // EditText Image mImageEditTextUri
    private TextView mImageTextViewUri;

    //EditText field to enter the product's price
    private EditText mPriceEditText;

    //EditText field to enter the product's shipment
    private Spinner mShipmentSpinner;

    //EditText field to enter the product's stock
    private EditText mStockEditText;

    // Image Button Plus Stock
    private ImageButton mButtonEditPlus;

    // Image Button Minus Stock
    private ImageButton mButtonEditMinus;

    // Image Button order to Supplier
    private ImageButton mButtonEditOrder;

    // Sock Variable
    //private int mProductStock;

    // EditText field to enter products sales value
    private EditText mSalesEditText;

    // Uri of the image
    private Uri mUriImage;

    // Button select image
    private Button mButtonEditImage;

    // EditText field to enter the product's supplier
    private EditText mSupplierEditText;


    /**
     * Shipment of the product. The possible values are:
     * 0 for in Storehouse, 1 for sent to client, 2 delivered. The client received the product.
     */
    private int mShipment = ProductsEntry.SHIPMENT_STOREHOUSE;


    //Boolean flag that keeps track of whether the product has been edited (true) or not (false)
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        // Prevent the soft keyboard from pushing the view up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Examine the intent that was used to launch this activity
        // in order to figure out if we are creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, the we know that we are
        // creating a new product.

        Log.i(LOG_TAG, "Enter onCreate before the if. currentUri is: " + mCurrentProductUri);

        if (mCurrentProductUri == null) {

            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mShipmentSpinner = (Spinner) findViewById(R.id.spinner_shipment);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mStockEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mSalesEditText = (EditText) findViewById(R.id.edit_product_solds);
        mImageEditText = (ImageView) findViewById(R.id.edit_product_image); // La imagen
        mImageTextViewUri = (TextView) findViewById(R.id.edit_product_image_uri); // Texto URI oculto
        mButtonEditImage = (Button) findViewById(R.id.edit_product_image_button);
        mButtonEditPlus = (ImageButton) findViewById(R.id.plus_button);
        mButtonEditMinus = (ImageButton) findViewById(R.id.minus_button);
        mButtonEditOrder = (ImageButton) findViewById(R.id.order_button);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mShipmentSpinner.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mStockEditText.setOnTouchListener(mTouchListener);
        mSalesEditText.setOnTouchListener(mTouchListener);
        mButtonEditImage.setOnTouchListener(mTouchListener);
        mButtonEditPlus.setOnTouchListener(mTouchListener);
        mButtonEditMinus.setOnTouchListener(mTouchListener);
        mButtonEditOrder.setOnTouchListener(mTouchListener);

        setupSpinner();

        // Set listener to button Select image
        mButtonEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();

            }
        });

        // Set listener to button plus stock
        mButtonEditPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                increaseStock(view);

            }
        });

        // Set listener to button minus stock
        mButtonEditMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseStock(view);
            }
        });

        // Set listener to button order stock to supplier
        mButtonEditOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialer(view);
            }
        });

    }

    public void openDialer(View view) {

        phone = mSupplierEditText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
        startActivity(intent);
    }


    public void increaseStock(View view) {

        mStock = Integer.parseInt(mStockEditText.getText().toString()) + 1;
        Log.i(LOG_TAG, "mStock es: " + mStock);

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsEntry.COLUMN_PRODUCT_STOCK, mStock);
        getContentResolver().update(mCurrentProductUri, contentValues, null, null);

    }

    public void decreaseStock(View view) {

        mStock = Integer.parseInt(mStockEditText.getText().toString()) - 1;
        if (mStock < 0) {
            // Warning the user that stock is 0 and can't be updated
            Toast.makeText(this, this.getString(R.string.stock_zero), Toast.LENGTH_SHORT).show();
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductsEntry.COLUMN_PRODUCT_STOCK, mStock);
            getContentResolver().update(mCurrentProductUri, contentValues, null, null);

        }

    }

    public void openImageSelector() {
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUriImage = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUriImage.toString());

                mImageTextViewUri.setText(mUriImage.toString());
                //mImageEditText.setImageBitmap(getBitmapFromUri(mUriImage));
                Picasso.with(this).load(mUriImage.toString()).into(mImageEditText);

            }
        }
    }


    /**
     * Setup the dropdown spinner that allows the user to select the shipments state of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter shipmentSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_shipment_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        shipmentSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mShipmentSpinner.setAdapter(shipmentSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mShipmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.shipment_storehouse))) {
                        mShipment = ProductsEntry.SHIPMENT_STOREHOUSE; // Storehouse
                    } else if (selection.equals(getString(R.string.shipment_sent))) {
                        mShipment = ProductsEntry.SHIPMENT_SENT; // Sent to client
                    } else {
                        mShipment = ProductsEntry.SHIPMENT_DELIVERED; // Client received product
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mShipment = 0; // storehouse
            }
        });
    }

    /**
     * Get user input from editor and save new product into database.
     */
    private void saveProduct() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String imageString = mImageTextViewUri.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String stockString = mStockEditText.getText().toString().trim();
        String salesString = mSalesEditText.getText().toString().trim();


        if (nameString.isEmpty() || supplierString.isEmpty() || imageString.isEmpty()) {

            Toast.makeText(this, (R.string.error_name_blank), Toast.LENGTH_LONG).show();
            return;
        }


        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(imageString) &&
                TextUtils.isEmpty(stockString) && TextUtils.isEmpty(salesString) &&
                mShipment == ProductsEntry.SHIPMENT_STOREHOUSE) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductsEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductsEntry.COLUMN_PRODUCT_SHIPMENTS, mShipment);
        values.put(ProductsEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        //values.put(ProductsEntry.COLUMN_PRODUCT_STOCK, stockString);
        //values.put(ProductsEntry.COLUMN_PRODUCT_SALES, salesString);
        values.put(ProductsEntry.COLUMN_PRODUCT_IMAGE, imageString);

        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ProductsEntry.COLUMN_PRODUCT_PRICE, price);

        // If the stock is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int stock = 0;
        if (!TextUtils.isEmpty(stockString)) {
            stock = Integer.parseInt(stockString);
        }
        values.put(ProductsEntry.COLUMN_PRODUCT_STOCK, stock);

        // If  sales is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int sales = 0;
        if (!TextUtils.isEmpty(salesString)) {
            sales = Integer.parseInt(salesString);
        }
        values.put(ProductsEntry.COLUMN_PRODUCT_SALES, sales);


        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductsEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                finish();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }


        }
        // Close the activity
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductsEntry._ID,
                ProductsEntry.COLUMN_PRODUCT_NAME,
                ProductsEntry.COLUMN_PRODUCT_PRICE,
                ProductsEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductsEntry.COLUMN_PRODUCT_STOCK,
                ProductsEntry.COLUMN_PRODUCT_SALES,
                ProductsEntry.COLUMN_PRODUCT_IMAGE,
                ProductsEntry.COLUMN_PRODUCT_SHIPMENTS};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        DatabaseUtils.dumpCursor(cursor);

        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_PRICE);
            int shipmentsColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_SHIPMENTS);
            int supplierColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_SUPPLIER);
            int stockColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_STOCK);
            int salesColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_SALES);
            int imageColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int shipments = cursor.getInt(shipmentsColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int stock = cursor.getInt(stockColumnIndex);
            int sales = cursor.getInt(salesColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mPriceEditText.setText(Integer.toString(price));
            mImageTextViewUri.setText(image);
            mStockEditText.setText(Integer.toString(stock));
            mSalesEditText.setText(Integer.toString(sales));
            Picasso.with(this).load(image).into(mImageEditText);

            // Shipments is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Storehouse, 1 is Sent, 2 is Delivered).
            // Then call setSelection() so that option is displayed on screen as the current selection

            switch (shipments) {
                case ProductsEntry.SHIPMENT_STOREHOUSE:
                    mShipmentSpinner.setSelection(0);
                    break;
                case ProductsEntry.SHIPMENT_SENT:
                    mShipmentSpinner.setSelection(1);
                    break;
                default:
                    mShipmentSpinner.setSelection(2);
                    break;
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mStockEditText.setText("");


    }
}
