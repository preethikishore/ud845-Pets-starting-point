package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PetProvider extends ContentProvider {

    private PetDbHelper petDbHelper;
    private static final int PETS = 100;


    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;
    private static final UriMatcher sUriMatch = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
       sUriMatch.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS,PETS);
       sUriMatch.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS+"/#",PET_ID);
    }

    @Override
    public boolean onCreate() {
        petDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
      SQLiteDatabase sqLiteDatabase = petDbHelper.getReadableDatabase();
      Cursor cursor = null;

      int match = sUriMatch.match(uri);
      switch(match)
      {
        case PETS:
        // For the PETS code, query the pets table directly with the given
        // projection, selection, selection arguments, and sort order. The cursor
        // could contain multiple rows of the pets table.
        // TODO: Perform database query on pets table
            cursor = sqLiteDatabase.query(PetsContract.PetEntry.TABLE_NAME, strings, null, null,
                    null, null, null);
        break;
        case PET_ID:
        // For the PET_ID code, extract out the ID from the URI.
        // For an example URI such as "content://com.example.android.pets/pets/3",
        // the selection will be "_id=?" and the selection argument will be a
        // String array containing the actual ID of 3 in this case.
        //
        // For every "?" in the selection, we need to have an element in the selection
        // arguments that will fill in the "?". Since we have 1 question mark in the
        // selection, we have 1 String in the selection arguments' String array.
           // strings = new String[]{PetsContract.PetEntry._ID, PetsContract.PetEntry.COLUMN_PET_NAME};
            s =  PetsContract.PetEntry._ID + "=?";;
            strings1 = new String[]{String.valueOf(ContentUris.parseId(uri))};

            // This will perform a query on the pets table where the _id equals 3 to return a
        // Cursor containing that row of the table.
        cursor = sqLiteDatabase.query(PetsContract.PetEntry.TABLE_NAME, strings, s, strings1,
                null, null, null);
        break;
        default:
        throw new IllegalArgumentException("Cannot query unknown URI " + uri);
    }
    return cursor;


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatch.match(uri);
        switch (match) {
            case PETS:
                return PetsContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatch.match(uri);
        switch(match)
        {
            case PETS :  return insertPet(uri, contentValues);
            default:    throw new IllegalArgumentException("Insertion is not supported for " + uri);

        }

    }

    private Uri insertPet(Uri uri,ContentValues contentValues)
    {
        String name = contentValues.getAsString(PetsContract.PetEntry.COLUMN_PET_NAME);
        Integer weight = contentValues.getAsInteger(PetsContract.PetEntry.COLUMN_PET_WEIGHT);
        Integer gender = contentValues.getAsInteger(PetsContract.PetEntry.COLUMN_PET_GENDER);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }


        if(gender == null || !PetsContract.PetEntry.isValidGender(gender))
        {
            throw new IllegalArgumentException("Pet requires valid gender");

        }

        if(weight != null && weight < 0)
        {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        SQLiteDatabase database = petDbHelper.getWritableDatabase();
        
        long id =  database.insert(PetsContract.PetEntry.TABLE_NAME,null,contentValues);

        if(id == -1)
        {
            Toast.makeText(getContext(),"There is an Error",Toast.LENGTH_LONG).show();

        }else
        {
            Toast.makeText(getContext(),String.valueOf(id),Toast.LENGTH_LONG).show();
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int rowsDeleted;
        final int match = sUriMatch.match(uri);
        SQLiteDatabase sqLiteDatabase = petDbHelper.getWritableDatabase();
        switch(match)
        {
            case PETS:
                rowsDeleted = sqLiteDatabase.delete(PetsContract.PetEntry.TABLE_NAME,s, strings);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case PET_ID:
                s = PetsContract.PetEntry._ID + "=?";
                strings = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqLiteDatabase.delete(PetsContract.PetEntry.TABLE_NAME,s, strings);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }


    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        final int match = sUriMatch.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri,contentValues, s, strings);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                s = PetsContract.PetEntry._ID + "=?";
                strings = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri,
                        contentValues, s, strings);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
       
    }

    private int updatePet(Uri uri ,ContentValues values, String s, String[] strings) {
        if (values.containsKey(PetsContract.PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetsContract.PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetsContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetsContract.PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetsContract.PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }


        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = petDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(PetsContract.PetEntry.TABLE_NAME, values, s, strings);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }



}
