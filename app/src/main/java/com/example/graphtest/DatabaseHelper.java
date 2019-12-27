package com.example.graphtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME="data_table";
    private static final String COL0="ID";
    private static final String COL1="date";
    private static final String COL2="user";
    private static final String COL3="avg_gsr";
    private static final String COL4="avg_gsr_binaural";
    private static final String COL5="avg_skt";
    private static final String COL6="avg_skt_binaural";
    private static final String COL7="avg_hr";
    private static final String COL8="avg_hr_binaural";
    private static final String COL9="avg_hrv";
    private static final String COL10="avg_hrv_binaural";
    private static final String COL11="avg_anxiety";
    private static final String COL12="avg_anxiety_binaural";


    public DatabaseHelper(Context context){
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+TABLE_NAME+" (ID INTEGER PRIMARY KEY AUTOINCREMENT, "+COL1+" DATE, "+COL2+" TEXT, "+COL3+ "NUMBER, "+COL4+ "NUMBER, "+COL5+ "NUMBER, "+COL6+ "NUMBER, "+COL7+ "NUMBER, "+COL8+ "NUMBER, "+COL9+ "NUMBER, "+COL10+ "NUMBER, "+COL11+ "NUMBER, "+COL12+ "NUMBER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String item){


        return true;
    }
}
