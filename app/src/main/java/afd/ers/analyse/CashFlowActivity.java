package afd.ers.analyse;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import afd.ers.MainActivity;
import afd.ers.R;
import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

import static afd.ers.R.id.expenses;

public class CashFlowActivity extends AppCompatActivity {
    private static final String TAG = "CashFlowActivity";
    private mDbHelper mHelper;

    private CashFlowAdapter mCashFlowAdapter;
    private ListView mListView;
    private ArrayList<CashFlowItem> objects;
    private  String monday;
    private String sunday;
    private String exactDay;
    private String cateogry;
    private String selectedCategory;
    private String period;
    Calendar calendarBuffer;
    Calendar calendarWork;
    static final String[] categories = {"Snacks", "Drinks", "Hygienic", "Fridge", "Talk Time", "Other"};
    static final String[] categoriesAllX = {"All","Snacks", "Drinks", "Hygienic", "Fridge", "Talk Time", "Other"};
    static final String[] categoriesAll = {"All","Snacks", "Drinks", "Hygienic", "Vegetables", "Talk Time", "Other","Restaurant"};
    static final String[] periods = {"Day", "Week", "Month", "Year"};
    CashFlowActivity test = this;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_flow_analyse);

        setTitle("Cashflow");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

        mListView = (ListView) findViewById(R.id.cash_flow_items);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        exactDay = sdf.format(c.getTime());
        if(calendarBuffer == null){
            calendarBuffer = c;
            exactDay = sdf.format(c.getTime());
        }
        if(selectedCategory == null){
            selectedCategory = " 1 OR 1 = 1 ";
//            selectedCategory = "'Snacks' OR " + "I." + ERSList.ItemEntry.COL_cat
//                    + " = 'Drinks' OR " + "I." + ERSList.ItemEntry.COL_cat
//                    + " = 'Hygienic' OR " + "I." + ERSList.ItemEntry.COL_cat
//                    + " = 'Fridge' OR " + "I." + ERSList.ItemEntry.COL_cat
//                    + " = 'Talk Time' OR " + "I." + ERSList.ItemEntry.COL_cat
//                    + " = 'Other')";
        }



        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        monday = sdf.format(c.getTime());
        c.add(Calendar.DATE, 6);
        sunday = sdf.format(c.getTime());



        mHelper = new mDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        updateMainData(db,monday,sunday);
        updateList(db,monday,sunday);
        db.close();

        categorySpinner();
        periodsSpinner();

        verifyStoragePermissions(this);
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void updateList(SQLiteDatabase db,String monday, String sunday) {
//        try {
//        createCashFlowList(db, monday, sunday);
            mCashFlowAdapter = new CashFlowAdapter(this, createCashFlowList(db, monday, sunday));
            mListView.setAdapter(mCashFlowAdapter);
//        }catch (Exception e){}
    }
    public ArrayList<CashFlowItem> createCashFlowList(SQLiteDatabase db,String monday , String sunday){

        ArrayList<CashFlowItem> cashFlowList = new ArrayList<>();
        Cursor cursor = db.rawQuery(getQueryCashFlowItem(monday,sunday),new String[]{});
        String cat = "";
        while (cursor.moveToNext()) {
            String catItem  = cursor.getString(cursor.getColumnIndex(ERSList.ItemEntry.COL_cat));
            if (!cat.equals(catItem)){
                cat = catItem;
                cashFlowList.add(new CashFlowItem(cat, "" , ""));
            }
            String name  = cursor.getString(cursor.getColumnIndex(ERSList.ItemEntry.COL_name));
            String revenue  = String.valueOf(cursor.getInt(cursor.getColumnIndex("revenue")));
            String amount  = cursor.getString(cursor.getColumnIndex("amount"));
            cashFlowList.add(new CashFlowItem(name, amount,revenue));
        }
        cursor.close();
        Cursor c = db.rawQuery(getQueryCashFlowItemStock(monday,sunday),new String[]{});
        String catS = "";
        while (c.moveToNext()) {
            String catItem  = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_cat));
            if (!catS.equals(catItem)){
                catS = catItem ;
                cashFlowList.add(new CashFlowItem(catS + " Stock ", "" , ""));
            }
            String name  = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_name));
            String revenue  = String.valueOf(c.getInt(c.getColumnIndex("revenue")));
            String amount  = c.getString(c.getColumnIndex("amount"));
            cashFlowList.add(new CashFlowItem(name, amount,revenue));
        }
        c.close();
        db.close();
        return cashFlowList;
    }
    private String getQueryCashFlowRecordStock(String monday, String sunday) {
        String tableColumns = ERSList.RecordEntry.COL_item_ID+
                " , "+
                "SUM(" + ERSList.RecordEntry.COL_price +" * "+ERSList.RecordEntry.COL_amount + ")" +
                " AS revenue "+
                " , "+
                "SUM(" + ERSList.RecordEntry.COL_amount + ")" +
                " AS amount ";
        String whereClause =  " ( "+
                "julianday("+ERSList.RecordEntry.COL_date + ")" +
                " >= " +
                "julianday('"+monday +"')"+
                " )" +
                "AND "+
                " ( "+
                "julianday("+ERSList.RecordEntry.COL_date +")"+
                " <= " +
                "julianday('"+sunday +"')"+
                " ) " +
                "AND "+
                " ( "+
                ERSList.RecordEntry.COL_price + " < 0 " +
                " ) ";
        String from = ERSList.RecordEntry.TABLE;
        String groupBy = ERSList.RecordEntry.COL_item_ID;
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause + " GROUP BY " + groupBy ;
        return query;
    }
    private String getQueryCashFlowItemStock(String monday, String sunday) {
        String tableColumns = "I."+ERSList.ItemEntry.COL_name+
                " , "+
                "I."+ERSList.ItemEntry.COL_cat+
                " , "+
                " R.revenue "+
                " , "+
                " R.amount ";
        String whereClause ="R."+ERSList.RecordEntry.COL_item_ID +
                " = " +
                "I."+ERSList.ItemEntry._ID

                + " AND " + "(I."+ERSList.ItemEntry.COL_cat +
                " = " + selectedCategory +")";
        String from = ERSList.ItemEntry.TABLE + " AS I" +
                " , "+
                "("+getQueryCashFlowRecordStock(monday,sunday)+")"+" AS R ";

        String orderBy = "I."+ERSList.ItemEntry.COL_cat;
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause +
                " ORDER BY " + orderBy;
        return query;
    }
    private String getQueryCashFlowRecord(String monday, String sunday) {
        String tableColumns = ERSList.RecordEntry.COL_item_ID+
                " , "+
                "SUM(" + ERSList.RecordEntry.COL_price +" * "+ERSList.RecordEntry.COL_amount + ")" +
                " AS revenue "+
                " , "+
                "SUM(" + ERSList.RecordEntry.COL_amount + ")" +
                " AS amount ";
        String whereClause =  " ( "+
                "julianday("+ERSList.RecordEntry.COL_date + ")" +
                " >= " +
                "julianday('"+monday +"')"+
                " )" +
                "AND "+
                " ( "+
                "julianday("+ERSList.RecordEntry.COL_date +")"+
                " <= " +
                "julianday('"+sunday +"')"+
                " ) " +
                "AND "+
                " ( "+
                ERSList.RecordEntry.COL_price + " > 0 " +
                " ) ";
        String from = ERSList.RecordEntry.TABLE;
        String groupBy = ERSList.RecordEntry.COL_item_ID;
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause + " GROUP BY " + groupBy ;
        return query;
    }
    private String getQueryCashFlowItem(String monday, String sunday) {
        String tableColumns = "I."+ERSList.ItemEntry.COL_name+
                " , "+
                "I."+ERSList.ItemEntry.COL_cat+
                " , "+
                " R.revenue "+
                " , "+
                " R.amount ";
        String whereClause ="R."+ERSList.RecordEntry.COL_item_ID +
                " = " +
                "I."+ERSList.ItemEntry._ID
                + " AND " + "(I."+ERSList.ItemEntry.COL_cat +
                " = " + selectedCategory +" ) ";
        String from = ERSList.ItemEntry.TABLE + " AS I" +
                " , "+
                "("+getQueryCashFlowRecord(monday,sunday)+")"+" AS R ";

        String orderBy = "I."+ERSList.ItemEntry.COL_cat;
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause +
                " ORDER BY " + orderBy;
        return query;
    }

    public void updateMainData(SQLiteDatabase db,String monday , String sunday){
        TextView textDate = (TextView)findViewById(R.id.date);
        TextView textRevenue = (TextView)findViewById(R.id.revenue);
        TextView textDiscrepancy = (TextView)findViewById(R.id.discr);
        TextView textExpenses = (TextView)findViewById(expenses);
        TextView textNetto = (TextView)findViewById(R.id.netto);

        int revenue = getRevenue(db,monday,sunday);
        int discrepancy = getDiscrepancy(db, monday, sunday, revenue);
        int expenses = getExpenses(db,monday,sunday);
        int netto = revenue+expenses;

        if(monday.equals(sunday)){
            textDate.setText(monday);
        }else {
            textDate.setText(monday+" / "+sunday);
        }

        textRevenue.setText(""+revenue);
        textDiscrepancy.setText(""+discrepancy);
        textExpenses.setText(""+expenses);
        textNetto.setText(""+netto);
    }
    public int getRevenue(SQLiteDatabase db ,String monday, String sunday){
        Cursor cursor = db.rawQuery(getQueryRevenue(monday,sunday),new String[]{});
        int revenue = 0;
        while (cursor.moveToNext() ) {
            revenue  = cursor.getInt(cursor.getColumnIndex("revenue"));
        }
        cursor.close();
        return revenue;
    }
    private String getQueryRevenue(String monday, String sunday){
        String tableColumns ="SUM("+ERSList.RecordEntry.COL_price + " * " + ERSList.RecordEntry.COL_amount+") as revenue ";
        String whereClause =  " ( "+
                "julianday("+ERSList.RecordEntry.COL_date + ")" +
                " >= " +
                "julianday('"+monday +"')"+
                " )" +
                "AND "+
                " ( "+
                "julianday("+ERSList.RecordEntry.COL_date +")"+
                " <= " +
                "julianday('"+sunday +"')"+
                " ) " +
                "AND "+
                " ( "+
                ERSList.RecordEntry.COL_price + " > 0 " +
                " ) " +
                " AND " +
                "I."+ERSList.ItemEntry._ID+ " = " + ERSList.RecordEntry.COL_item_ID;
        String from = ERSList.RecordEntry.TABLE + " , " +"(" +getQueryItemsSelectedCatagory()+") AS I ";
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause;
        return query;
    }

    public int getDiscrepancy(SQLiteDatabase db ,String monday, String sunday, int revenue) {
        int discrepancy,
            start_money = 0,
            end_money   = 0,
            admin_costs = 0;
        Cursor cursor = db.rawQuery(getQueryStartMoney(monday, sunday), new String[] {});
        if (cursor.moveToNext() ) {
            start_money  = cursor.getInt(cursor.getColumnIndex("start_money"));
        }
        cursor.close();
        cursor = db.rawQuery(getQueryEndMoney(monday, sunday), new String[] {});

        if (cursor.moveToNext() ) {
            end_money  = cursor.getInt(cursor.getColumnIndex("end_money"));
        }
        cursor.close();
        cursor = db.rawQuery(getQueryAdminCosts(monday, sunday),new String[]{});
        if (cursor.moveToNext() ) {
            admin_costs  = cursor.getInt(cursor.getColumnIndex("admincosts"));
        }
        cursor.close();

        discrepancy = end_money-start_money - admin_costs;

        return discrepancy-revenue;
    }

    private String getQueryStartMoney(String monday, String sunday){
        String tableColumns =ERSList.ClockInEntry.COL_start_money +" as start_money ";
        String whereClause = " ( "+
                "julianday("+ERSList.ClockInEntry.COL_date + ")" +
                " >= " +
                "julianday('"+monday +"')"+
                " )" +
                "AND "+
                " ( "+
                "julianday("+ERSList.ClockInEntry.COL_date +")"+
                " <= " +
                "julianday('"+sunday +"')"+
                " ) ";
        String from = ERSList.ClockInEntry.TABLE ;
        String order_by = ERSList.ClockInEntry._ID+ " ASC ";
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause + " ORDER BY " + order_by;
        return query;
    }

    private String getQueryEndMoney(String monday, String sunday){
        String tableColumns =ERSList.ClockInEntry.COL_end_money +" as end_money ";
        String whereClause = " ( "+
                "julianday("+ERSList.ClockInEntry.COL_date + ")" +
                " >= " +
                "julianday('"+monday +"')"+
                " )" +
                "AND "+
                " ( "+
                "julianday("+ERSList.ClockInEntry.COL_date +")"+
                " <= " +
                "julianday('"+sunday +"')"+
                " ) ";
        String from = ERSList.ClockInEntry.TABLE ;
        String order_by = ERSList.ClockInEntry._ID+ " DESC ";
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause + " ORDER BY " + order_by;
        return query;
    }

    private String getQueryAdminCosts(String monday, String sunday){
        String tableColumns ="SUM("+ERSList.ClockInEntry.COL_difference +") as admincosts ";
        String whereClause = " ( "+
                "julianday("+ERSList.ClockInEntry.COL_date + ")" +
                " >= " +
                "julianday('"+monday +"')"+
                " )" +
                "AND "+
                " ( "+
                "julianday("+ERSList.ClockInEntry.COL_date +")"+
                " <= " +
                "julianday('"+sunday +"')"+
                " ) " +
                "AND "+
                ERSList.ClockInEntry.COL_name + " = 'Admin' ";
        String from = ERSList.ClockInEntry.TABLE ;
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause;
        return query;
    }

    public int getExpenses(SQLiteDatabase db ,String monday, String sunday){
        Cursor cursor = db.rawQuery(getQueryExpenses(monday,sunday),new String[]{});
        int expenses = 0;
        while (cursor.moveToNext() ) {
            expenses  = cursor.getInt(cursor.getColumnIndex("expenses"));
        }
        cursor.close();
        return expenses;
    }
    private String getQueryExpenses(String monday, String sunday){
        String tableColumns ="SUM("+ERSList.RecordEntry.COL_price + " * " + ERSList.RecordEntry.COL_amount +") as expenses ";
        String whereClause = " ( "+
                "julianday("+ERSList.RecordEntry.COL_date + ")" +
                " >= " +
                "julianday('"+monday +"')"+
                " )" +
                "AND "+
                " ( "+
                "julianday("+ERSList.RecordEntry.COL_date +")"+
                " <= " +
                "julianday('"+sunday +"')"+
                " ) " +
                "AND "+
                " ( "+
                ERSList.RecordEntry.COL_price + " <= 0 " +
                " ) "+
                " AND " +
                "I."+ERSList.ItemEntry._ID+ " = " + ERSList.RecordEntry.COL_item_ID;
        String from = ERSList.RecordEntry.TABLE+ " , " +"(" +getQueryItemsSelectedCatagory()+") AS I ";
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause;
        return query;
    }
    public String getQueryItemsSelectedCatagory(){
        String tableColumns = ERSList.ItemEntry._ID;
        String whereClause =
                "("+ERSList.ItemEntry.COL_cat +
                " = " + selectedCategory +" ) ";
        String from = ERSList.ItemEntry.TABLE;
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause ;
        return query;
    }
//    public String getTransactions(SQLiteDatabase db ,String monday, String sunday){
////        SQLiteDatabase db = mHelper.getReadableDatabase();
//        Cursor cursor = db.rawQuery(getQueryAmountTransactions(monday,sunday),new String[]{});
//        int amount = 0;
//        String s = "";
//        String d="";
//        while (cursor.moveToNext() ) {
//            amount  = cursor.getInt(cursor.getColumnIndex("amount"));
//            s  = cursor.getString(cursor.getColumnIndex("sunday"));
//            d  = cursor.getString(cursor.getColumnIndex("day"));
//        }
//        cursor.close();
////        db.close();
//        return amount+" /" +s + "/"+d;
//    }
//    private String getQueryAmountTransactions(String monday, String sunday){
//        String tableColumns =ERSList.RecordEntry.COL_amount+" as amount" +
//                " , "+
//                ERSList.RecordEntry.COL_date + " as date"+
//                " , "+
//                "julianday("+ERSList.RecordEntry.COL_date + ") as day "+
//                " , "+
//                "julianday('"+sunday + "') as sunday "+
//                " , "+
//                "DATE('now','localtime','weekday 0','-6 days') as monday ";
//        String whereClause =  " ( "+
//                "julianday("+ERSList.RecordEntry.COL_date + ") >= "+ "julianday("+ monday +
//                " ))";
////                +
////                "AND "+
////                " ( "+
////                "julianday("+ERSList.RecordEntry.COL_date +")"
////                +
////                " <= " +
////                "julianday("+sunday +")"+
////                ")";
//        String from = ERSList.RecordEntry.TABLE;
//        String query = " SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause;
//        return query;
//    }

    // Override the back button to do nothing
    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.record_list_go_back:
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return true;
            case R.id.action_admin:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivityForResult(mainIntent,1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void choseDateCashflow(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.cashflow_date_popup, null);
        final DatePicker picker = (DatePicker) textEntryView.findViewById(R.id.cashflowDatePicker);
        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.cashflow_date_popup_confirm);
        ImageButton cancel = (ImageButton) textEntryView.findViewById(R.id.cashflow_date_popup_cancel);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(textEntryView)
                .setCancelable(false)
                .create();
        dialog.show();
        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                Calendar c = Calendar.getInstance();

                c.set(picker.getYear(),picker.getMonth(),picker.getDayOfMonth());
                exactDay = sdf.format(c.getTime());
                calendarBuffer = c;


                if(period.equals(periods[0])){
                    monday = sdf.format(c.getTime());
                    sunday = sdf.format(c.getTime());
                }
                if(period.equals(periods[1])){
                    while(c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
                        c.add(Calendar.DATE,-1);
                    }

                    monday = sdf.format(c.getTime());
                    c.add(Calendar.DATE, 6);
                    sunday = sdf.format(c.getTime());
                }
                if(period.equals(periods[2])){

                    String month = exactDay.substring(5,7);
                    String monthyear = exactDay.substring(0,7);
                    if(month.equals("01") || month.equals("03") || month.equals("05") || month.equals("07") || month.equals("08") || month.equals("10") || month.equals("12")){

                        monday = monthyear + "-01";
                        sunday = monthyear + "-31";
                    }
                    if(month.equals("04") || month.equals("06") || month.equals("09") || month.equals("11")){
                        monday = monthyear + "-01";
                        sunday = monthyear + "-30";
                    }
                    if(month.equals("02")){
                        String year = exactDay.substring(0,4);
                        int yearint = Integer.valueOf(year);
                        if(yearint%4 == 0){
                            monday = monthyear + "-01";
                            sunday = monthyear + "-29";
                        }else {
                            monday = monthyear + "-01";
                            sunday = monthyear + "-28";
                        }

                    }
                }
                if(period.equals(periods[3])){
                    String year = exactDay.substring(0,4);
                    monday = year + "-01-01";
                    sunday = year + "-12-31";

                }
                /*
                while(c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
                    c.add(Calendar.DATE,-1);
                }

                monday = sdf.format(c.getTime());
                c.add(Calendar.DATE, 6);
                sunday = sdf.format(c.getTime());
                 */

                mHelper = new mDbHelper(test);
                SQLiteDatabase db = mHelper.getReadableDatabase();
                updateMainData(db,monday,sunday);
                updateList(db,monday,sunday);
                db.close();

                dialog.cancel();

            }
        });
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                dialog.cancel();

            }
        });
    }
    public void setSundayMonday(){

    }
    public void makeCashFlowCSV() {

        String fileName = "Cashflow/cashflow_kiosk_" + monday + "_" + sunday + ".csv";
        if(monday.equals(sunday)){
            fileName = "Cashflow/cashflow_kiosk_" + monday + ".csv";
        }
        File outDbFile = new File(Environment.getExternalStorageDirectory() + fileName);
        if (outDbFile.exists()) {
            outDbFile.delete();
            Toast.makeText(this, "Cashflow File Updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cashflow File Created", Toast.LENGTH_SHORT).show();
        }


        mDbHelper dbhelper = new mDbHelper(getApplicationContext());
        SQLiteDatabase db = dbhelper.getReadableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }


        int revenue = getRevenue(db, monday, sunday);
        int expenses = getExpenses(db, monday, sunday);
        int netto = revenue + expenses;

        String date = monday + " / " + sunday;
        if(monday.equals(sunday)){
            date = monday;
        }
        String revenueX = "" + revenue;
        String expensesX = "" + expenses;
        String nettoX = "" + netto;




        ArrayList<CashFlowItem> cashFlowItems = createCashFlowList(db, monday, sunday);

        db.close();

        File file = new File(exportDir, fileName);

        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));




            String arrStrDate[] = {"Date:", date};
            String arrStrRevenue[] = {"Revenue:", revenueX};
            String arrStrExpenses[] = {"Expenses:", expensesX};
            String arrStrNetto[] = {"Netto:", nettoX};
            String arrStrEmpty[] = {""};
            String arrStrInfo[] = {"Name","Amount","Revenue"};


            csvWrite.writeNext(arrStrDate,false);
            csvWrite.writeNext(arrStrRevenue,false);
            csvWrite.writeNext(arrStrExpenses,false);
            csvWrite.writeNext(arrStrNetto,false);
            csvWrite.writeNext(arrStrEmpty,false);
            csvWrite.writeNext(arrStrInfo,false);


            for(int i = 0; i < cashFlowItems.size();i++){
                String arrStr[] = {cashFlowItems.get(i).getName(),cashFlowItems.get(i).getAmount(),cashFlowItems.get(i).getRevenue()};
                csvWrite.writeNext(arrStr,false);

            }


            csvWrite.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }


    }
    public void printPdf() {

        PrintAttributes.Builder printAttributes = new PrintAttributes.Builder();
        printAttributes.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
        printAttributes.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        printAttributes.setMinMargins(new PrintAttributes.Margins(10, 10, 10, 10));
        PrintAttributes attributesBuild = printAttributes.build();
        PrintedPdfDocument document = new PrintedPdfDocument(this.getApplicationContext(), attributesBuild);


        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        Paint paintBIG = new Paint();
        paintBIG.setColor(Color.BLACK);
        paintBIG.setFakeBoldText(true);

        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);


        Paint paintCategory = new Paint();
        paintCategory.setColor(Color.BLACK);
        paintCategory.setTypeface(font);


        mHelper = new mDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();

        int revenue = getRevenue(db, monday, sunday);
        int discrepancy = getDiscrepancy(db, monday, sunday, revenue);
        int expenses = getExpenses(db, monday, sunday);


        ArrayList<CashFlowItem> cashFlowItems = createCashFlowList(db, monday, sunday);

        String fileName = "/Cashflow/cashflow_kiosk_" + monday + "_" + sunday + ".pdf";
        if(monday.equals(sunday)){
            fileName = "/Cashflow/cashflow_kiosk_" + monday + ".pdf";
        }else {
            fileName = "/Cashflow/cashflow_kiosk_" + monday + "_" + sunday + ".pdf";
        }



        int netto = revenue + expenses;

        db.close();

        String date = monday + " / " + sunday;
        if (monday.equals(sunday)){
            date = monday;
        }
        String revenueX = "" + revenue;
        String discrepancyX = "" + discrepancy;
        String expensesX = "" + expenses;
        String nettoX = "" + netto;
        String category ="" + selectedCategory;


        int itemsLeftToPrint = cashFlowItems.size();


        canvas.drawText("Date: ", 50f, 50f, paintBIG);
        canvas.drawText(date, 110f, 50f, paint);
        canvas.drawText("Category: ", 50f, 65f, paintBIG);
        canvas.drawText(category, 110f, 65f, paint);

        canvas.drawText("Revenue: ", 320f, 50f, paint);
        canvas.drawText(revenueX, 480f, 50f, paint);
        canvas.drawText("Discrepancy: ", 320f, 65f, paint);
        canvas.drawText(discrepancyX, 480f, 65f, paint);
        canvas.drawText("Expenses: ", 320f, 80f, paint);
        canvas.drawText(expensesX, 480f, 80f, paint);
        canvas.drawText("Netto: ", 320f, 95f, paintBIG);
        canvas.drawText(nettoX, 480f, 95f, paint);
        canvas.drawLine(310f, 80f + 4f, 550f, 80f + 4f, paint);

        canvas.drawText("Amount", 180f, 140f, paint);
        canvas.drawText("Revenue", 245f, 140f, paint);


        float height = 150f;
        for (int i = 0; i < cashFlowItems.size(); i++) {
            if (height < 810) {
                if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                    height = height + 15f;
                    canvas.drawText(cashFlowItems.get(i).getName(), 50f, height, paintCategory);
                }

                //canvas.drawLine(40f,height+4f,280f,height+4f,paint);
                if (itemsLeftToPrint > 1) {
                    if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && !(cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                        canvas.drawText(cashFlowItems.get(i).getName(), 50f, height, paint);
                        canvas.drawText(cashFlowItems.get(i).getAmount(), 200f, height, paint);
                        canvas.drawText(cashFlowItems.get(i).getRevenue(), 250f, height, paint);


                        canvas.drawLine(40f, height + 4f, 280f, height + 4f, paint);

                    }
                    if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && (cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                        canvas.drawText(cashFlowItems.get(i).getName(), 50f, height, paint);
                        canvas.drawText(cashFlowItems.get(i).getAmount(), 200f, height, paint);
                        canvas.drawText(cashFlowItems.get(i).getRevenue(), 250f, height, paint);
                    }
                }
                if (itemsLeftToPrint == 1 && !(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals(""))) {
                    canvas.drawText(cashFlowItems.get(i).getName(), 50f, height, paint);
                    canvas.drawText(cashFlowItems.get(i).getAmount(), 200f, height, paint);
                    canvas.drawText(cashFlowItems.get(i).getRevenue(), 250f, height, paint);
                }


                if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                    height = height + 15f;
                }

                height = height + 15f;
                itemsLeftToPrint--;


            }

        }
        height = 150f;
        if (itemsLeftToPrint > 0) {
            for (int i = cashFlowItems.size() - itemsLeftToPrint; i < cashFlowItems.size(); i++) {
                if (height < 810) {
                    if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                        height = height + 15f;
                        canvas.drawText(cashFlowItems.get(i).getName(), 320f, height, paintCategory);
                    }

                    //canvas.drawLine(310f,height+4f,550f,height+4f,paint);
                    if (itemsLeftToPrint > 1) {
                        if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && !(cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                            canvas.drawText(cashFlowItems.get(i).getName(), 320f, height, paint);
                            canvas.drawText(cashFlowItems.get(i).getAmount(), 470f, height, paint);
                            canvas.drawText(cashFlowItems.get(i).getRevenue(), 520f, height, paint);


                            canvas.drawLine(310f, height + 4f, 550f, height + 4f, paint);
                        }
                        if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && (cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                            canvas.drawText(cashFlowItems.get(i).getName(), 320f, height, paint);
                            canvas.drawText(cashFlowItems.get(i).getAmount(), 470f, height, paint);
                            canvas.drawText(cashFlowItems.get(i).getRevenue(), 520f, height, paint);
                        }


                    }
                    if (itemsLeftToPrint == 1 && !(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals(""))) {
                        canvas.drawText(cashFlowItems.get(i).getName(), 320f, height, paint);
                        canvas.drawText(cashFlowItems.get(i).getAmount(), 470f, height, paint);
                        canvas.drawText(cashFlowItems.get(i).getRevenue(), 520f, height, paint);
                    }


                    if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                        height = height + 15f;
                    }

                    height = height + 15f;
                    itemsLeftToPrint--;


                }

            }
        }
        document.finishPage(page);

        int pageNumber = 1;

        while(itemsLeftToPrint > 0){
            pageNumber++;
            PdfDocument.PageInfo pageInfoAfterOne =
                    new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
            PdfDocument.Page pageAfterOne = document.startPage(pageInfoAfterOne);



            Canvas canvasAfterOne = pageAfterOne.getCanvas();
            /*
            while(height < 810 && itemsLeftToPrint > 0){

                canvasAfterOne.drawText(cashFlowItems.get(position).getName(), 50f, height, paint);
                height = height + 15f;
                itemsLeftToPrint--;
                position++;
            }
            */

            /*
            for (int i = (cashFlowItems.size()-itemsLeftToPrint); i < cashFlowItems.size(); i++){
                if(height < 810) {
                    canvas.drawText(cashFlowItems.get(i).getName(), 50f, height, paint);
                    height = height + 15f;
                    itemsLeftToPrint--;
                }

            }
            */


            height = 50f;
            for (int i = (cashFlowItems.size()-itemsLeftToPrint); i < cashFlowItems.size(); i++) {
                if (height < 810) {
                    if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                        height = height + 15f;
                        canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 50f, height, paintCategory);
                    }

                    //canvas.drawLine(40f,height+4f,280f,height+4f,paint);
                    if (itemsLeftToPrint > 1) {
                        if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && !(cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                            canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 50f, height, paint);
                            canvasAfterOne.drawText(cashFlowItems.get(i).getAmount(), 200f, height, paint);
                            canvasAfterOne.drawText(cashFlowItems.get(i).getRevenue(), 250f, height, paint);


                            canvasAfterOne.drawLine(40f, height + 4f, 280f, height + 4f, paint);

                        }
                        if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && (cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                            canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 50f, height, paint);
                            canvasAfterOne.drawText(cashFlowItems.get(i).getAmount(), 200f, height, paint);
                            canvasAfterOne.drawText(cashFlowItems.get(i).getRevenue(), 250f, height, paint);
                        }
                    }
                    if (itemsLeftToPrint == 1 && !(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals(""))) {
                        canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 50f, height, paint);
                        canvasAfterOne.drawText(cashFlowItems.get(i).getAmount(), 200f, height, paint);
                        canvasAfterOne.drawText(cashFlowItems.get(i).getRevenue(), 250f, height, paint);
                    }


                    if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                        height = height + 15f;
                    }

                    height = height + 15f;
                    itemsLeftToPrint--;


                }

            }
            height = 50f;
            if (itemsLeftToPrint > 0) {
                for (int i = cashFlowItems.size() - itemsLeftToPrint; i < cashFlowItems.size(); i++) {
                    if (height < 810) {
                        if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                            height = height + 15f;
                            canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 320f, height, paintCategory);
                        }

                        //canvas.drawLine(310f,height+4f,550f,height+4f,paint);
                        if (itemsLeftToPrint > 1) {
                            if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && !(cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                                canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 320f, height, paint);
                                canvasAfterOne.drawText(cashFlowItems.get(i).getAmount(), 470f, height, paint);
                                canvasAfterOne.drawText(cashFlowItems.get(i).getRevenue(), 520f, height, paint);


                                canvasAfterOne.drawLine(310f, height + 4f, 550f, height + 4f, paint);
                            }
                            if (!(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) && (cashFlowItems.get(i + 1).getAmount().equals("") && cashFlowItems.get(i + 1).getRevenue().equals(""))) {
                                canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 320f, height, paint);
                                canvasAfterOne.drawText(cashFlowItems.get(i).getAmount(), 470f, height, paint);
                                canvasAfterOne.drawText(cashFlowItems.get(i).getRevenue(), 520f, height, paint);
                            }


                        }
                        if (itemsLeftToPrint == 1 && !(cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals(""))) {
                            canvasAfterOne.drawText(cashFlowItems.get(i).getName(), 320f, height, paint);
                            canvasAfterOne.drawText(cashFlowItems.get(i).getAmount(), 470f, height, paint);
                            canvasAfterOne.drawText(cashFlowItems.get(i).getRevenue(), 520f, height, paint);
                        }


                        if (cashFlowItems.get(i).getAmount().equals("") && cashFlowItems.get(i).getRevenue().equals("")) {
                            height = height + 15f;
                        }

                        height = height + 15f;
                        itemsLeftToPrint--;


                    }

                }
            }

            document.finishPage(pageAfterOne);


        }




        // write the document content
        String targetPdf = Environment.getExternalStorageDirectory() + fileName;
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(),
                    Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();


    }
    public void makePdfCsvCashflow(View view){
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Cashflow");

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Toast.makeText(getApplicationContext(), "Cannot make a folder in external storage!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        makeCashFlowCSV();
        printPdf();
    }
    public void categorySpinner(){
        Spinner menuSpinner = (Spinner) findViewById(R.id.cashflowCategorySpinner);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,categoriesAll );
        menuSpinner.setAdapter(adapter);
        menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        cateogry = categoriesAll[0];
                        selectedCategory = " 1 OR 1 = 1 ";
//                        selectedCategory = "'Snacks' OR " + "I." + ERSList.ItemEntry.COL_cat
//                            + " = 'Drinks' OR " + "I." + ERSList.ItemEntry.COL_cat
//                            + " = 'Hygienic' OR " + "I." + ERSList.ItemEntry.COL_cat
//                            + " = 'Fridge' OR " + "I." + ERSList.ItemEntry.COL_cat
//                            + " = 'Talk Time' OR " + "I." + ERSList.ItemEntry.COL_cat
//                            + " = 'Other')";
                        final SQLiteDatabase db = mHelper.getReadableDatabase();
                        updateMainData(db,monday,sunday);
                        updateList(db,monday,sunday);
                        db.close();

                        break;
                    case 1:
                        cateogry = categoriesAll[1];
                        selectedCategory = "'" + categoriesAll[1] + "'";
                        final SQLiteDatabase db2 = mHelper.getReadableDatabase();
                        updateMainData(db2,monday,sunday);
                        updateList(db2,monday,sunday);
                        db2.close();

                        break;
                    case 2:
                        cateogry = categoriesAll[2];
                        selectedCategory = "'" + categoriesAll[2] + "'";
                        final SQLiteDatabase db3 = mHelper.getReadableDatabase();
                        updateMainData(db3,monday,sunday);
                        updateList(db3,monday,sunday);
                        db3.close();
                        break;
                    case 3:
                        cateogry = categoriesAll[3];
                        selectedCategory = "'" + categoriesAll[3] + "'";
                        final SQLiteDatabase db4 = mHelper.getReadableDatabase();
                        updateMainData(db4,monday,sunday);
                        updateList(db4,monday,sunday);
                        db4.close();
                        break;
                    case 4:
                        cateogry = categoriesAll[4];
                        selectedCategory = "'" + categoriesAll[4] + "'";
                        final SQLiteDatabase db5 = mHelper.getReadableDatabase();
                        updateMainData(db5,monday,sunday);
                        updateList(db5,monday,sunday);
                        db5.close();
                        break;
                    case 5:
                        cateogry = categoriesAll[5];
                        selectedCategory = "'" + categoriesAll[5] + "'";
                        final SQLiteDatabase db6 = mHelper.getReadableDatabase();
                        updateMainData(db6,monday,sunday);
                        updateList(db6,monday,sunday);
                        db6.close();
                        break;
                    case 6:
                        cateogry = categoriesAll[6];
                        selectedCategory = "'" + categoriesAll[6] + "'";
                        final SQLiteDatabase db7 = mHelper.getReadableDatabase();
                        updateMainData(db7,monday,sunday);
                        updateList(db7,monday,sunday);
                        db7.close();
                        break;
                    case 7:
                        cateogry = categoriesAll[7];
                        selectedCategory = "'" + categoriesAll[7] + "'";
                        final SQLiteDatabase db8 = mHelper.getReadableDatabase();
                        updateMainData(db8,monday,sunday);
                        updateList(db8,monday,sunday);
                        db8.close();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    public void periodsSpinner(){
        Spinner menuSpinner = (Spinner) findViewById(R.id.cashflowPeriodSpinner);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,periods );
        menuSpinner.setAdapter(adapter);
        menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        period = periods[0];

                        monday = exactDay;
                        sunday = exactDay;

                        final SQLiteDatabase db = mHelper.getReadableDatabase();
                        updateMainData(db,monday,sunday);
                        updateList(db,monday,sunday);
                        db.close();

                        break;
                    case 1:
                        period = periods[1];
                        calendarWork = calendarBuffer;
                        while(calendarWork.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
                            calendarWork.add(Calendar.DATE,-1);
                        }

                        monday = sdf.format(calendarWork.getTime());
                        calendarWork.add(Calendar.DATE, 6);
                        sunday = sdf.format(calendarWork.getTime());

                        final SQLiteDatabase db2 = mHelper.getReadableDatabase();
                        updateMainData(db2,monday,sunday);

                        updateList(db2,monday,sunday);
                        db2.close();

                        break;
                    case 2:
                        period = periods[2];
                        String month = exactDay.substring(5,7);
                        String monthyear = exactDay.substring(0,7);

                        if(month.equals("01") || month.equals("03") || month.equals("05") || month.equals("07") || month.equals("08") || month.equals("10") || month.equals("12")){

                            monday = monthyear + "-01";
                            sunday = monthyear + "-31";
                        }
                        if(month.equals("04") || month.equals("06") || month.equals("09") || month.equals("11")){
                            monday = monthyear + "-01";
                            sunday = monthyear + "-30";
                        }
                        if(month.equals("02")){
                            String year = exactDay.substring(0,4);
                            int yearint = Integer.valueOf(year);
                            if(yearint%4 == 0){
                                monday = monthyear + "-01";
                                sunday = monthyear + "-29";
                            }else {
                                monday = monthyear + "-01";
                                sunday = monthyear + "-28";
                            }

                        }




                        final SQLiteDatabase db3 = mHelper.getReadableDatabase();
                        updateMainData(db3,monday,sunday);

                        updateList(db3,monday,sunday);
                        db3.close();
                        break;
                    case 3:
                        period = periods[3];
                        String year = exactDay.substring(0,4);
                        monday = year + "-01-01";
                        sunday = year + "-12-31";
                        final SQLiteDatabase db4 = mHelper.getReadableDatabase();
                        updateMainData(db4,monday,sunday);

                        updateList(db4,monday,sunday);
                        db4.close();
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /*
    if(period.equals(periods[0])){
        monday = sdf.format(c.getTime());
        sunday = sdf.format(c.getTime());
    }
                if(period.equals(periods[1])){
        while(c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
            c.add(Calendar.DATE,-1);
        }

        monday = sdf.format(c.getTime());
        c.add(Calendar.DATE, 6);
        sunday = sdf.format(c.getTime());
    }
                if(period.equals(periods[2])){

    }
                if(period.equals(periods[3])){

    }
    */

}