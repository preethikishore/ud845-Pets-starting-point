package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PetsContract {

    private void PetContract() {}


    public static final String CONTENT_AUTHORITY ="com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = "petData";


    public  static abstract class PetEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);
        public static final String TABLE_NAME = "petData";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender" ;
        public static final String COLUMN_PET_WEIGHT = "weight";
        public static final Integer GENDER_MALE = 1;
        public static final Integer GENDER_FEMALE = 2;
        public static final Integer GENDER_UNKNOWN = 0;
        public static boolean isValidGender(int gender) {
            if (gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE) {
                return true;
            }
            return false;
        }
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;


        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;


    }


//    public  static abstract class PetSample implements BaseColumns
//    {
//        public static final String SAMPLE_TABLE_NAME = "petDetails";
//        public static final String SAMPLE_ID = BaseColumns._ID;
//        public static final String COLUMN_SAMPLE_NAME = "name";
//        public static final String COLUMN_SAMPLE_WEIGHT = "weight";
//
//    }


}