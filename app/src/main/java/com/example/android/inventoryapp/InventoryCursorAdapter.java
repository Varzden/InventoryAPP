package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Varzden on 15.7.2017..
 */


/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of inventory data in the {@link Cursor}.
 */

public class InventoryCursorAdapter extends CursorAdapter {


    private static final String TAG = InventoryCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */

    protected InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
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
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView product_name = (TextView) view.findViewById(R.id.inventory_item_name_text);
        TextView product_quantity = (TextView) view.findViewById(R.id.inventory_item_current_quantity_text);
        TextView product_price = (TextView) view.findViewById(R.id.inventory_item_price_text);
        ImageView product_add_btn = (ImageView) view.findViewById(R.id.sale_product);
        ImageView product_thumbnail = (ImageView) view.findViewById(R.id.product_thumbnail);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int thumbnailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PICTURE);
        int salesColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEMS_SOLD);

        int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        final String productName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int products_sold = cursor.getInt(salesColumnIndex);
        String productPrice = "Price $" + cursor.getString(priceColumnIndex);
        Uri thumbUri = Uri.parse(cursor.getString(thumbnailColumnIndex));

        String productQuantity = String.valueOf(quantity) + " Inventory";

        final Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        Log.d(TAG, "Uri: " + currentProductUri + " Product name: " + productName + " id: " + id);

        product_name.setText(productName);
        product_quantity.setText(productQuantity);
        product_price.setText(productPrice);

        //Glide
        Glide.with(context).load(thumbUri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.ic_insert_placeholder)
                .crossFade()
                .centerCrop()
                .into(product_thumbnail);

        // On click listener for shop button
        product_add_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, productName + " quantity= " + quantity);
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();

                if (quantity > 0) {
                    int quant = quantity;
                    int pSold = products_sold;
                    Log.d(TAG, "new quantity= " + quant);
                    // Decrease quantity by one
                    values.put(InventoryEntry.COLUMN_QUANTITY, --quant);

                    // increase item sold by one
                    values.put(InventoryEntry.COLUMN_ITEMS_SOLD, ++pSold);

                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null
                    );

                    context.getContentResolver().notifyChange(currentProductUri, null);
                }

                // If item drops below 0 make Toast with "Item out of stock" text
                else {
                    Toast.makeText(context, "Item out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}


