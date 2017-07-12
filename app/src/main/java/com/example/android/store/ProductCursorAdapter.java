package com.example.android.store;

/**
 * Created by sgomezp on 09/07/2017.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.store.data.ProductContract.ProductsEntry;
import com.squareup.picasso.Picasso;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of productt data in the {@link Cursor}.
 */

public class ProductCursorAdapter extends CursorAdapter {


    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {

        super(context, c, 0 /* flags */);
    }


    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        /// Find individual views that we want to modify in the list item layout
        TextView productNameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView productPriceTextView = (TextView) view.findViewById(R.id.product_price);
        TextView productStockTextView = (TextView) view.findViewById(R.id.product_stock);
        TextView productImageTextView = (TextView) view.findViewById(R.id.image_uri);
        TextView productSalesTextView = (TextView) view.findViewById(R.id.product_sales);
        ImageView productImageView = (ImageView) view.findViewById(R.id.image);
        Button productSalesButton = (Button) view.findViewById(R.id.sales_button);

        // Find the columns of product attributes that we're interested in

        int nameColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_PRICE);
        final int stockColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_STOCK);
        final int salesColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_SALES);
        int imageColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_IMAGE);
        final Uri uri = ContentUris.withAppendedId(ProductsEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(ProductsEntry._ID)));

        // Read the products attributes from the cursor for the current products
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final int productStock = cursor.getInt(stockColumnIndex);
        final int productSales = cursor.getInt(salesColumnIndex);
        String productImage = cursor.getString(imageColumnIndex);


        // Update the TextViews with the attributes for the current pet
        productNameTextView.setText(productName);
        productPriceTextView.setText(productPrice);
        productStockTextView.setText(Integer.toString(productStock));
        productSalesTextView.setText(Integer.toString(productSales));
        productImageTextView.setText(productImage);
        Picasso.with(context).load(productImage).into(productImageView);

        // Set button Sales  a clickListener

        productSalesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (productStock == 0) {
                    // Warning the user that stock is 0 and can't be updated
                    Toast.makeText(context, context.getString(R.string.sales_zero), Toast.LENGTH_SHORT).show();
                } else {
                    int newStock = productStock - 1;
                    int newSales = productSales + 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductsEntry.COLUMN_PRODUCT_STOCK, newStock);
                    values.put(ProductsEntry.COLUMN_PRODUCT_SALES, newSales);
                    context.getContentResolver().update(uri, values, null, null);
                }
            }

        });

    }

}
