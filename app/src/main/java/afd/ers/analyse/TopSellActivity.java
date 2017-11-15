package afd.ers.analyse;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import afd.ers.MainActivity;
import afd.ers.R;
import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

public class TopSellActivity extends AppCompatActivity {
    private static final String TAG = "TopSellActivity";
    private mDbHelper mHelper;
    private ListView mListView;
    private TextView testData;
    private TopSellAdapter mTopSellAdapter;
    //HorizontalBarChart barChart = (HorizontalBarChart) findViewById(R.id.chart);
    private BarChart mChart;
    private ArrayList<ProfitItem> objects;
    private String labelString;
    private String priceString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_sell);
        setTitle("Top Sellers");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

//        TextView textView = new TextView(this);
//
//        ViewGroup layout = (ViewGroup) findViewById(R.id.profit_list_id);
//        layout.addView(textView);

        // ***********************
        mChart = (BarChart)findViewById(R.id.chart);

        // ***********************

        mHelper = new mDbHelper(this);
        //mListView = (ListView) findViewById(R.id.list_top_sell);
        //objects = createProfitList();
        setData(createProfitList());
        //updateUI();
    }
    private void setData(ArrayList<ProfitItem> objects) {

        mChart.setScaleXEnabled(false);
        mChart.setScaleYEnabled(false);
        mChart.setDragEnabled(true);
        mChart.setPinchZoom(false);
        mChart.setScaleEnabled(false);


        //mChart.setVisibleXRangeMaximum(5);
        //mChart.moveViewToX(10);
        //mChart.setFocusable(false);


        ArrayList<String> labels = new ArrayList<String>();


        XAxis xval = mChart.getXAxis();
        xval.setDrawLabels(true);
        xval.setPosition(XAxis.XAxisPosition.BOTTOM);

        xval.setTextSize(20);
        xval.setTextColor(0xff000000);
        xval.setGranularity(1.0f);


        xval.setGridColor(0xffffffff);

        //mChart.setMaxVisibleValueCount(5);
        //mChart.setVisibleXRangeMaximum(5);



        //xval.setAxisMaximum(6);

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        int smallerThenTenValues = 10;

        if(objects.size() <= 10){
            smallerThenTenValues = objects.size();
        }
        xval.setLabelCount(smallerThenTenValues);
        int p = smallerThenTenValues;

        for(int i = 0; i < smallerThenTenValues; i++){


            yVals1.add(new BarEntry(p,objects.get(i).getProfit(),"Label"));
            //labels.add(objects.get(i).getName());

            priceString = priceString + " " + String.valueOf(objects.get(i).getProfit());
            //labelString = labelString + " " + objects.get(i).getName();

            p--;

        }

        labels.add("ooo");




        for (int q = smallerThenTenValues; q > 0; q--){
            labels.add(objects.get(q-1).getName());

        }

        String labelLabelString = "";
        String valueValueString = "";
        for(int i = 0; i < labels.size();i++){
            labelLabelString = labelLabelString + " " + labels.get(i);
            //valueValueString = valueValueString + " " + yVals1.get(i).getYVals().toString();
        }




        xval.setValueFormatter(new IndexAxisValueFormatter(labels));
        xval.setDrawAxisLine(false);

        BarDataSet set1;

        set1 = new BarDataSet(yVals1, "");

        //set1.setDrawIcons(false);
        set1.setColors(ColorTemplate.colorWithAlpha(0x7FFF00,255));
        set1.setLabel("");



        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);


        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        //data.setValueTypeface(mTfLight);
        data.setBarWidth(0.7f);



        mChart.setData(data);



        /*
        for(int i = objects.size(); i > 0; i--){
            yVals1.add(new BarEntry(i,objects.get(i).getProfit()));
        }
        */

        /*
        yVals1.add(new BarEntry(1,50));
        yVals1.add(new BarEntry(2,30));
        yVals1.add(new BarEntry(3,25));
        yVals1.add(new BarEntry(4,10));
        yVals1.add(new BarEntry(5,5));

        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
        yVals1.add(new BarEntry(6,25));
        yVals1.add(new BarEntry(7,10));
        yVals1.add(new BarEntry(8,7));
        yVals1.add(new BarEntry(9,3));
        yVals1.add(new BarEntry(10,1));


        BarDataSet set2;

        set2 = new BarDataSet(yVals2, "The year 2017");

        set2.setDrawIcons(false);

        set2.setColors(ColorTemplate.MATERIAL_COLORS);
        */


        /*
        for (int q = objects.size(); q > 0; q--){
            labels.add(objects.get(q-1).getName());
            labelString = labelString + " " + objects.get(q-1).getName();
        }
        */



        /*

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "The year 2017");

            set1.setDrawIcons(false);

            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            //data.setValueTypeface(mTfLight);
            data.setBarWidth(0.9f);

            mChart.setData(data);
        }
        */
        //labels.add("ooo");




        //mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        /*
        XAxis xval = mChart.getXAxis();
        xval.setDrawLabels(true);

        xval.setPosition(XAxis.XAxisPosition.BOTTOM);
        xval.setTextColor(0xff000000);
        xval.setTextSize(5);
        //xval.setDrawAxisLine(false);
        //xval.setGridColor(0xffffffff);
        xval.setAxisMaximum(5);
        */
    }

    private void updateUI() {

        mTopSellAdapter = new TopSellAdapter(this, createProfitList());
        mListView.setAdapter(mTopSellAdapter);

    }
    private void createChart(){
        /*
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(4f, 0));
        entries.add(new BarEntry(8f, 1));
        entries.add(new BarEntry(6f, 2));
        entries.add(new BarEntry(12f, 3));
        entries.add(new BarEntry(18f, 4));
        entries.add(new BarEntry(9f, 5));

        BarDataSet dataset = new BarDataSet(entries, "# of Calls");



        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        ArrayList<BarEntry> labelss = new ArrayList<>();
        labelss.add()

           */
        /* for create Grouped Bar chart
        ArrayList<BarEntry> group1 = new ArrayList<>();
        group1.add(new BarEntry(4f, 0));
        group1.add(new BarEntry(8f, 1));
        group1.add(new BarEntry(6f, 2));
        group1.add(new BarEntry(12f, 3));
        group1.add(new BarEntry(18f, 4));
        group1.add(new BarEntry(9f, 5));

        ArrayList<BarEntry> group2 = new ArrayList<>();
        group2.add(new BarEntry(6f, 0));
        group2.add(new BarEntry(7f, 1));
        group2.add(new BarEntry(8f, 2));
        group2.add(new BarEntry(12f, 3));
        group2.add(new BarEntry(15f, 4));
        group2.add(new BarEntry(10f, 5));

        BarDataSet barDataSet1 = new BarDataSet(group1, "Group 1");
        //barDataSet1.setColor(Color.rgb(0, 155, 0));
        barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);

        BarDataSet barDataSet2 = new BarDataSet(group2, "Group 2");
        barDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);

        ArrayList<BarDataSet> dataset = new ArrayList<>();
        dataset.add(barDataSet1);
        dataset.add(barDataSet2);
        */
        /*
        BarData data = new BarData(labels, dataset);
        // dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        barChart.setData(data);
        barChart.animateY(5000);
        */

    }


    private ArrayList<ProfitItem> createProfitList(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        ArrayList<ProfitItem> ProfitList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor c = db.rawQuery(getQueryTopSell(getQueryAverageBuyPrice(),getQueryTotalSell()),
                getQueryTopSellArg(sdf));


        while (c.moveToNext() ) {
            String name = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_name));
            String picture = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture));
            Float profit = c.getFloat(c.getColumnIndex("profit"));
            ProfitList.add(new ProfitItem(name, picture, profit));
        }

        c.close();
        db.close();

        Collections.reverse(ProfitList);


        return ProfitList;
    }

    public void topsellPopup(View view){



        View parent = (View) view.getParent();
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.topseller_list_popup, null);
        final ListView topSellerList = (ListView) textEntryView.findViewById(R.id.topsell_list_popup);

        mTopSellAdapter = new TopSellAdapter(this, createProfitList());
        topSellerList.setAdapter(mTopSellAdapter);



        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(textEntryView)
                .create();
        dialog.show();


    }



    private String getQueryAverageBuyPrice() {
    //TODO prijs per stuk of totale aankoop?: per stuk
        String tableColumns =
                ERSList.RecordEntry.COL_item_ID + "," +
                        "(SUM(-" + ERSList.RecordEntry.COL_price + " *" + ERSList.RecordEntry.COL_amount + ")/"
                        + "SUM(" + ERSList.RecordEntry.COL_amount + "))" +
                        " AS average_price";
        String whereClause = ERSList.RecordEntry.COL_price + " <= 0 AND "
                + ERSList.RecordEntry.COL_date + " > ?";
        String groupBy = ERSList.RecordEntry.COL_item_ID;
        String query = "SELECT " + tableColumns + " FROM " + ERSList.RecordEntry.TABLE
                + " WHERE " + whereClause + " GROUP BY " + groupBy;
        return query;

    }

    private String getQueryAverageBuyPriceArg(SimpleDateFormat sdf) {
        //get the date of 90 days back
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -90);
        String date90 = sdf.format(cal.getTime());
        return date90;

    }

    private String getQueryTotalSell() {

        String tableColumns =
                ERSList.RecordEntry.COL_item_ID + "," +
                        "SUM(" + ERSList.RecordEntry.COL_price + "*" + ERSList.RecordEntry.COL_amount + ")" +
                        " AS total_price " +","+
                        "SUM(" + ERSList.RecordEntry.COL_amount + ")" +
                        " AS total_amount";
        String whereClause = ERSList.RecordEntry.COL_price + " > 0 AND "
                + ERSList.RecordEntry.COL_date + " > ?";
        String groupBy = ERSList.RecordEntry.COL_item_ID;
        String query = "SELECT " + tableColumns + " FROM " + ERSList.RecordEntry.TABLE
                + " WHERE " + whereClause + " GROUP BY " + groupBy;
        return query;

    }

    private String getQueryTotalSellArg(SimpleDateFormat sdf) {
        //get the date of 150 days back
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -150);
        String date150 = sdf.format(cal.getTime());
        return date150;

    }

    private String getQueryTopSellID(String buyQuery, String sellQuery) {

        String tableColumns =
                "b."+ERSList.RecordEntry.COL_item_ID
                        + "," +
                        "round((s.total_price ) - (s.total_amount * b.average_price),2)" +
                        "AS profit";
        String whereClause = "b."+ERSList.RecordEntry.COL_item_ID + " = " +
                "s."+ERSList.RecordEntry.COL_item_ID ;
        String orderBy = "profit";
        String from = "("+buyQuery +")"
                + " AS b ,(" + sellQuery +")"+ " AS s";
        String query = "SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause + " ORDER BY " + orderBy;
        return query;

    }
    private String getQueryTopSell(String buyQuery, String sellQuery){
        String tableColumns =
                "qItem."+ERSList.ItemEntry.COL_name
                        + "," + "qItem."+ERSList.ItemEntry.COL_picture + "," +
                        "qID.profit";
        String whereClause = "qID."+ERSList.RecordEntry.COL_item_ID + " = " +
                "qItem."+ERSList.ItemEntry._ID ;
        String orderBy = "profit";
        String from = "("+getQueryTopSellID(buyQuery,sellQuery) +")"
                + " AS qID ,(" + ERSList.ItemEntry.TABLE +")"+ " AS qItem";
        String query = "SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause + " ORDER BY " + orderBy;
        return query;
    }
    private String[] getQueryTopSellArg(SimpleDateFormat sdf) {
        return new String[]{getQueryAverageBuyPriceArg(sdf),
        getQueryTotalSellArg(sdf)};

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
}

