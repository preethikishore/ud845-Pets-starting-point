/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract;
import com.example.android.pets.data.PetsContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PET_LOADER = 0;
    PetDbHelper mDbHelper;
    PetCursorAdapter adapter;
    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;
    Uri currentData;
    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;
    private boolean mPetHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intentData = getIntent();
        currentData = intentData.getData();
        if (currentData == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
            currentData = PetsContract.PetEntry.CONTENT_URI;
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
        }
        adapter = new PetCursorAdapter(this, null);
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        getSupportLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();
        mDbHelper = new PetDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }


    private void savePet() {
        String pName = mNameEditText.getText().toString().trim();
        String pBreed = mBreedEditText.getText().toString().trim();
        String pWeight = mWeightEditText.getText().toString().trim();
        int pWeight_ = 0;
        pWeight_ = Integer.parseInt(pWeight);
        String pGender = String.valueOf(mGender);
        if (currentData == null && TextUtils.isEmpty(pName) &&
                TextUtils.isEmpty(pWeight) && mGender == PetEntry.GENDER_UNKNOWN) {
            return;
        }


        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, pName);
        values.put(PetEntry.COLUMN_PET_BREED, pBreed);
        values.put(PetEntry.COLUMN_PET_GENDER, pGender);
        if (TextUtils.isEmpty(pWeight)) {
            pWeight_ = 0;
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT, pWeight_);
//        String selectionClause = PetEntry._ID +  "= ?";
//        String[] selectionArgs = {"%"};

        if (getString(R.string.editor_activity_title_new_pet).equals(getTitle().toString())) {
            Uri newRowId = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (newRowId == null) {
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed), Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful), Toast.LENGTH_LONG).show();
            }
        } else if (getString(R.string.editor_activity_title_edit_pet).equals(getTitle().toString())) {
            int newRowId = getContentResolver().update(currentData, values, null, null);
            if (newRowId == 0) {
                Toast.makeText(this, getString(R.string.editor_activity_update_fail), Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, getString(R.string.editor_activity_update_success), Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getString(R.string.editor_activity_title_new_pet).equals(getTitle().toString())) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
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
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();
                // Do nothing for now
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)

                if (!mPetHasChanged && TextUtils.isEmpty(mNameEditText.getText().toString())){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
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

    private void deletePet() {
        if (currentData != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(currentData, null, null);
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                PetsContract.PetEntry._ID,
                PetsContract.PetEntry.COLUMN_PET_NAME,
                PetsContract.PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };


        return new CursorLoader(this,
                currentData,
                projection,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (getString(R.string.editor_activity_title_new_pet).equals(getTitle().toString())) {


        } else {
            if (data.moveToFirst()) {
                // Find the columns of pet attributes that we're interested in
                int nameColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_NAME);
                int breedColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_BREED);
                int genderColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
                int weightColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

                // Extract out the value from the Cursor for the given column index
                String name = data.getString(nameColumnIndex);
                String breed = data.getString(breedColumnIndex);
                int gender = data.getInt(genderColumnIndex);
                int weight = data.getInt(weightColumnIndex);
                mNameEditText.setText(name);
                mBreedEditText.setText(breed);
                mWeightEditText.setText(Integer.toString(weight));
                mGenderSpinner.setSelection(gender);

            }
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged && TextUtils.isEmpty(mNameEditText.getText().toString())) {
            super.onBackPressed();
            return;
        }else {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                           finish();
                        }

                    };

            showUnsavedChangesDialog(discardButtonClickListener);
        }

    }
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



}