package com.example.android.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.File;

import static com.example.android.inventoryapp.R.drawable.ic_insert_placeholder;

/**
 * Created by Varzden on 15.7.2017..
 * Allows user to create a new product or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = EditorActivity.class.getSimpleName();

    public static final int PHOTO_REQUEST = 10;

    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 100;

    //Loader identifier
    private static final int CURRENT_INVENTORY_LOADER = 0;

    //Product Query projection
    public final String[] PRODUCT_COLUMNS = {
            InventoryEntry._ID,
            InventoryEntry.COLUMN_NAME,
            InventoryEntry.COLUMN_QUANTITY,
            InventoryEntry.COLUMN_PRICE,
            InventoryEntry.COLUMN_DESCRIPTION,
            InventoryEntry.COLUMN_ITEMS_SOLD,
            InventoryEntry.COLUMN_PICTURE
    };

    private Uri mCurrentProductUri;

    // EditTexts
    private EditText mProductName;
    private EditText mProductDescription;
    private EditText mProductInventory;
    private EditText mProductItemSold;
    private EditText mProductPrice;

    //ImageView
    private ImageView mProductPhoto;

    //TextViews
    private TextView labelUpdate_Save;
    private TextView labelDelete;

    //ImageButtons
    private ImageButton deleteIB;
    private ImageButton updateIB;

    //Photo uri
    private String mCurrentPhotoUri = "no images";


    // Boolean flag that keeps track of whether the project has been edited (true) or not (false)
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

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            //User click new product
            setTitle(getString(R.string.add_product_title));
            labelUpdate_Save.setText(R.string.save_product_label);
            //We can't order product for new ones
            deleteIB.setVisibility(View.GONE);
            labelDelete.setVisibility(View.GONE);

        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.edit_product_title));
            labelUpdate_Save.setText(R.string.update_product_label);
            labelDelete.setVisibility(View.VISIBLE);
            deleteIB.setVisibility(View.VISIBLE);
            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(CURRENT_INVENTORY_LOADER, null, this);
        }

        //Connect elements to required view

        mProductPhoto = (ImageView) findViewById(R.id.image_product_photo);

        // Find all relevant views that we will need to read user input from
        mProductName = (EditText) findViewById(R.id.item_name_ET);
        mProductDescription = (EditText) findViewById(R.id.item_description_ET);
        mProductInventory = (EditText) findViewById(R.id.item_quantity_ET);
        mProductItemSold = (EditText) findViewById(R.id.current_sales_ET);
        mProductPrice = (EditText) findViewById(R.id.item_price_ET);

        deleteIB = (ImageButton) findViewById(R.id.delete_product_button);
        updateIB = (ImageButton) findViewById(R.id.save_product_button);

        labelUpdate_Save = (TextView) findViewById(R.id.update_save_label);
        labelDelete = (TextView) findViewById(R.id.delete_label);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductPhoto.setOnTouchListener(mTouchListener);
        mProductName.setOnTouchListener(mTouchListener);
        mProductDescription.setOnTouchListener(mTouchListener);
        mProductInventory.setOnTouchListener(mTouchListener);
        mProductItemSold.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);


        //On click listeners for photo and buttons
        mProductPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPhotoProductUpdate(view);
            }
        });

        updateIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
            }
        });

        deleteIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });


    }

    /**
     * Get user input from editor and save/update product into database.
     */
    private void saveProduct() {
        //Read Values from text field
        // Use trim to eliminate leading or trailing white space
        String nameString = mProductName.getText().toString().trim();
        String descriptionString = mProductDescription.getText().toString().trim();
        String inventoryString = mProductInventory.getText().toString().toString();
        String salesString = mProductItemSold.getText().toString().trim();
        String priceString = mProductPrice.getText().toString().trim();

        //Check if is new or if an update
        // and check if all the fields in the editor are blank
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(descriptionString)
                || TextUtils.isEmpty(inventoryString) || TextUtils.isEmpty(salesString)
                || TextUtils.isEmpty(priceString)) {

            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, R.string.error_missing_textfields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryEntry.COLUMN_DESCRIPTION, descriptionString);
        values.put(InventoryEntry.COLUMN_QUANTITY, inventoryString);
        values.put(InventoryEntry.COLUMN_ITEMS_SOLD, salesString);
        values.put(InventoryEntry.COLUMN_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_PICTURE, mCurrentPhotoUri);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.error_inserting_product,
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.update_successful,
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
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
                Toast.makeText(this, R.string.error_inserting_product, Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.update_successful, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.settings:
                return true;

            case android.R.id.home:
                //if the user didn't make any changes
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

    // This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentProductUri,
                PRODUCT_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Find the columns of project attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_DESCRIPTION);
            int soldColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEMS_SOLD);
            int photoColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PICTURE);



            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int itemSold = cursor.getInt(soldColumnIndex);
            String photo = cursor.getString(photoColumnIndex);
            mCurrentPhotoUri = cursor.getString(photoColumnIndex);


            // Update the views on the screen with the values from the database
            mProductName.setText(name);
            mProductPrice.setText(String.valueOf(price));
            mProductInventory.setText(String.valueOf(quantity));
            mProductDescription.setText(description);
            mProductItemSold.setText(String.valueOf(itemSold));

            //Update photo using Glide
            Glide.with(this).load(mCurrentPhotoUri)
                    .placeholder(R.drawable.ic_insert_placeholder)
                    .error(R.drawable.ic_insert_placeholder)
                    .crossFade()
                    .fitCenter()
                    .into(mProductPhoto);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductName.setText("");
        mProductPrice.setText("");
        mProductInventory.setText("");
        mProductDescription.setText("");
        mProductItemSold.setText("");

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
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
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
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
     * Perform the deletion of the  in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
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


    public void onPhotoProductUpdate(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //We are on M or above so we need to ask for runtime permissions
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                requestPhoto();
            } else {
                // If we don't already have a permission, we should request it
                String[] requestPermission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(requestPermission, EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE);
            }
        } else {
            //Photo request on older devices
            requestPhoto();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //We got a GO from the user
            requestPhoto();
        } else {
            Toast.makeText(this, R.string.err_external_storage_permissions, Toast.LENGTH_LONG).show();
        }
    }

    private void requestPhoto() {
        // invoke the image gallery using intent.
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // Location for finding data
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // Defining data type
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, PHOTO_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                //If we are here, everything processed successfully and we have an Uri data
                Uri mProductPhotoUri = data.getData();
                mCurrentPhotoUri = mProductPhotoUri.toString();
                Log.d(TAG, "Selected images " + mProductPhotoUri);

                //We use Glide to import photo images
                Glide.with(this).load(mProductPhotoUri)
                        .placeholder(ic_insert_placeholder)
                        .crossFade()
                        .fitCenter()
                        .into(mProductPhoto);
            }
        }
    }

}