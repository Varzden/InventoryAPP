package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Varzden on 15.7.2017..
 * <p>
 * API Contract for the Inventory app.
 */

public class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/inventory/ is a valid path for
     * looking at inventory data. content://com.example.android.inventoryapp/stuff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "stuff".
     */

    public static final String PATH_INVENTORY = "inventory";

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single product.
     */

    public InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {

        // The content URI to access the inventory data in the provider

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        // The MIME type of the CONTENT_URI for a list of products.

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // The MIME type of the CONTENT_URI for a single product.

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // name of database table for inventory

        public final static String TABLE_NAME = "inventory";

        // Following represents table data (columns)

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_DESCRIPTION = "description";
        public final static String COLUMN_ITEMS_SOLD = "sales";
        public final static String COLUMN_PICTURE = "picture";

        public static Uri buildInventoryURI(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}
