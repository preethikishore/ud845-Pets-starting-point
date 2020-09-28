package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PetDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shelter.db";
    private static final int DATABASE_VERSION = 1;


    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetsContract.PetEntry.TABLE_NAME + " ("
                + PetsContract.PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetsContract.PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetsContract.PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetsContract.PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetsContract.PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
//        String SQL_CREATE_Sample = "CREATE TABLE " + PetsContract.PetSample.SAMPLE_TABLE_NAME + " ("
//                + PetsContract.PetSample.SAMPLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + PetsContract.PetSample.COLUMN_SAMPLE_NAME + " TEXT NOT NULL, "
//                + PetsContract.PetSample.COLUMN_SAMPLE_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
//        sqLiteDatabase.execSQL(SQL_CREATE_Sample);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {


//        onCreate(sqLiteDatabase);
    }
}
