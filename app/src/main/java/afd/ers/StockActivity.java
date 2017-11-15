package afd.ers;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

public class StockActivity extends AppCompatActivity {
    private static final String TAG = "StockActivity";
    private mDbHelper mHelper;
    private StockAdapter mStockAdapter;
    private ListView mListView;
    private String selected_category;
    private int menuCase = 1;
    private ArrayList<StockItem> basket = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_page);

        setTitle("Stock");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

        TextView textView = new TextView(this);

        ViewGroup layout = (ViewGroup) findViewById(R.id.stock_id);
        layout.addView(textView);
        mHelper = new mDbHelper(this);
        mListView = (ListView) findViewById(R.id.stock_product_list);

        Button button = (Button) findViewById(R.id.select_snacks);

        button.setSelected(true);

        selected_category = "Snacks";

        updateUI();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateUI();
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

        button.setSelected(true);

        menuCase = 5;

        updateUI();
    }

    public void selectOther(View view) {
        resetButton();

        selected_category = "Other";

        Button button = (Button) findViewById(R.id.select_other);

        button.setSelected(true);

        menuCase = 6;

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

    private String imageUri(String name) {

        String tableColumns = ERSList.ItemEntry.COL_picture;
        String table = ERSList.ItemEntry.TABLE;
        String whereClause = ERSList.ItemEntry._ID + " = " + "'" + name + "'";
        String groupBy = "";
        String query = "SELECT " + tableColumns + " FROM " + table
                + " WHERE " + whereClause ; // + " GROUP BY " + groupBy;
        return query;

    }

    public void addStock(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        final String date = sdf.format(new Date());

        View parent = (View) view.getParent();
        final TextView name = (TextView) parent.findViewById(R.id.item_title);
        final TextView ID = (TextView) parent.findViewById(R.id.item_id);
        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.add_stock, null);
        final EditText taskEditPrice = (EditText) textEntryView.findViewById(R.id.set_price);
        final EditText taskEditAmount = (EditText) textEntryView.findViewById(R.id.set_amount);
        final TextView stockAddTitle = (TextView) textEntryView.findViewById(R.id.addstock_title);

        ImageView pictureStock = (ImageView) textEntryView.findViewById(R.id.add_stock_picture);

        ImageButton cancel = (ImageButton)textEntryView.findViewById(R.id.addstock_cancel);
        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.addstock_confirm);

        String imageUri = "";
        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor c = db.rawQuery(imageUri(ID.getText().toString()),new String[]{});
        while (c.moveToNext()){
            imageUri = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture));

        }
        c.close();
        db.close();
        pictureStock.setImageURI(Uri.fromFile(new File(imageUri)));

        stockAddTitle.setText("Specify Price and Amount of " + name.getText().toString());

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(textEntryView)
                /*
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            setValuesDataBase(date ,name ,ID ,taskEditPrice ,taskEditAmount);
                        } catch(NumberFormatException e) {

                    }
                    }
                })
                .setNegativeButton("Cancel", null)
                */
                .create();
        dialog.show();
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                dialog.cancel();

            }
        });
        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {
                    setValuesDataBase(date ,name ,ID ,taskEditPrice ,taskEditAmount);
                } catch(NumberFormatException e) {


                }
                dialog.cancel();
            }
        });

    }

    public void checkStock(View view) {
        View parent = (View) view.getParent();
        final TextView name = (TextView) parent.findViewById(R.id.item_title);
        final TextView ID = (TextView) parent.findViewById(R.id.item_id);

        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.check_stock, null);
        final EditText taskEditAmount = (EditText) textEntryView.findViewById(R.id.set_stock);

        final TextView checkStockTitle = (TextView) textEntryView.findViewById(R.id.check_stock_title);
        final ImageView checkStockPicture = (ImageView) textEntryView.findViewById(R.id.check_stock_picture);
        checkStockTitle.setText("Check stock for " + name.getText().toString());

        ImageButton cancel = (ImageButton)textEntryView.findViewById(R.id.check_cancel);
        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.check_confirm);
        String imageUri = "";
        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor c = db.rawQuery(imageUri(ID.getText().toString()),new String[]{});
        while (c.moveToNext()){
            imageUri = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture));

        }
        c.close();

        checkStockPicture.setImageURI(Uri.fromFile(new File(imageUri)));



        String whereClause = ERSList.ItemEntry.COL_name + " = ?";

        String[] whereArgs = new String[1];
        whereArgs[0] = name.getText().toString();

        Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
                new String[]{ERSList.ItemEntry.COL_stock},
                whereClause, whereArgs, null, null, null);
        while (cursor.moveToNext()) {
            int id_stock = cursor.getColumnIndex(ERSList.ItemEntry.COL_stock);
            TextView current = (TextView) textEntryView.findViewById(R.id.current_stock);
            int stock = cursor.getInt(id_stock);
            current.setText(Integer.toString(stock));
        }

        cursor.close();

        db.close();
        /*
        TextView itemName = (TextView) textEntryView.findViewById(R.id.item_name);
        itemName.setText(name.getText());
        */
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(textEntryView)
                /*
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                        int stock = Integer.valueOf(taskEditAmount.getText().toString().trim());

                        SQLiteDatabase db = mHelper.getWritableDatabase();

                        String sql = "UPDATE " + ERSList.ItemEntry.TABLE +
                                " SET " + ERSList.ItemEntry.COL_stock + " = " + stock +
                                " WHERE " + ERSList.ItemEntry.COL_name + " = \"" + name.getText().toString()+"\"";

                        db.execSQL(sql);

                        db.close();

                        updateUI();
                        } catch(NumberFormatException e) {

                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                */
                .create();
        dialog.show();
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                dialog.cancel();

            }
        });
        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {
                    int stock = Integer.valueOf(taskEditAmount.getText().toString().trim());

                    SQLiteDatabase db = mHelper.getWritableDatabase();

                    String sql = "UPDATE " + ERSList.ItemEntry.TABLE +
                            " SET " + ERSList.ItemEntry.COL_stock + " = " + stock +
                            " WHERE " + ERSList.ItemEntry.COL_name + " = \"" + name.getText().toString()+"\"";

                    db.execSQL(sql);

                    db.close();

                    updateUI();
                } catch(NumberFormatException e) {

                }
                dialog.cancel();

            }
        });


    }

    private void updateUI() {

        ArrayList<StockItem> itemList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
                new String[]{ERSList.ItemEntry._ID, ERSList.ItemEntry.COL_cat, ERSList.ItemEntry.COL_name,ERSList.ItemEntry.COL_price,ERSList.ItemEntry.COL_stock, ERSList.ItemEntry.COL_picture},
                null, null, null, null, ERSList.ItemEntry.COL_name +" COLLATE NOCASE ");
        while (cursor.moveToNext()) {
            int id = cursor.getColumnIndex(ERSList.ItemEntry._ID);
            int idcat = cursor.getColumnIndex(ERSList.ItemEntry.COL_cat);
            int idx = cursor.getColumnIndex(ERSList.ItemEntry.COL_name);
            int idy = cursor.getColumnIndex(ERSList.ItemEntry.COL_stock);
            int id_picture = cursor.getColumnIndex(ERSList.ItemEntry.COL_picture);
            if (cursor.getString(idcat).equals(selected_category)) {
                String name = cursor.getString(idx);
                if (name.endsWith("%") == false) {
                    itemList.add(new StockItem(cursor.getInt(id), cursor.getString(idcat),
                            cursor.getString(idx), 0, cursor.getInt(idy),
                            cursor.getString(id_picture)));
                }
            }
        }

        mStockAdapter = new StockAdapter(this, itemList,menuCase);
        mListView.setAdapter(mStockAdapter);

        cursor.close();
        db.close();

    }

    private void setValuesDataBase(String date ,TextView name , TextView ID ,EditText taskEditPrice ,EditText taskEditAmount){
        float cost = Float.valueOf(taskEditPrice.getText().toString().trim());
        int amount = Integer.valueOf(taskEditAmount.getText().toString().trim());
        cost = cost / amount;
        cost = -(float)Math.floor(cost * 10.0) / (float) 10.0;
        long itemID = Long.valueOf(ID.getText().toString().trim());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(getQueryStockFromID(itemID),new String[]{});
        while (cursor.moveToNext()) {
            int stock = cursor.getInt(cursor.getColumnIndex(ERSList.ItemEntry.COL_stock));
            ContentValues values = new ContentValues();
            values.put(ERSList.RecordEntry.COL_price, cost);
            values.put(ERSList.RecordEntry.COL_amount, amount);
            values.put(ERSList.RecordEntry.COL_date, date);
            values.put(ERSList.RecordEntry.COL_item_ID,itemID);
            values.put(ERSList.RecordEntry.COL_stock_after_record,stock + amount);
            db.insertWithOnConflict(ERSList.RecordEntry.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
        cursor.close();


        String sql = "UPDATE " + ERSList.ItemEntry.TABLE +
                " SET " + ERSList.ItemEntry.COL_stock + " = " + ERSList.ItemEntry.COL_stock + " + " + amount +
                " WHERE lower(" + ERSList.ItemEntry.COL_name + ") = '" + name.getText().toString().toLowerCase() + "'";
        db.execSQL(sql);

        db.close();

        updateUI();

    }

    private String getQueryStockFromID(long ID){
        String tableColumns =
                ERSList.ItemEntry.COL_stock;
        String whereClause = ERSList.ItemEntry._ID + " = " +
                ID ;
        String from =ERSList.ItemEntry.TABLE;
        String query = "SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause ;
        return query;
    }

    // Override the back button to do nothing
    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_admin:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivityForResult(mainIntent,1);
                return true;

            case R.id.latest_transactions:
                Intent intent = new Intent(this, StockRecordListActivity.class);
                startActivityForResult(intent,1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}