package afd.ers.analyse;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import afd.ers.MainActivity;
import afd.ers.R;
import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;


public class GraphActivity extends AppCompatActivity {

    private mDbHelper mHelper;
    Spinner time_dropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_profit);

        //getSupportActionBar().setTitle("Add or Remove Products");
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Revenue Graph" + "</font>"));
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

        time_dropdown = (Spinner)findViewById(R.id.dates_spinner);
        String[] items = new String[]{"week", "month", "year", "years"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        time_dropdown.setAdapter(adapter);

        time_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                int item = time_dropdown.getSelectedItemPosition();
                int time = 0;
                switch (time_dropdown.getSelectedItem().toString()) {
                    case "week" : time = -7; break;

                    case "month" : time = -30; break;

                    case "year" : time = -365; break;

                    case "years" : time = -1000; break;

                    default : break;
                }

                graphSettings(getDateRevenue(time));
            }
            public void onNothingSelected(AdapterView<?> arg0) { }
        });

        graphSettings(getDateRevenue(-7));

    }


    public void graphSettings(DataPoint[] dataPoints) {

        // you can directly pass Date objects to DataPoint-Constructor
        // this will convert the Date to double via Date#getTime()
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setThickness(8);

        int time = 0;
        switch (time_dropdown.getSelectedItem().toString()) {
            case "week" : time = -7; break;

            case "month" : time = -30; break;

            case "year" : time = -365; break;

            case "years" : time = -1000; break;

            default : break;
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();

        GridLabelRenderer label_renderer = graph.getGridLabelRenderer();
        label_renderer.setPadding(64);
        label_renderer.setVerticalAxisTitle("Revenue");

        graph.addSeries(series);

        // set manual x bounds to have nice steps
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        calendar.add(Calendar.DATE, time);
        Date d2 = calendar.getTime();
        graph.getViewport().setMinX(d2.getTime());
        graph.getViewport().setMaxX(d1.getTime());
        //graph.getViewport().setXAxisBoundsManual(true);

        // set date label formatter

        label_renderer.setLabelFormatter(new DefaultLabelFormatter());
        label_renderer.setNumVerticalLabels(5);
        label_renderer.setLabelFormatter(new DateAsXAxisLabelFormatter(GraphActivity.this));
        label_renderer.setNumHorizontalLabels(5);
        //label_renderer.setHorizontalLabelsVisible(false);// remove horizontal x labels and line
//        label_renderer.setVerticalLabelsVisible(false);

        label_renderer.setGridStyle(GridLabelRenderer.GridStyle.BOTH);

        // as we use dates as labels, the human rounding to nice readable numbers
        //is not necessary
        //label_renderer.setHumanRounding(false);



    }

    private DataPoint[] getDateRevenue(int time) {
        ArrayList<DataPoint> dataPointList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mHelper = new mDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor c = db.rawQuery(getQueryRevenueDate(), getQueryRevenueDateArg(sdf, time));

        while (c.moveToNext() ) {

            String date = c.getString(c.getColumnIndex(ERSList.RecordEntry.COL_date));
            Float profit = c.getFloat(c.getColumnIndex("revenue"));

            try {
                dataPointList.add(new DataPoint(sdf.parse(date), profit));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        c.close();
        db.close();
        return dataPointList.toArray(new DataPoint[dataPointList.size()]);
    }

    private String getQueryRevenueDate() {

        String tableColumns =
                ERSList.RecordEntry.COL_date + "," +
                        "SUM(" + ERSList.RecordEntry.COL_price +" * "+ ERSList.RecordEntry.COL_amount + ") AS revenue";
        String whereClause = ERSList.RecordEntry.COL_date + " > ?" +" AND " + ERSList.RecordEntry.COL_price+" > 0 ";
        String groupBy = ERSList.RecordEntry.COL_date;
        String query = "SELECT " + tableColumns + " FROM " + ERSList.RecordEntry.TABLE
                + " WHERE " + whereClause + " GROUP BY " + groupBy;
        return query;

    }

    private String[] getQueryRevenueDateArg(SimpleDateFormat sdf, int time) {
        //get the date of time days back
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, time);
        String date7 = sdf.format(cal.getTime());
        return new String[]{date7};

    }

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
}
