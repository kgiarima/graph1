package com.example.graphtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME="data_table";
    private static final String col0="ID";
    private static final String col1="date";
    private static final String col2="user";
    private static final String col3="avg_gsr";
    private static final String col4="avg_gsr_binaural";
    private static final String col5="avg_skt";
    private static final String col6="avg_skt_binaural";
    private static final String col7="avg_hr";
    private static final String col8="avg_hr_binaural";
    private static final String col9="avg_hrv";
    private static final String col10="avg_hrv_binaural";
    private static final String col11="avg_anxiety";
    private static final String col12="avg_anxiety_binaural";


    public DatabaseHelper(Context context){
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
