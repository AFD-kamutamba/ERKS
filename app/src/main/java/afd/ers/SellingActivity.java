package afd.ers;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

public class SellingActivity extends AppCompatActivity {
    private static final String TAG = "SellingActivity";
    private mDbHelper mHelper;
    private GridView mGridView;
    private ImageView loginLockImage;
    private SellingAdapter mGridAdapter;
    private BasketAdapter mBasketAdapter;
    private ListView mListView;
    private float total_cost;
    private String selected_category;
    private ArrayList<StockItem> basket = new ArrayList<>();
    public Dialog paymentDialog;
    private int menuCase;
    private SharedPreferences loginSwitch;

    public Dialog adminDialog;
    public TextView adminPassword;
    public java.lang.Class targetClass = null;
    private AlertDialog LoginDialog = null;
    private AlertDialog LogoutDialog = null;


    //USB connection*
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    Boolean connectionExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      assign layout
        setContentView(R.layout.selling_page);
//        TODO checker for depricated (maybe a try catch ?)
        setTitle("Sell Items");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));
        TextView textView = new TextView(this);

        ViewGroup layout = (ViewGroup) findViewById(R.id.selling_id);
        layout.addView(textView);
//      connect database
        mHelper = new mDbHelper(this);

        // set the loginswitch
        loginSwitch = PreferenceManager.getDefaultSharedPreferences(this);

        mGridView = (GridView) findViewById(R.id.available_items);
//      we need assign a list for the products that need to be sold
        mListView = (ListView) findViewById(R.id.basket_items);
//      initialize on snacks tab
        Button button = (Button)findViewById(R.id.select_snacks);
        loginLockImage = (ImageView)findViewById(R.id.loginLockImage);

        button.setSelected(true);

        selected_category = "Snacks";
        updateUI();
        updateGrid();
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                sellItemPopUp(view);

                return true;
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                sellItemSingle(view);

            }
        });

        
        basket = new ArrayList<>();

        //USB connection
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

    }

    // Override the back button to do nothing
    @Override
    public void onBackPressed() {
    }

// administrator mode
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_selling_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater factory;
        final View dialogView;

        switch (item.getItemId()) {
            case R.id.action_admin:
                /*
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivityForResult(mainIntent,1);
                */
                factory = LayoutInflater.from(this);
                targetClass = MainActivity.class;
                dialogView = factory.inflate(
                        R.layout.admin_popup, null);
                adminDialog = new Dialog(this, R.style.ourTheme);
                adminDialog.setCancelable(false);
                adminDialog.setContentView(dialogView);
                adminDialog.show();
                adminPassword= (TextView)dialogView.findViewById(R.id.admin_password);

                return true;
            case R.id.latest_transactions:
                factory = LayoutInflater.from(this);
                targetClass = SoldRecordListActivity.class;
                dialogView = factory.inflate(
                        R.layout.admin_popup, null);
                adminDialog = new Dialog(this, R.style.ourTheme);
                adminDialog.setCancelable(false);
                adminDialog.setContentView(dialogView);
                adminDialog.show();
                adminPassword= (TextView)dialogView.findViewById(R.id.admin_password);
                return true;
            case R.id.login:
                loginLogout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loginLogout() {
        String check_login = loginSwitch.getString("loginSwitch", "");
        if (check_login.equals("")) {
            LayoutInflater factory = LayoutInflater.from(this);

            final View loginView = factory.inflate(R.layout.login_popup, null);

            LoginDialog = new AlertDialog.Builder(this)
                    .setView(loginView)

                    .setCancelable(false)
                    .create();
            Spinner NameSpinner = (Spinner) loginView.findViewById(R.id.name_employee_spinner);

            ArrayList<String> items = new ArrayList<String>();

            SQLiteDatabase db = mHelper.getWritableDatabase();

            Cursor c = db.query(ERSList.EmployeeEntry.TABLE,
                    new String[]{ERSList.EmployeeEntry.COL_name}, null, null, null, null, null);

            while (c.moveToNext()){
                items.add(c.getString(c.getColumnIndex(ERSList.EmployeeEntry.COL_name)));
            }
            c.close();
            db.close();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
            NameSpinner.setAdapter(adapter);

            LoginDialog.show();
        } else {
            LayoutInflater factory = LayoutInflater.from(this);

            final View loginView = factory.inflate(R.layout.logout_popup, null);
            TextView employeeNameView = (TextView)loginView.findViewById(R.id.logoutEmployeeName);

            SQLiteDatabase db = mHelper.getWritableDatabase();

            Cursor c = db.rawQuery(getQueryFirstEntryList(), new String[] {});

            String employee_name = "";

            if (c.moveToNext()){

                employee_name = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_name));
            }
            c.close();

            db.close();
            employeeNameView.setText(employee_name);


            LogoutDialog = new AlertDialog.Builder(this)
                    .setView(loginView)

                    .setCancelable(false)
                    .create();

            LogoutDialog.show();
        }
    }

    public void cancelLogin(View v) {
        LoginDialog.cancel();
    }

    public void confirmLogin(View v) {
        v = (View)v.getParent();
        v = (View)v.getParent();
        final Spinner NameSpinner = (Spinner) v.findViewById(R.id.name_employee_spinner);
        final EditText StartMoneyEditText = (EditText) v.findViewById(R.id.start_money_edittext);
        final EditText PasswordEditText = (EditText) v.findViewById(R.id.password_edittext);

        if (NameSpinner.getSelectedItem() == null) {
            Toast.makeText(getApplicationContext(),"No employees detected!",Toast.LENGTH_SHORT).show();
            return;
        }

        String employee_name = NameSpinner.getSelectedItem().toString();
        String start_money_string = StartMoneyEditText.getText().toString();
        String password  = PasswordEditText.getText().toString();

        if (!start_money_string.equals("") && checkValidEmployee(employee_name, password, PasswordEditText)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String date = sdf.format(new Date());

            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            String time = sdf2.format(new Date());

            Float start_money = Float.parseFloat(start_money_string);

            SQLiteDatabase db = mHelper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put(ERSList.ClockInEntry.COL_name, employee_name);
            values.put(ERSList.ClockInEntry.COL_date, date);
            values.put(ERSList.ClockInEntry.COL_start_hour, time);
            values.put(ERSList.ClockInEntry.COL_end_hour, time);
            values.put(ERSList.ClockInEntry.COL_start_money, start_money);
            values.put(ERSList.ClockInEntry.COL_end_money, 0);
            values.put(ERSList.ClockInEntry.COL_difference, 0);

            db.insertWithOnConflict(ERSList.ClockInEntry.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);

            db.close();

            SharedPreferences.Editor editor = loginSwitch.edit();
            editor.putString("loginSwitch","-");
            editor.apply();

            LoginDialog.cancel();
        }
        updateGrid();
        updateUI();
    }

    public void cancelLogout(View v) {
        LogoutDialog.cancel();
    }

    public void confirmLogout(View v) {
        v = (View)v.getParent();
        v = (View)v.getParent();

        final EditText EndMoneyEditText = (EditText) v.findViewById(R.id.end_money_edittext);
        final EditText PasswordEditText = (EditText) v.findViewById(R.id.password_edittext);

        String end_money_string = EndMoneyEditText.getText().toString();
        String password  = PasswordEditText.getText().toString();

        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor c = db.rawQuery(getQueryFirstEntryList(), new String[] {});

        String employee_name = "";
        int clockin_id = 0;
        if (c.moveToNext()){
            clockin_id = c.getInt(c.getColumnIndex(ERSList.ClockInEntry._ID));
            employee_name = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_name));
        }
        c.close();

        db.close();

        if (!end_money_string.equals("") && checkValidEmployee(employee_name, password, PasswordEditText)) {
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            String time = sdf2.format(new Date());

            Float end_money = Float.parseFloat(end_money_string);

            db = mHelper.getWritableDatabase();

            String sql = "UPDATE " + ERSList.ClockInEntry.TABLE +
                    " SET " + ERSList.ClockInEntry.COL_end_hour + " = '" + time  + "' , " +
                    ERSList.ClockInEntry.COL_end_money + " = " + end_money + " , " +
                    ERSList.ClockInEntry.COL_difference + " = " + end_money + " - " + ERSList.ClockInEntry.COL_start_money +
                    " WHERE " + ERSList.ClockInEntry._ID + " = " + clockin_id;

            db.execSQL(sql);

            db.close();

            SharedPreferences.Editor editor = loginSwitch.edit();
            editor.putString("loginSwitch","");
            editor.apply();

            LogoutDialog.cancel();
        }
        updateGrid();
        updateUI();

    }

    private String getQueryFirstEntryList(){
        String tableColumns =
                ERSList.ClockInEntry._ID + "," +
                        ERSList.ClockInEntry.COL_name;
        String orderBy = ERSList.ClockInEntry._ID + " DESC ";
        String from = ERSList.ClockInEntry.TABLE;
        String query = "SELECT " + tableColumns + " FROM " + from
                + " ORDER BY " + orderBy;
        return query;
    }

    private boolean checkValidEmployee(String name, String password, EditText PasswordEditText) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor c = db.query(ERSList.EmployeeEntry.TABLE,
                new String[]{ERSList.EmployeeEntry.COL_password}, ERSList.EmployeeEntry.COL_name + " = ?", new String[]{name}, null, null, null);

        if (c.moveToNext()){
            if(c.getString(c.getColumnIndex(ERSList.EmployeeEntry.COL_password)).equals(password)) {
                db.close();
                db.close();
                return true;
            } else {
                PasswordEditText.setText("");
                Toast.makeText(getApplicationContext(),"Wrong Password!",Toast.LENGTH_SHORT).show();
            }
        }
        c.close();
        db.close();

        return false;
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

    public void sellItemPopUp(View view) {

        final TextView category = (TextView) view.findViewById(R.id.item_cat);
        final TextView name = (TextView) view.findViewById(R.id.item_title);
        final TextView price = (TextView) view.findViewById(R.id.item_price);
        final TextView ID = (TextView) view.findViewById(R.id.item_ID);





        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.selling_item_edit, null);
        final EditText taskEditAmount = (EditText)textEntryView.findViewById(R.id.set_amount);
        final ImageView imagePreview = (ImageView) textEntryView.findViewById(R.id.popup_specify_picture);
        final TextView titleTextView = (TextView) textEntryView.findViewById(R.id.specify_title);

        String title = "Specify the amount of ";
        titleTextView.setText(title + name.getText().toString());

        int itemID;

        try {
            itemID = Integer.parseInt(ID.getText().toString());
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }

        String imageUri = "";

        ImageButton cancel = (ImageButton)textEntryView.findViewById(R.id.specify_cancel);
        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.specify_confirm);

        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor c = db.rawQuery(imageUri(ID.getText().toString()),new String[]{});
        while (c.moveToNext()){
            imageUri = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture));

        }
        c.close();
        db.close();

        imagePreview.setImageURI(Uri.fromFile(new File(imageUri)));


        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(textEntryView)
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
                try{
                    int amount = Integer.valueOf(taskEditAmount.getText().toString().trim());
                    float cost = Float.parseFloat(price.getText().toString());
                    cost = (float)Math.floor(cost * 10.0) / (float) 10.0;

                    total_cost += amount * cost;

                    int exists = -1;

                    for (int i = 0; i < basket.size(); i++) {
                        if (ID.getText().toString().equals(String.valueOf(basket.get(i).getItemID()))) {
                            exists = i;
                            break;
                        }
                    }

                    if (exists >= 0) {
                        basket.get(exists).increaseAmount(amount);
                    } else {
                        SQLiteDatabase db = mHelper.getWritableDatabase();

                        String select = "SELECT " + ERSList.ItemEntry._ID + "," + ERSList.ItemEntry.COL_name + "," + ERSList.ItemEntry.COL_cat + "," + ERSList.ItemEntry.COL_price + "," + ERSList.ItemEntry.COL_picture + "," + ERSList.ItemEntry.COL_stock ;
                        String from = " FROM " + ERSList.ItemEntry.TABLE;
                        String where = " WHERE " + ERSList.ItemEntry._ID + " = " + ID.getText().toString();
                        Cursor cursor = db.rawQuery(select + from + where,new String[]{});

                        /*
                        String whereClause = "lower(" + ERSList.ItemEntry.COL_name + ") = ?";
                        String[] whereArgs = new String[1];
                        whereArgs[0] = name.getText().toString().toLowerCase();



                        Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
                                new String[]{ERSList.ItemEntry._ID, ERSList.ItemEntry.COL_name,
                                        ERSList.ItemEntry.COL_cat, ERSList.ItemEntry.COL_price,
                                        ERSList.ItemEntry.COL_picture, ERSList.ItemEntry.COL_stock},
                                whereClause, whereArgs, null, null, null);
                        */

                        while (cursor.moveToNext()) {
                            int picture = cursor.getColumnIndex(ERSList.ItemEntry.COL_picture);
                            int itemID = cursor.getColumnIndex(ERSList.ItemEntry._ID);
                            int stock = cursor.getColumnIndex(ERSList.ItemEntry.COL_stock);

                            StockItem newEntry = new StockItem(
                                    basket.size(),cursor.getInt(itemID), category.getText().toString(),
                                    name.getText().toString(), cost, amount,
                                    cursor.getString(picture),Math.max(0,cursor.getInt(stock)));
                            basket.add(0, newEntry);
                        }
                        db.close();
                    }
                } catch(NumberFormatException e) {

                }

                updateUI();
                dialog.cancel();

            }
        });

        updateUI();
    }
    public void sellItemSingle(View view){
        final TextView category = (TextView) view.findViewById(R.id.item_cat);
        final TextView name = (TextView) view.findViewById(R.id.item_title);
        final TextView price = (TextView) view.findViewById(R.id.item_price);
        final TextView ID = (TextView) view.findViewById(R.id.item_ID);
        int amount = 1;
        float cost = Float.parseFloat(price.getText().toString());
        cost = (float)Math.floor(cost * 10.0) / (float)10.0;

        total_cost += cost;

        int exists = -1;

        for (int i = 0; i < basket.size(); i++) {
            if (ID.getText().toString().equals(String.valueOf(basket.get(i).getItemID()))) {
                exists = i;
                break;
            }
        }

        if (exists >= 0) {
            basket.get(exists).increaseAmount(1);
        } else {

            /*
            SQLiteDatabase db = mHelper.getWritableDatabase();
            String whereClause = "lower(" + ERSList.ItemEntry.COL_name + ") = ?";
            String[] whereArgs = new String[1];
            whereArgs[0] = name.getText().toString().toLowerCase();
            Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
                    new String[]{ERSList.ItemEntry._ID, ERSList.ItemEntry.COL_name,
                            ERSList.ItemEntry.COL_cat, ERSList.ItemEntry.COL_price,
                            ERSList.ItemEntry.COL_picture, ERSList.ItemEntry.COL_stock},
                    whereClause, whereArgs, null, null, null);

            */
            SQLiteDatabase db = mHelper.getWritableDatabase();

            String select = "SELECT " + ERSList.ItemEntry._ID + "," + ERSList.ItemEntry.COL_name + "," + ERSList.ItemEntry.COL_cat + "," + ERSList.ItemEntry.COL_price + "," + ERSList.ItemEntry.COL_picture + "," + ERSList.ItemEntry.COL_stock ;
            String from = " FROM " + ERSList.ItemEntry.TABLE;
            String where = " WHERE " + ERSList.ItemEntry._ID + " = " + ID.getText().toString();
            Cursor cursor = db.rawQuery(select + from + where,new String[]{});

            while (cursor.moveToNext()) {
                int picture = cursor.getColumnIndex(ERSList.ItemEntry.COL_picture);
                int itemID = cursor.getColumnIndex(ERSList.ItemEntry._ID);
                int stock = cursor.getColumnIndex(ERSList.ItemEntry.COL_stock);


                StockItem newEntry = new StockItem(basket.size(),cursor.getInt(itemID), category.getText().toString(),
                        name.getText().toString(), cost, amount,
                        cursor.getString(picture),
                        Math.max(0,cursor.getInt(stock)));
                basket.add(0, newEntry);
            }
            db.close();

        }

        updateUI();
    }

    public void removeFromBasket(View view) {
        View parent = (View) view.getParent();
        TextView NameTextView = (TextView) parent.findViewById(R.id.item_title);
        String name = String.valueOf(NameTextView.getText());

        for (int i = 0; i < basket.size(); i++) {
            if (name.equals(basket.get(i).getName())) {
                total_cost -= basket.get(i).getAmount() * basket.get(i).getPrice();
                basket.remove(i);
                break;
            }
        }

        updateUI();
    }

    public void Checkout(View view) {

        // USB Connection
        if(serialPort == null) {
            onClickStart();
        }
        if(serialPort != null) {
            onClickSend();
        }
        // USB Connection

        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(
                R.layout.selling_popup_payment, null);
        paymentDialog = new Dialog(this, R.style.ourTheme);
        paymentDialog.setCancelable(false);
        paymentDialog.setContentView(dialogView);
        paymentDialog.show();

        final TextView totalCost = (TextView) dialogView.findViewById(R.id.total_cost);
        totalCost.setText(String.valueOf(total_cost));

        final Button checkoutButton = (Button) view;
        final ImageButton confirmPaymentButton = (ImageButton)dialogView.findViewById(R.id.accept_payment);

        checkoutButton.setEnabled(false);
        confirmPaymentButton.setEnabled(false);

        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        checkoutButton.setEnabled(true);
                        confirmPaymentButton.setEnabled(true);
                    }
                });
            }
        }, 800);

    }

    public void UserClickedOne(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "1";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedTwo(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "2";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedThree(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "3";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedFour(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "4";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedFive(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "5";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedSix(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "6";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedSeven(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "7";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedEight(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "8";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedNine(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "9";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedPlus(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "+";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedPoint(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + ".";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedZero(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation + "0";
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void UserClickedBack(View view) {
        View parent = (View)view.getParent();
        parent = (View)parent.getParent();
        parent = (View)parent.getParent();
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String calculation = "";
        if(CalculationString.getText() != null) {
            calculation = CalculationString.getText().toString();
        }

        calculation = calculation.substring(0, Math.max(0,calculation.length()-1));
        calculateTotalReceivedMoney(calculation, parent);
    }

    public void calculateTotalReceivedMoney(String calculation, View parent) {
        TextView MoneyReceivedView = (TextView)parent.findViewById(R.id.money_received);
        TextView MoneyOwedView = (TextView)parent.findViewById(R.id.money_owed);
        TextView CalculationString = (TextView)parent.findViewById(R.id.calculation_string);

        String[] elements = calculation.split("\\+");

        float result = 0;
        for (int i = 0; i < elements.length; i++) {
            if(elements[i].matches("[0-9]*\\.?[0-9]+")) {
                result += Float.parseFloat(elements[i]);
            }
        }

        Float moneyOwed = Math.max(result-total_cost,0);
        moneyOwed = (float)Math.floor(moneyOwed * 10.0) / (float)10.0;
        MoneyOwedView.setText(String.valueOf(moneyOwed));

        CalculationString.setText(calculation);

        calculation = String.valueOf(result);
        MoneyReceivedView.setText(calculation);
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

        Button button = (Button)findViewById(R.id.select_snacks);

        button.setSelected(true);

        menuCase = 1;


        updateGrid();
    }

    public void selectDrinks(View view) {
        resetButton();

        selected_category = "Drinks";

        Button button = (Button)findViewById(R.id.select_drinks);

        button.setSelected(true);

        menuCase = 3;

        updateGrid();
    }

    public void selectHygienic(View view) {
        resetButton();

        selected_category = "Hygienic";

        Button button = (Button)findViewById(R.id.select_hygienic);

        button.setSelected(true);

        menuCase = 2;

        updateGrid();
    }

    public void selectVegetables(View view) {
        resetButton();

        selected_category = "Vegetables";

        Button button = (Button)findViewById(R.id.select_vegetables);

        button.setSelected(true);

        menuCase = 4;

        updateGrid();
    }

    public void selectTalkTime(View view) {
        resetButton();

        selected_category = "Talk Time";

        Button button = (Button)findViewById(R.id.select_talk_time);

        button.setSelected(true);

        menuCase = 5;

        updateGrid();
    }

    public void selectOther(View view) {
        resetButton();

        selected_category = "Other";

        Button button = (Button)findViewById(R.id.select_other);

        button.setSelected(true);

        menuCase = 6;

        updateGrid();
    }
    public void selectRestaurant(View view) {
        resetButton();

        selected_category = "Restaurant";

        Button button = (Button)findViewById(R.id.select_restaurant);

        button.setSelected(true);

        menuCase = 7;

        updateGrid();
    }

    private void updateGrid() {

        String check_login = loginSwitch.getString("loginSwitch", "");
        if (check_login.equals("")){
            loginLockImage.setVisibility(View.VISIBLE);
            ArrayList<StockItem> itemList = new ArrayList<>();
            mGridAdapter = new SellingAdapter(this, itemList, menuCase);
            mGridView.setAdapter(mGridAdapter);
        }else {
            loginLockImage.setVisibility(View.INVISIBLE);
            ArrayList<StockItem> itemList = new ArrayList<>();
            SQLiteDatabase db = mHelper.getReadableDatabase();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Cursor cursor = db.rawQuery(getQueryOrderdSellingList(date90DaysBack(sdf)),new String[]{});
//        Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
//                new String[]{ERSList.ItemEntry._ID, ERSList.ItemEntry.COL_cat,
//                        ERSList.ItemEntry.COL_name, ERSList.ItemEntry.COL_price,
//                        ERSList.ItemEntry.COL_picture},
//                null, null, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getColumnIndex(ERSList.ItemEntry._ID);
                int id_cat = cursor.getColumnIndex(ERSList.ItemEntry.COL_cat);
                int id_name = cursor.getColumnIndex(ERSList.ItemEntry.COL_name);
                int id_price = cursor.getColumnIndex(ERSList.ItemEntry.COL_price);
                int id_picture = cursor.getColumnIndex(ERSList.ItemEntry.COL_picture);
                if (cursor.getString(id_cat).equals(selected_category)) {
                    String name = cursor.getString(id_name);
                    if (name.endsWith("%") == false) {
                        itemList.add(new StockItem(cursor.getInt(id), cursor.getString(id_cat),
                                cursor.getString(id_name), cursor.getFloat(id_price), 1,
                                cursor.getString(id_picture)));
                    }
                }
            }
            mGridAdapter = new SellingAdapter(this, itemList, menuCase);
            mGridView.setAdapter(mGridAdapter);

            cursor.close();

            db.close();
        }





    }
    public String getQueryItemSoldAmount(String XDaysBack){
        String tableColumns =
                ERSList.RecordEntry.COL_item_ID +" AS ID " +
                        " , " +
                        "SUM(" + ERSList.RecordEntry.COL_amount + ") AS soldAmount";
        String whereClause =  ERSList.RecordEntry.COL_date + " > '" + XDaysBack+"'"+
                " AND "+
                ERSList.RecordEntry.COL_price + " > 0 " ;
        String groupBy = " ID ";
        String query = "SELECT " + tableColumns + " FROM " + ERSList.RecordEntry.TABLE
                + " WHERE " + whereClause + " GROUP BY " + groupBy ;
        return query;
    }
    public String getQueryItemNotInSoldAmount(String XDaysBack){
        String tableColumns =
                ERSList.ItemEntry._ID +" AS ID "+
                        " , " +
                        "(" + 0 + ") AS soldAmount";
        String whereClause =  ERSList.ItemEntry._ID;

        String notInSelect = "itemSold.ID";
        String notInFrom = "("+getQueryItemSoldAmount(XDaysBack)+") AS itemSold";
        String notIn =" SELECT " + notInSelect + " FROM "+ notInFrom;

        String query = "SELECT " + tableColumns + " FROM " + ERSList.ItemEntry.TABLE
                + " WHERE " + whereClause + " NOT IN ( " + notIn +" )";
        return query;
    }
    public String getQueryItemSoldAmountFull(String XDaysBack){
        String select1 = "*";
        String from1 = "("+getQueryItemSoldAmount(XDaysBack)+")";
        String select2 = "*";
        String from2 = "("+getQueryItemNotInSoldAmount(XDaysBack)+")";
        String query = " SELECT "+ select1+" FROM " +from1+ " UNION "+
                " SELECT "+select2+" FROM "+from2;
        return query;
    }
    private String getQueryOrderdSellingList(String XDaysBack){
        String tableColumns =
                "I."+ERSList.ItemEntry._ID+" , "+ "I."+ERSList.ItemEntry.COL_cat+" , "+
                        "I."+ERSList.ItemEntry.COL_name+" , "+  "I."+ERSList.ItemEntry.COL_price+" , "+
                        "I."+ERSList.ItemEntry.COL_picture +" , "+" itemsAmount.soldAmount " ;
        String whereClause = "itemsAmount.ID" +" = "+  "I."+ERSList.ItemEntry._ID;
        String from =
                ERSList.ItemEntry.TABLE+" AS I" +
                " , "+
                "("+getQueryItemSoldAmountFull(XDaysBack)+") AS itemsAmount";
        String orderBy = "itemsAmount.soldAmount";
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause + " ORDER BY " + orderBy + " DESC ";
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

    private void updateUI() {
        total_cost = (float)Math.floor(total_cost * 10.0) / (float)10.0;

        mBasketAdapter = new BasketAdapter(this, basket);
        mListView.setAdapter(mBasketAdapter);

        TextView text_price = (TextView)this.findViewById(R.id.text_total_price);
        text_price.setText("Cost: " +getString(R.string.currency)+" "+ String.valueOf(total_cost));
    }

    //USB Connection

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {

                //Log.d("ALRT", "second ");Log.d("ALRT", "second ");Log.d("ALRT", "second ");
               // boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
               // if (granted) {

                    //Log.d("ALRT", "third ");Log.d("ALRT", "third ");Log.d("ALRT", "third ");
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {

                       // Log.d("ALRT", "fourth ");Log.d("ALRT", "fourth ");Log.d("ALRT", "fourth ");
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            connectionExist = true;
                            //Log.d("ALERT", "IT IS STARTING");
                            //Log.d("ALERT", "IT IS STARTING");
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);


                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                //} else {
                 //   Log.d("SERIAL", "PERM NOT GRANTED");
               // }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop();
                connectionExist = false;

            }
        }

        ;
    };
    public void onClickStart() {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                   // Log.d("ALRT", "EHJGFKJDSBHJFNDSKFBJSHFJS ");Log.d("ALRT", "EHJGFKJDSBHJFNDSKFBJSHFJS ");Log.d("ALRT", "EHJGFKJDSBHJFNDSKFBJSHFJS ");

                    broadcastReceiver.onReceive(this, new Intent(ACTION_USB_PERMISSION));
                    //PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    //usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }


    }
    public void onClickSend() {

        String string = "o";
        serialPort.write(string.getBytes());


    }

    public void onClickStop() {

        if (serialPort != null) {
            serialPort.close();
        }

    }

    public void cancelPayment(View view){
        paymentDialog.cancel();
        updateUI();
    }
    public void confirmPayment(View view){


        if (total_cost != 0) {
            total_cost = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String date = sdf.format(new Date());

            SQLiteDatabase db = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            for (int i = 0; i < basket.size(); i++) {
                StockItem item = basket.get(i);

                values.put(ERSList.RecordEntry.COL_price, item.getPrice());
                values.put(ERSList.RecordEntry.COL_amount, item.getAmount());
                values.put(ERSList.RecordEntry.COL_date, date);
                values.put(ERSList.RecordEntry.COL_item_ID, item.getItemID());
                values.put(ERSList.RecordEntry.COL_stock_after_record, item.getStock() - item.getAmount());


                db.insertWithOnConflict(ERSList.RecordEntry.TABLE,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);

                String sql = "UPDATE " + ERSList.ItemEntry.TABLE +
                        " SET " + ERSList.ItemEntry.COL_stock + " = MAX(0," + ERSList.ItemEntry.COL_stock + " - " + item.getAmount() +
                        ") WHERE lower(" + ERSList.ItemEntry.COL_name + ") = '" + item.getName().toLowerCase() + "'";

                db.execSQL(sql);
            }

            db.close();

            basket = new ArrayList<>();

            
            // Confirm payment popup
            LayoutInflater inflater = getLayoutInflater();
            View confirmPayment = inflater.inflate(R.layout.sell_confirm,
                    (ViewGroup) findViewById(R.id.relative_layout_id_confirm_payment));

            Toast toast = new Toast(this);
            toast.setView(confirmPayment);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        paymentDialog.cancel();

        updateUI();
    }
    public void cancelAdmin(View view){
        adminDialog.cancel();
    }
    public void confirmAdmin(View view){
        if(!checkValidPassword(targetClass)) {
            adminPassword.setText("");
            Toast.makeText(getApplicationContext(),"Wrong Password!",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkValidPassword(java.lang.Class activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sharedPreferencesPassword = preferences.getString("Password", "");
        String sharedPreferencesMasterPassword = preferences.getString("MasterPassword", "");

        if (adminPassword.getText().toString().equalsIgnoreCase(sharedPreferencesPassword) || adminPassword.getText().toString().equalsIgnoreCase(sharedPreferencesMasterPassword)) {
            Intent intent = new Intent(this, activity);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    public void addLetterToPassword(String letter){
        adminPassword.setText(adminPassword.getText()+letter);
    }
    public void pressedBackspace(View view){
        adminPassword.setText(
                adminPassword.getText().subSequence(0,Math.max(0,adminPassword.getText().length() -1)));
    }

    public void pressedQ(View view){
        addLetterToPassword("Q");
    }
    public void pressedW(View view){
        addLetterToPassword("W");
    }
    public void pressedE(View view){
        addLetterToPassword("E");
    }
    public void pressedR(View view){
        addLetterToPassword("R");
    }
    public void pressedT(View view){
        addLetterToPassword("T");
    }
    public void pressedY(View view){
        addLetterToPassword("Y");
    }
    public void pressedU(View view){
        addLetterToPassword("U");
    }
    public void pressedI(View view){
        addLetterToPassword("I");
    }
    public void pressedO(View view){
        addLetterToPassword("O");
    }
    public void pressedP(View view){
        addLetterToPassword("P");
    }
    public void pressedA(View view){
        addLetterToPassword("A");
    }
    public void pressedS(View view){
        addLetterToPassword("S");
    }
    public void pressedD(View view){
        addLetterToPassword("D");
    }
    public void pressedF(View view){
        addLetterToPassword("F");
    }
    public void pressedG(View view){
        addLetterToPassword("G");
    }
    public void pressedH(View view){
        addLetterToPassword("H");
    }
    public void pressedJ(View view){
        addLetterToPassword("J");
    }
    public void pressedK(View view){
        addLetterToPassword("K");
    }
    public void pressedL(View view){
        addLetterToPassword("L");
    }
    public void pressedZ(View view){
        addLetterToPassword("Z");
    }
    public void pressedX(View view){
        addLetterToPassword("X");
    }
    public void pressedC(View view){
        addLetterToPassword("C");
    }
    public void pressedV(View view){
        addLetterToPassword("V");
    }
    public void pressedB(View view){
        addLetterToPassword("B");
    }
    public void pressedN(View view){
        addLetterToPassword("N");
    }
    public void pressedM(View view){
        addLetterToPassword("M");
    }


}