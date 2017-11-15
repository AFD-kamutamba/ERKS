package afd.ers.analyse;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import afd.ers.MainActivity;
import afd.ers.R;
import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

public class StockAnalyseActivity extends AppCompatActivity {
    private static final String TAG = "StockAnalyseActivity";
    private mDbHelper mHelper;

    private StockAnalyseAdapter mStockAnalyseAdapter;
    private ListView mListView;
    private String selected_category;
    private int menuCase = 1;
    Button stockInDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_analyse);

        setTitle("Shopping List");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

        mHelper = new mDbHelper(this);
        mListView = (ListView) findViewById(R.id.stock_analyse_items);

        Button button = (Button) findViewById(R.id.select_snacks);

        button.setSelected(true);

        selected_category = "Snacks";

        stockInDays = (Button)findViewById(R.id.set_stock_in_days);

        stockInDays.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                }
                return false;
            }
        });

        updateUI();
    }

    public void setDays(View view) {
        LayoutInflater factory = LayoutInflater.from(this);

        final View daysView = factory.inflate(R.layout.stock_analyse_days, null);

        ImageButton confirm = (ImageButton) daysView.findViewById(R.id.days_popup_confirm);
        ImageButton cancel = (ImageButton) daysView.findViewById(R.id.days_popup_cancel);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(daysView)
                .setCancelable(false)
                .create();

        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.cancel();

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView given_days = (TextView) daysView.findViewById(R.id.shopping_days);

                if (!given_days.getText().toString().equals("")) {
                    stockInDays.setText(given_days.getText().toString());
                }

                dialog.cancel();
                updateUI();

            }
        });
    }

    public void resetButton() {
        Button button = null;

        switch (selected_category) {
            case "Snacks": button = (Button)findViewById(R.id.select_snacks);
                break;
            case "Drinks": button = (Button)findViewById(R.id.select_drinks);
                break;
            case "Hygienic": button = (Button)findViewById(R.id.select_hygienic);
                break;
            case "Vegetables": button = (Button)findViewById(R.id.select_vegetables);
                break;
            case "Talk Time": button = (Button)findViewById(R.id.select_talk_time);
                break;
            case "Other": button = (Button)findViewById(R.id.select_other);
                break;
            case "Restaurant": button = (Button)findViewById(R.id.select_restaurant);
                break;
        }
        button.setSelected(false);
    }

    public void selectSnacks(View view) {
        resetButton();

        selected_category = "Snacks";

        Button button = (Button) findViewById(R.id.select_snacks);

        button.setSelected(true);

        menuCase = 1;

        updateUI();
    }

    public void selectDrinks(View view) {
        resetButton();

        selected_category = "Drinks";

        Button button = (Button) findViewById(R.id.select_drinks);

        button.setSelected(true);

        menuCase = 3;

        updateUI();
    }

    public void selectHygienic(View view) {
        resetButton();

        selected_category = "Hygienic";

        Button button = (Button) findViewById(R.id.select_hygienic);

        button.setSelected(true);

        menuCase = 2;

        updateUI();
    }

    public void selectVegetables(View view) {
        resetButton();

        selected_category = "Vegetables";

        Button button = (Button)findViewById(R.id.select_vegetables);

        button.setSelected(true);

        menuCase = 4;

        updateUI();
    }

    public void selectTalkTime(View view) {
        resetButton();

        selected_category = "Talk Time";

        Button button = (Button) findViewById(R.id.select_talk_time);

        menuCase = 5;

        button.setSelected(true);

        updateUI();
    }

    public void selectOther(View view) {
        resetButton();

        selected_category = "Other";

        Button button = (Button) findViewById(R.id.select_other);

        menuCase = 6;

        button.setSelected(true);

        updateUI();
    }
    public void selectRestaurant(View view) {
        resetButton();

        selected_category = "Restaurant";

        Button button = (Button)findViewById(R.id.select_restaurant);

        button.setSelected(true);

        menuCase = 7;

        updateUI();
    }

    private void updateUI() {

        mStockAnalyseAdapter = new StockAnalyseAdapter(this,createStockAnalyseList(), menuCase);
        mListView.setAdapter(mStockAnalyseAdapter);

    }
    public ArrayList<AnalyseStockItem> createStockAnalyseList(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//        Date date = new Date();
//        String currentDate = sdf.format(date);

        double days =1;
        try{days = Double.valueOf(stockInDays.getText().toString().trim());}
        catch (Exception e){}



        ArrayList<AnalyseStockItem> analyseStockList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();


        Cursor cursor = db.rawQuery(getQueryStockAnalyse(date90DaysBack(sdf)),new String[]{});
        while (cursor.moveToNext() ) {
            String averageSalesADay = cursor.getString(cursor.getColumnIndex("averageSalesADay"));
            int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(ERSList.ItemEntry._ID)));
            String name = cursor.getString(cursor.getColumnIndex(ERSList.ItemEntry.COL_name));
            String cat = cursor.getString(cursor.getColumnIndex(ERSList.ItemEntry.COL_cat));
            String stock = cursor.getString(cursor.getColumnIndex(ERSList.ItemEntry.COL_stock));
            String picture = cursor.getString(cursor.getColumnIndex(ERSList.ItemEntry.COL_picture));
            String proposedAmount;
            try {
                proposedAmount = String.valueOf(Math.ceil(days * Double.valueOf(averageSalesADay)));
            } catch (Exception e) {
                proposedAmount = "Null";
            }
            if (cat.equals(selected_category)) {
                analyseStockList.add(new AnalyseStockItem(id, cat, name, picture, stock, proposedAmount));

            }
        }
        cursor.close();

//        Cursor c = db.rawQuery(getQueryStockAnalyseTest(date90DaysBack(sdf)),new String[]{});
//        while (c.moveToNext() ) {
//            String amount = c.getString(c.getColumnIndex("amount"));
//            String ID = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_name));
//            String frac = c.getString(c.getColumnIndex("frac"));
//            String testDays = c.getString(c.getColumnIndex("testDays"));
//            String dateM = c.getString(c.getColumnIndex("dateM"));
//            Log.d(TAG," ID : "+ ID +" amount : "+amount + " frac : "+frac + " testDays : "+testDays + " dateM :" + dateM);
//
//        }
//        c.close();

        db.close();
        return analyseStockList;
    }
//    public String getQuerySoldOutID(){
//        String tableColumns =
//                ERSList.ItemEntry._ID;
//        String whereClause =  ERSList.ItemEntry.COL_stock+ " <= 0 ";
//        String query = "SELECT " + tableColumns + " FROM " + ERSList.ItemEntry.TABLE
//                + " WHERE " + whereClause ;
//        return query;
//    }
//    public String getQuerySoldOutDateAndID(String soldOut){
//        String tableColumns =
//                "R."+ERSList.RecordEntry.COL_item_ID
//                        + "," +
//                        "MAX(R."+ERSList.RecordEntry.COL_date+") AS soldOutDate";
//        String whereClause = "(R."+ERSList.RecordEntry.COL_stock_after_record+ " <= 0) AND"
//                +"(I."+ERSList.ItemEntry._ID +" = " + "R."+ERSList.RecordEntry.COL_item_ID+")";
//        String groupBy = "R."+ERSList.RecordEntry.COL_item_ID;
//        String from =  "("+ERSList.RecordEntry.TABLE+") AS R,("+
//                soldOut+") AS I ";
//        String query = "SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause + " GROUP BY " + groupBy;
//        return query;
//    }
//    public String getQuerySoldOutDatesAndID(){
//        String tableColumns =
//                "R."+ERSList.RecordEntry.COL_item_ID
//                        + " , " +
//                        "(R."+ERSList.RecordEntry.COL_date+") AS sold_out_date ";
//        String whereClause = "(R."+ERSList.RecordEntry.COL_stock_after_record+ " <= 0)";
//        String from =  "("+ERSList.RecordEntry.TABLE+") AS R";
//        String query = " SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause ;
//        return query;
//    }
//    private String getQueryRefillStockDates(){
//        String tableColumns =
//                "R."+ERSList.RecordEntry.COL_item_ID
//                        + " , " +
//                        "(R."+ERSList.RecordEntry.COL_date+") AS refill_stock_date";
//        String whereClause = "R."+ERSList.RecordEntry.COL_price + " <= 0" ;
//        String from = "("+ERSList.RecordEntry.TABLE+") AS R";
//        String query = " SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause;
//        return query;
//    }
//    private String getQueryRefillStockDates(String id,String XDaysBack){
//        String tableColumns =
//                "MIN("+ERSList.RecordEntry.COL_date+")";
//        String whereClause = "(("+ERSList.RecordEntry.COL_date + " >= " + XDaysBack
//                +") AND (" + ERSList.RecordEntry.COL_item_ID+" = "+id+"))";
//        String from = ERSList.RecordEntry.TABLE;
//        String query = " SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause;
//        return query;
//    }
//    private String getQueryRefillStockDatesFromSoldOutDate2(String XDaysBack){
//        String tableColumns =
//                " COALESCE(" +
//                "T."+ERSList.RecordEntry.COL_item_ID+","+"S."+ERSList.RecordEntry.COL_item_ID+
//                        ")" +
//                        " AS itemID"+
//                        " , " +
//                        " COALESCE(" +
//                        " T.soldOutDate , S.sold_out_date"+
//                        ") "+
//                        " AS soldOutDate"+
//                        " , " +
//                        " COALESCE(" +
//                            " T.refillStockDate" +
//                            " ,"+
//                            "("+getQueryRefillStockDates( " COALESCE(" +
//                            "T."+ERSList.RecordEntry.COL_item_ID+","+"S."+ERSList.RecordEntry.COL_item_ID+
//                            ")",XDaysBack)+")"+
//                            " ,'"+
//                            XDaysBack+
//                        "') "+
//                        " AS refillStockDate";
//        String leftJoin = "("+getQueryRefillStockDatesFromSoldOutDate(XDaysBack)+") AS T" ;
//        String on = "S."+ERSList.RecordEntry.COL_item_ID + " = " + "T."+ERSList.RecordEntry.COL_item_ID
//                ;
////                +") OR (" + "T.soldOutDate  NOT EXISTS = R.sold_out_date))"
////                ;
//        String from = "("+getQuerySoldOutDatesAndID()+") AS S";
//        String query = " SELECT " + tableColumns + " FROM " + from
//                + " LEFT JOIN " + leftJoin  + " ON " + on;
//        return query;
//    }
//    private String getQueryRefillStockDatesFromSoldOutDate(String XDaysBack){
//        String tableColumns =
//                "soldOut."+ERSList.RecordEntry.COL_item_ID+
//                        " , " +
//                        " soldOut.sold_out_date " +
//                        " AS soldOutDate " +
//                        " , " +
//                        " COALESCE(" +
//                        " MAX(refill.refill_stock_date) , '"+XDaysBack+
//                        "') "+
//                        " AS refillStockDate";
//        String whereClause = "((refill."+ERSList.RecordEntry.COL_item_ID + " = " + "soldOut."+ERSList.RecordEntry.COL_item_ID
//                +") AND (" + "refill.refill_stock_date < soldOut.sold_out_date))";
//        String from =
//                "(" +
//                        "("+getQueryRefillStockDates()+") AS refill , "+
//                        "("+getQuerySoldOutDatesAndID()+") AS soldOut "
//                        + ")";
//        String groupBy =
//                " soldOut."+ERSList.RecordEntry.COL_item_ID +
//                        " , " +
//                        " soldOutDate ";
//        String query = " SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause +
//                " GROUP BY " + groupBy + " ORDER BY " + groupBy;
//        return query;
//    }
//    private String getQueryMaxRefillDateWithDuplicates(){
//        String tableColumns =
//                "refill."+ERSList.RecordEntry.COL_item_ID +
//                        " , " +
//                        " DATE('now') " +
//                        " AS soldOutDate "+
//                        " , " +
//                        " MAX(refill.refill_stock_date) " +
//                        " AS refillStockDate ";
//        String from = "("+getQueryRefillStockDates()+") AS refill";
//        String groupBy = "refill."+ERSList.RecordEntry.COL_item_ID;
//        String query = "SELECT " + tableColumns + " FROM " + from +
//                " GROUP BY " + groupBy;
//        return query;
//    }
//    private String getQueryMaxRefillDateContainedInMain(String XDaysBack){
//        String tableColumns =
//                "maxRefill."+ERSList.RecordEntry.COL_item_ID +
//                        " , " +
//                        " main.soldOutDate " +
//                        " AS soldOutDate " +
//                        " , "+
//                        " main.refillStockDate AS refillStockDate ";
//        String from = "(" +
//                "("+getQueryRefillStockDatesFromSoldOutDate2(XDaysBack)
//                +")" +
//                " AS main " +
//                " , " +
//                "("+getQueryMaxRefillDateWithDuplicates()+
//                ")" +
//                " AS maxRefill " +
//                ")";
//        String whereClause = "(main."+ERSList.RecordEntry.COL_item_ID + " = " + "maxRefill."+ERSList.RecordEntry.COL_item_ID
//                +") AND (" + " main.refillStockDate = maxRefill.refillStockDate )";
//        String query = "SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause ;
//        return query;
//    }
//    private String getQueryMaxRefillDateNotContainedInMain(String XDaysBack){
//        String tableColumns =
//                "refill.*";
//        String from = "("+
//                "("+
//                getQueryMaxRefillDateWithDuplicates()+
//                ")"+
//                " AS refill) ";
//        String whereClause = "(" +
//                "refill."+ERSList.RecordEntry.COL_item_ID +
//                " NOT IN ( SELECT "
//                +ERSList.RecordEntry.COL_item_ID +" FROM ("+getQueryMaxRefillDateContainedInMain(XDaysBack)+"))" +
//                ")";
//        String query = "SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause ;
//        return query;
//    }
//    private String getQueryUnionRefillNotContainedInMain(String XDaysBack){
//        String select1 = "*";
//        String from1 = "("+getQueryRefillStockDatesFromSoldOutDate2(XDaysBack)+")";
//        String select2 = "*";
//        String from2 = "("+getQueryMaxRefillDateNotContainedInMain(XDaysBack)+")";
//        String query = " SELECT "+ select1+" FROM " +from1+ " UNION "+
//                " SELECT "+select2+" FROM "+from2;
//        return query;
//    }
//
//    private String getQueryIDAndDaysInStock(String XDaysBack){
//        String tableColumns =
//                "x."+ERSList.RecordEntry.COL_item_ID
//                        + " , " +
//                        "SUM(ABS(JULIANDAY(x.refillStockDate) - JULIANDAY(y.soldOutDate)) + 1 )" +
//                        " AS daysInStock";
//        String whereClause = "(x."+ERSList.RecordEntry.COL_item_ID + " = " + "y."+ERSList.RecordEntry.COL_item_ID + ") " +
//                " AND "+
//                "(x.soldOutDate = y.soldOutDate) " +
//                " AND " +
//                " (x.refillStockDate = y.refillStockDate) "+
//                " AND " +
//                "(x."+ERSList.RecordEntry.COL_item_ID + " = " + "y."+ERSList.RecordEntry.COL_item_ID+")";
//        String from = "(" +
//                getQueryUnionRefillNotContainedInMain(XDaysBack)+
//                ") " +
//                " AS x" +
//                " , " +
//                "("+
//                getQueryUnionRefillNotContainedInMain(XDaysBack)+
//                ") " +
//                " AS y";
//        String groupBy = "x."+ERSList.RecordEntry.COL_item_ID;
//        String query = "SELECT " + tableColumns + " FROM " + from
//                + " WHERE " + whereClause + " GROUP BY " + groupBy;
//        return query;
//    }
//
//    public String getQueryAverageSalesADay(String XDaysBack){
//        String tableColumns =
//                "R."+ERSList.RecordEntry.COL_item_ID +
//                        " , " +
//                        "(SUM(" + ERSList.RecordEntry.COL_amount + ")/"
//                        + "days.daysInStock)" +
//                        " AS averageSalesADay ";
//        String whereClause = "("+ERSList.RecordEntry.COL_price + " > 0 )" +
//                " AND "
//                + "("+ERSList.RecordEntry.COL_date + " > '"+XDaysBack +"')"+
//                " AND " +
//                "(days."+ERSList.RecordEntry.COL_item_ID +" = " + "R."+ERSList.RecordEntry.COL_item_ID+")";
//        String from = "(" +
//                "("+ERSList.RecordEntry.TABLE +") AS R " +
//                " , " +
//                "("+ getQueryIDAndDaysInStock(XDaysBack)+") AS days" +
//                ")";
//        String groupBy = "R."+ERSList.RecordEntry.COL_item_ID;
//
//        String query = "SELECT " + tableColumns + " FROM " +from
//                + " WHERE " + whereClause + " GROUP BY " + groupBy;
//        return query;
//    }
//    private String getQueryStockAnalyseOmega(String XDaysBack){
//        String tableColumns =
//                "I."+ERSList.ItemEntry._ID + "," +"I."+ERSList.ItemEntry.COL_name +
//                        ","+"I."+ERSList.ItemEntry.COL_cat + ","+
//                        "I."+ERSList.ItemEntry.COL_picture + ","+" alpha.averageSalesADay";
//        String whereClause = "I."+ERSList.ItemEntry._ID + " = "+"alpha."+ERSList.RecordEntry.COL_item_ID;
//        String from = "(("+ERSList.ItemEntry.TABLE +") AS I ,(" +getQueryAverageSalesADay(XDaysBack)+") AS alpha)";
//
//        String query = " SELECT " + tableColumns + " FROM " +from
//                + " WHERE " + whereClause ;
//        return query;
//    }
//
//
//    public String getQueryAverageSalesADayBeta(String XDaysBack){
//        String tableColumns =
//                "R."+ERSList.RecordEntry.COL_item_ID + "," +
//                        "(SUM(R." + ERSList.RecordEntry.COL_amount + ")/"
//                        + "COUNT(R."+ERSList.RecordEntry.COL_amount+")) AS averageSalesADay";
//        String whereClause = "R."+ERSList.RecordEntry.COL_price + " > 0 AND R."
//                + ERSList.RecordEntry.COL_date + " > '" + XDaysBack+"'";
//        String from = ERSList.RecordEntry.TABLE +" AS R ";
//        String groupBy = "R."+ERSList.RecordEntry.COL_item_ID;
//
//        String query = " SELECT " + tableColumns + " FROM " +from
//                + " WHERE " + whereClause + " GROUP BY " + groupBy;
//        return query;
//    }
//    private String getQueryStockAnalyseBeta(String XDaysBack){
//        String tableColumns =
//                "I."+ERSList.ItemEntry._ID + "," +"I."+ERSList.ItemEntry.COL_name +
//                        ","+"I."+ERSList.ItemEntry.COL_cat + ","+
//                        "I."+ERSList.ItemEntry.COL_picture + ","+" beta.averageSalesADay";
//        String whereClause = "I."+ERSList.ItemEntry._ID + " = "+"beta."+ERSList.RecordEntry.COL_item_ID;
//        String from = "(("+ERSList.ItemEntry.TABLE +") AS I ,(" +getQueryAverageSalesADayBeta(XDaysBack)+") AS beta)";
//
//        String query = " SELECT " + tableColumns + " FROM " +from
//                + " WHERE " + whereClause ;
//        return query;
//    }
    public String getQueryItemIDSoldOut(){
        String tableColumns =
                ERSList.ItemEntry._ID;
        String whereClause =  ERSList.ItemEntry.COL_stock+ " <= 0 ";
        String query = "SELECT " + tableColumns + " FROM " + ERSList.ItemEntry.TABLE
                + " WHERE " + whereClause ;
        return query;
    }
    public String getQueryItemIDNotSoldOut(){
        String tableColumns =
                ERSList.ItemEntry._ID;
        String whereClause =  ERSList.ItemEntry.COL_stock+ " > 0 ";
        String query = "SELECT " + tableColumns + " FROM " + ERSList.ItemEntry.TABLE
                + " WHERE " + whereClause ;
        return query;
    }
    public String getQueryAverageSalesADay(String XDaysBack,String itemsID){
        String tableColumns =
                "(R."+ERSList.RecordEntry.COL_item_ID + ") AS ID"
                        +
                        "," +
                        "(SUM(R." + ERSList.RecordEntry.COL_amount + ")/"
                        +"((JULIANDAY('now') - JULIANDAY("+"MIN(R."+ERSList.RecordEntry.COL_date+")"+")) )"+") AS averageSalesADay";
        String whereClause =
                "R."+ERSList.RecordEntry.COL_price + " > 0 " +
                        " AND " +
                        "R."+ ERSList.RecordEntry.COL_date + " > '" + XDaysBack+"'"+
                        " AND " +
                        " R."+ERSList.RecordEntry.COL_item_ID + " = " + " items." + ERSList.ItemEntry._ID ;
        String from = ERSList.RecordEntry.TABLE +" AS R ,(" +itemsID+") AS items";
        String groupBy = "R."+ERSList.RecordEntry.COL_item_ID;

        String query = " SELECT " + tableColumns + " FROM " +from
                + " WHERE " + whereClause + " GROUP BY " + groupBy;
        return query;
    }
//    public String getQueryAverageSalesADayTest(String XDaysBack,String itemsID){
//        String tableColumns =
//                "(R."+ERSList.RecordEntry.COL_item_ID + ") AS ID"
//                        +
//                        "," +
//                        "(SUM(R." + ERSList.RecordEntry.COL_amount + ")/"
//                        +"((JULIANDAY('now') - JULIANDAY("+"MIN(R."+ERSList.RecordEntry.COL_date+")"+")) + 1)"+") AS averageSalesADay"
//                        +
//                        " , " +
//                        " SUM(R." + ERSList.RecordEntry.COL_amount + ") AS amount" +
//                        " , " +
//                        "(JULIANDAY('now') - JULIANDAY("+"MIN(R."+ERSList.RecordEntry.COL_date+")"+")) AS frac "+
//                        " , " +
//                        "(JULIANDAY('now') - JULIANDAY("+" '" + XDaysBack+"'"+")) AS testDays"+
//                        " , " +
//                        "MIN(R."+ERSList.RecordEntry.COL_date+") AS dateM "
//                        ;
//        String whereClause =
//                "R."+ERSList.RecordEntry.COL_price + " > 0 " +
//                        " AND " +
//                        "R."+ ERSList.RecordEntry.COL_date + " > '" + XDaysBack+"'"+
//                        " AND " +
//                        " R."+ERSList.RecordEntry.COL_item_ID + " = " + " items." + ERSList.ItemEntry._ID ;
//        String from = ERSList.RecordEntry.TABLE +" AS R ,(" +itemsID+") AS items";
//        String groupBy = "R."+ERSList.RecordEntry.COL_item_ID;
//
//        String query = " SELECT " + tableColumns + " FROM " +from
//                + " WHERE " + whereClause + " GROUP BY " + groupBy;
//        return query;
//    }
//    private String getQueryStockAnalyseTest(String XDaysBack){
//        String tableColumns =
//                 "I."+ERSList.ItemEntry.COL_name +" , "+
//                         " T.amount , T.frac , T.testDays , T.dateM ";
//        String whereClause = "I."+ERSList.ItemEntry._ID + " = "+" T.ID ";
//        String from = "(("+ERSList.ItemEntry.TABLE +") AS I" +
//                " , " +
//                "(" +getQueryAverageSalesADayTest(XDaysBack,getQueryItemIDNotSoldOut())+") AS T)";
//        String query = " SELECT " + tableColumns + " FROM " +from
//                + " WHERE " + whereClause ;
//        return query;
//    }
    public float multiplySoldOut =1.3f;
    public String getQueryAverageSalesADayFull(String XDaysBack){
        String select1 = "*";
        String from1 = "("+getQueryAverageSalesADay(XDaysBack,getQueryItemIDNotSoldOut())+")";
        String select2 = "ID ," +
                " (averageSalesADay*1.3) AS averageSalesADay ";
        String from2 = "("+getQueryAverageSalesADay(XDaysBack,getQueryItemIDSoldOut())+")";
        String query = " SELECT "+ select1+" FROM " +from1+ " UNION "+
                " SELECT "+select2+" FROM "+from2;
        return query;
    }
    private String getQueryStockAnalyse(String XDaysBack){
        String tableColumns =
                "I."+ERSList.ItemEntry._ID + "," +"I."+ERSList.ItemEntry.COL_name + "," + "I."+ERSList.ItemEntry.COL_stock +
                        ","+"I."+ERSList.ItemEntry.COL_cat + ","+
                        "I."+ERSList.ItemEntry.COL_picture + ","+" salesADay.averageSalesADay";
        String whereClause = "I."+ERSList.ItemEntry._ID + " = "+"salesADay.ID";
        String from = "(("+ERSList.ItemEntry.TABLE +") AS I ,(" +getQueryAverageSalesADayFull(XDaysBack)+") AS salesADay)";

        String query = " SELECT " + tableColumns + " FROM " +from
                + " WHERE " + whereClause ;
        return query;
    }
    private String date90DaysBack(SimpleDateFormat sdf) {
        //get the date of 90 days back
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -90);
        String date90 = sdf.format(cal.getTime());
        return date90;
    }
    private Date StringToDate(SimpleDateFormat sdf,String date){
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }
    private String DateToString(SimpleDateFormat sdf,Date date){
        return sdf.format(date);
    }
    private int stringDateDifference(String date1,String date2 ){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return getDateDiff(StringToDate(sdf,date2), StringToDate(sdf,date1), TimeUnit.DAYS);

    }
    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static int getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return (int)timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
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

    public void UserClickedOne(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("1", parent);
    }

    public void UserClickedTwo(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("2", parent);
    }

    public void UserClickedThree(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("3", parent);
    }

    public void UserClickedFour(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("4", parent);
    }

    public void UserClickedFive(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("5", parent);
    }

    public void UserClickedSix(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("6", parent);
    }

    public void UserClickedSeven(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("7", parent);
    }

    public void UserClickedEight(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("8", parent);
    }

    public void UserClickedNine(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("9", parent);
    }

    public void UserClickedZero(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        addNumber("0", parent);
    }

    public void UserClickedBack(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView current_days = (TextView) parent.findViewById(R.id.shopping_days);

        String days_string = current_days.getText().toString();

        days_string = days_string.substring(0, Math.max(0,days_string.length()-1));
        current_days.setText(days_string);
    }


    public void addNumber(String number, View parent) {
        TextView current_days = (TextView) parent.findViewById(R.id.shopping_days);

        String days_string = current_days.getText().toString();
        days_string += number;
        current_days.setText(days_string);
    }

}