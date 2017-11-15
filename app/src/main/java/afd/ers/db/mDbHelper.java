package afd.ers.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class mDbHelper extends SQLiteOpenHelper {

    public mDbHelper(Context context) {
        super(context, ERSList.DB_NAME, null, ERSList.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableItems = " CREATE TABLE " + ERSList.ItemEntry.TABLE + " ( " +
                ERSList.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ERSList.ItemEntry.COL_cat + " TEXT NOT NULL," +
                ERSList.ItemEntry.COL_name + " TEXT NOT NULL," +
                ERSList.ItemEntry.COL_price + " FLOAT NOT NULL," +
                ERSList.ItemEntry.COL_stock + " INTEGER NOT NULL," +
                ERSList.ItemEntry.COL_picture + " TEXT NOT NULL );";

        String createTableRecords = " CREATE TABLE " + ERSList.RecordEntry.TABLE + " ( " +
                ERSList.RecordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ERSList.RecordEntry.COL_item_ID + " INTEGER NOT NULL,"+
                ERSList.RecordEntry.COL_price + " FLOAT NOT NULL," +
                ERSList.RecordEntry.COL_amount + " INT NOT NULL," +
                ERSList.RecordEntry.COL_stock_after_record + " INTEGER NOT NULL,"+
                ERSList.RecordEntry.COL_date + " DATE NOT NULL );";

        String createTableEmployees = " CREATE TABLE " + ERSList.EmployeeEntry.TABLE + " ( " +
                ERSList.EmployeeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ERSList.EmployeeEntry.COL_name + " TEXT NOT NULL," +
                ERSList.EmployeeEntry.COL_password + " TEXT NOT NULL);";

        String createTableClockIns = " CREATE TABLE " + ERSList.ClockInEntry.TABLE + " ( " +
                ERSList.ClockInEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ERSList.ClockInEntry.COL_name + " TEXT NOT NULL," +
                ERSList.ClockInEntry.COL_date + " DATE NOT NULL," +
                ERSList.ClockInEntry.COL_start_hour + " STRING NOT NULL," +
                ERSList.ClockInEntry.COL_end_hour + " STRING NOT NULL," +
                ERSList.ClockInEntry.COL_start_money + " FLOAT NOT NULL," +
                ERSList.ClockInEntry.COL_end_money + " FLOAT NOT NULL," +
                ERSList.ClockInEntry.COL_difference + " FLOAT NOT NULL);";

        db.execSQL(createTableItems);
        db.execSQL(createTableRecords);
        db.execSQL(createTableEmployees);
        db.execSQL(createTableClockIns);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ERSList.RecordEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ERSList.ItemEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ERSList.EmployeeEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ERSList.ClockInEntry.TABLE);
        onCreate(db);
    }
}