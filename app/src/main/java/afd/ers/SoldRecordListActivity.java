package afd.ers;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

public class    SoldRecordListActivity extends AppCompatActivity {
    private static final String TAG = "SoldRecordListActivity";
    private mDbHelper mHelper;
    private ListView mListView;
    private RecordAdapter mRecordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_list);

        setTitle("Last Transactions");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

        TextView textView = new TextView(this);

        ViewGroup layout = (ViewGroup) findViewById(R.id.record_list_id);
        layout.addView(textView);
        mHelper = new mDbHelper(this);
        mListView = (ListView) findViewById(R.id.list_records);

        updateUI();
    }

    public void deleteItem(View view) {
        View parent = (View) view.getParent();
        final TextView ID = (TextView) parent.findViewById(R.id.record_id);

        LayoutInflater factory = LayoutInflater.from(this);

        View WarningView = factory.inflate(R.layout.sold_record_list_return, null);
        ImageButton confirm = (ImageButton)WarningView.findViewById(R.id.confirm_remove);
        ImageButton cancel = (ImageButton)WarningView.findViewById(R.id.cancel_remove);

        TextView setAmount = (TextView)WarningView.findViewById(R.id.item_amount_record);
        TextView setPrice = (TextView)WarningView.findViewById(R.id.item_price);
        TextView setDate = (TextView)WarningView.findViewById(R.id.item_date);
        TextView setName = (TextView)WarningView.findViewById(R.id.item_name);
        final TextView returnedAmount = (TextView) WarningView.findViewById(R.id.returned_amount);
        ImageView setImage = (ImageView)WarningView.findViewById(R.id.item_picture);

        final String task = ID.getText().toString();

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(WarningView)
                .create();
        dialog.show();

        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor c = db.query(ERSList.RecordEntry.TABLE,
                new String[]{ERSList.RecordEntry.COL_item_ID, ERSList.RecordEntry.COL_amount, ERSList.RecordEntry.COL_price, ERSList.RecordEntry.COL_date},
                ERSList.RecordEntry._ID+" = ?", new String[]{task}, null, null, null);

        int item_id = 0;
        if(c.moveToNext()) {
            item_id = c.getInt(c.getColumnIndex(ERSList.RecordEntry.COL_item_ID));
            setAmount.setText(String.valueOf(c.getInt(c.getColumnIndex(ERSList.RecordEntry.COL_amount))));
            setPrice.setText(String.valueOf(c.getFloat(c.getColumnIndex(ERSList.RecordEntry.COL_price))*c.getInt(c.getColumnIndex(ERSList.RecordEntry.COL_amount))));

            SimpleDateFormat firstFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try{
                date = firstFormat.parse(c.getString(c.getColumnIndex(ERSList.RecordEntry.COL_date)));
            } catch (ParseException exc) { }
            SimpleDateFormat secondFormat = new SimpleDateFormat("dd/MM/yyyy");
            setDate.setText(secondFormat.format(date));
        }

        c.close();

        final String secondTask = String.valueOf(item_id);

        c = db.query(ERSList.ItemEntry.TABLE,
                new String[]{ERSList.ItemEntry.COL_name, ERSList.ItemEntry.COL_picture},
                ERSList.ItemEntry._ID+" = ?", new String[]{secondTask}, null, null, null);

        if(c.moveToNext()) {
            setName.setText((c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_name))));
            setImage.setImageURI(Uri.fromFile(new File(c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture)))));
        }

        c.close();
        db.close();

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                dialog.cancel();

            }
        });

        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                SQLiteDatabase db = mHelper.getWritableDatabase();
                Cursor c = db.query(ERSList.RecordEntry.TABLE,
                        new String[]{ERSList.RecordEntry.COL_item_ID, ERSList.RecordEntry.COL_amount, ERSList.RecordEntry.COL_price},
                        ERSList.RecordEntry._ID+" = ?", new String[]{task}, null, null, null);

                int item_id = 0;
                int amount = 0;
                int buyOrSell = 1;
                if(c.moveToNext()) {
                    item_id = c.getInt(c.getColumnIndex(ERSList.RecordEntry.COL_item_ID));
                    amount = c.getInt(c.getColumnIndex(ERSList.RecordEntry.COL_amount));
                    buyOrSell = (int)Math.signum(c.getFloat(c.getColumnIndex(ERSList.RecordEntry.COL_price)));
                }

                c.close();

                int returned_amount = Integer.parseInt(returnedAmount.getText().toString());

                if (amount - returned_amount <= 0) {

                    String sql = "UPDATE " + ERSList.ItemEntry.TABLE +
                            " SET " + ERSList.ItemEntry.COL_stock + " = " + ERSList.ItemEntry.COL_stock + " + " + buyOrSell + " * " + amount +
                            " WHERE " + ERSList.ItemEntry._ID + " = '" + item_id + "'";

                    db.execSQL(sql);

                    db.delete(ERSList.RecordEntry.TABLE,
                            ERSList.RecordEntry._ID + " = ?",
                            new String[]{task});
                    db.close();
                } else {
                    String sql = "UPDATE " + ERSList.ItemEntry.TABLE +
                            " SET " + ERSList.ItemEntry.COL_stock + " = " + ERSList.ItemEntry.COL_stock + " + " + returned_amount
                            + " WHERE " + ERSList.ItemEntry._ID + " = '" + item_id + "'";

                    db.execSQL(sql);

                    String sql2 = "UPDATE " + ERSList.RecordEntry.TABLE +
                            " SET " + ERSList.RecordEntry.COL_amount + " = " + ERSList.RecordEntry.COL_amount + " - " + returned_amount
                            + " , " + ERSList.RecordEntry.COL_stock_after_record + " = " + ERSList.RecordEntry.COL_stock_after_record + " + " + returned_amount
                            + " WHERE " + ERSList.RecordEntry._ID + " = '" + ID.getText().toString() + "'";

                    db.execSQL(sql2);

                    db.close();
                }
                updateUI();
                dialog.cancel();

            }
        });
    }

    private void updateUI() {
        ArrayList<Transaction> RecordList = new ArrayList<>();
        int counter = 0;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(getQueryRecordList(),new String[]{});
        while (cursor.moveToNext() && counter < 50) {
            int id_record  = cursor.getColumnIndex(ERSList.RecordEntry._ID);
            int id_item    = cursor.getColumnIndex(ERSList.RecordEntry.COL_item_ID);
            int idy        = cursor.getColumnIndex(ERSList.RecordEntry.COL_price);
            int idz        = cursor.getColumnIndex(ERSList.RecordEntry.COL_amount);
            int ida        = cursor.getColumnIndex(ERSList.RecordEntry.COL_date);
            RecordList.add(new Transaction(cursor.getInt(id_record), cursor.getInt(id_item), cursor.getFloat(idy), cursor.getInt(idz), cursor.getString(ida)));
            counter++;
        }
        mRecordAdapter = new RecordAdapter(this, RecordList);
        mListView.setAdapter(mRecordAdapter);

        cursor.close();
        db.close();
    }

    private String getQueryRecordList(){
        String tableColumns =
                ERSList.RecordEntry._ID + "," +
                        ERSList.RecordEntry.COL_item_ID + "," +
                        ERSList.RecordEntry.COL_price + "," +
                        ERSList.RecordEntry.COL_amount + "," +
                        ERSList.RecordEntry.COL_date;
        String orderBy = ERSList.RecordEntry._ID + " DESC ";
        String from = ERSList.RecordEntry.TABLE;
        String where = ERSList.RecordEntry.COL_price + " >= 0";
        String query = "SELECT " + tableColumns + " FROM " + from
                + " WHERE " + where + " ORDER BY " + orderBy;
        return query;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_go_back, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.record_list_go_back:
                Intent intent = new Intent(this, SellingActivity.class);
                startActivityForResult(intent,1);
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
        TextView current_days = (TextView) parent.findViewById(R.id.returned_amount);

        String days_string = current_days.getText().toString();

        days_string = days_string.substring(0, Math.max(0,days_string.length()-1));
        current_days.setText(days_string);
    }


    public void addNumber(String number, View parent) {
        TextView current_days = (TextView) parent.findViewById(R.id.returned_amount);

        String days_string = current_days.getText().toString();
        days_string += number;
        current_days.setText(days_string);
    }
}
