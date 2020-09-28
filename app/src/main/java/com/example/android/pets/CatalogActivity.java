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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    PetDbHelper mDbHelper;
    PetCursorAdapter adapter;
    ListView petListView;
    private static final int PET_LOADER = 0;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        fab = (FloatingActionButton) findViewById(R.id.fab);

        petListView =  findViewById(R.id.text_view_pet);
        mDbHelper = new PetDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);
        getSupportLoaderManager().initLoader(PET_LOADER,null,this);
        adapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(adapter);
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);
                Uri uri = ContentUris.withAppendedId(PetsContract.PetEntry.CONTENT_URI,l);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void insertData() {

        ContentValues values = new ContentValues();
        values.put(PetsContract.PetEntry.COLUMN_PET_NAME,"ffff");
        values.put(PetsContract.PetEntry.COLUMN_PET_BREED,"hgjgh");
        values.put(PetsContract.PetEntry.COLUMN_PET_GENDER, PetsContract.PetEntry.GENDER_MALE);
        values.put(PetsContract.PetEntry.COLUMN_PET_WEIGHT,9);
        Uri newRowId = getContentResolver().insert(PetsContract.PetEntry.CONTENT_URI,values);
        Log.i("Insert Status", String.valueOf(newRowId));

    }
    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetsContract.PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection  = {
                                PetsContract.PetEntry._ID,
                                PetsContract.PetEntry.COLUMN_PET_NAME,
                                PetsContract.PetEntry.COLUMN_PET_BREED
                                };

        return new CursorLoader(this,
                                 PetsContract.PetEntry.CONTENT_URI,
                                 projection,
                                null,
                                null,
                                null);
                          }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        adapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
                   adapter.swapCursor(null);
    }
}
