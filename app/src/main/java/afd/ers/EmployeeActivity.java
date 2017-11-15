package afd.ers;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import afd.ers.analyse.TopSellAdapter;
import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

public class EmployeeActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeActivity";
    private EmployeeAdapter mEmployeeAdapter = null;
    private mDbHelper mHelper;
    private AlertDialog AddDialog;
    AlertDialog EmployeesDialog;
    private EmployeeClockinAdapter mClockInAdapter;
    private ListView mClockInListView;
    AlertDialog ChangeCashDialog;
    private SharedPreferences loginSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_page);

        setTitle("Employees");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

        //      connect database
        mHelper = new mDbHelper(this);

        mClockInListView = (ListView) findViewById(R.id.clockinlist);

        // set the loginswitch
        loginSwitch = PreferenceManager.getDefaultSharedPreferences(this);

        updateUI();
    }

    public void deleteEmployee(View v) {
        v = (View)v.getParent();
        TextView NameTextView = (TextView)v.findViewById(R.id.employee_name);
        final String employee_name = NameTextView.getText().toString();

        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.remove_warning, null);
        ImageButton cancel = (ImageButton)textEntryView.findViewById(R.id.cancel_remove);
        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.confirm_remove);
        TextView popupTitle = (TextView)textEntryView.findViewById(R.id.removeWarningTitle);

        popupTitle.setText("Are you sure that you want to remove " + employee_name + " ?");


        final AlertDialog dialog = new AlertDialog.Builder(this)

                .setView(textEntryView)
                .setCancelable(false)
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
                SQLiteDatabase db = mHelper.getWritableDatabase();

                db.delete(ERSList.EmployeeEntry.TABLE,
                        ERSList.EmployeeEntry.COL_name + " = ?",
                        new String[]{employee_name});

                db.close();
                EmployeesDialog.cancel();

                SharedPreferences.Editor editor = loginSwitch.edit();
                editor.putString("loginSwitch","");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Employee added!", Toast.LENGTH_SHORT).show();


                dialog.cancel();

            }
        });


    }

    public void addEmployee(View v) {
        LayoutInflater factory = LayoutInflater.from(this);

        final View loginView = factory.inflate(R.layout.employee_add, null);

        AddDialog = new AlertDialog.Builder(this)
                .setView(loginView)

                .setCancelable(false)
                .create();

        AddDialog.show();
    }

    public void cancelAdd(View v) {
        AddDialog.cancel();
    }

    public void confirmAdd(View v) {
        v = (View)v.getParent();
        v = (View)v.getParent();
        final EditText EmployeeNameEditText = (EditText) v.findViewById(R.id.employee_name_add);
        final EditText PasswordEditText = (EditText) v.findViewById(R.id.password_add);

        String employee_name = EmployeeNameEditText.getText().toString();
        String password = PasswordEditText.getText().toString();

        if(!employee_name.equals("") && !password.equals("")) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(ERSList.EmployeeEntry.COL_name, employee_name);
            values.put(ERSList.EmployeeEntry.COL_password, password);

            db.insertWithOnConflict(ERSList.EmployeeEntry.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);

            db.close();

            AddDialog.cancel();
        } else {
            Toast.makeText(getApplicationContext(),"No name or password filled in!",Toast.LENGTH_SHORT).show();
        }
    }

    public void viewEmployees(View v) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.topseller_list_popup, null);
        final ListView EmployeeListView = (ListView) textEntryView.findViewById(R.id.topsell_list_popup);

        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor c = db.query(ERSList.EmployeeEntry.TABLE,
                new String[]{ERSList.EmployeeEntry.COL_name, ERSList.EmployeeEntry.COL_password}, null, null, null, null, null);

        ArrayList<Employee> employees = new ArrayList<>();

        while (c.moveToNext()){
            employees.add(new Employee(c.getString(c.getColumnIndex(ERSList.EmployeeEntry.COL_name)), c.getString(c.getColumnIndex(ERSList.EmployeeEntry.COL_password))));
        }
        c.close();
        db.close();

        mEmployeeAdapter = new EmployeeAdapter(this, employees);
        EmployeeListView.setAdapter(mEmployeeAdapter);

        EmployeesDialog = new AlertDialog.Builder(this)
                .setView(textEntryView)
                .create();
        EmployeesDialog.show();
    }

    public void toExcel(View v) {
        File outDbFile = new File(Environment.getExternalStorageDirectory() + "/clock_in_list.csv");
        if (outDbFile.exists()) {
            outDbFile.delete();
            //Toast.makeText(this,"csv deleted",Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "csv NOT NOT NOT deleted", Toast.LENGTH_SHORT).show();
        }

        File dbFile = getDatabasePath("ERS_table.db");
        mDbHelper dbhelper = new mDbHelper(getApplicationContext());
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "clock_in_list.csv");

        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM " + ERSList.ClockInEntry.TABLE, null);
            csvWrite.writeNext(c.getColumnNames());

            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndex(ERSList.ClockInEntry._ID));
                String name = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_name));
                String date = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_date));
                String starthour = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_start_hour));
                String endhour = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_end_hour));
                String startmoney = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_start_money));
                String endmoney = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_end_money));
                String difference = c.getString(c.getColumnIndex(ERSList.ClockInEntry.COL_difference));

                String arrStr[] = {id, name, date, starthour, endhour, startmoney,endmoney, difference};


                csvWrite.writeNext(arrStr, false);


            }
            c.close();
            Toast.makeText(this,"Clock in list created",Toast.LENGTH_SHORT).show();
            csvWrite.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }

    public void changeCash(View v) {
        LayoutInflater factory = LayoutInflater.from(this);

        final View loginView = factory.inflate(R.layout.employee_change_cash, null);

        ChangeCashDialog = new AlertDialog.Builder(this)
                .setView(loginView)

                .setCancelable(false)
                .create();

        ChangeCashDialog.show();
    }

    public void cancelChangeCash(View v) {
        ChangeCashDialog.cancel();
    }

    public void confirmChangeCash(View v) {
        v = (View)v.getParent();
        v = (View)v.getParent();
        final EditText StartMoneyEditText = (EditText) v.findViewById(R.id.change_start_money);
        final EditText EndMoneyEditText = (EditText) v.findViewById(R.id.change_end_money);

        String start_money_string = StartMoneyEditText.getText().toString();
        String end_money_string = EndMoneyEditText.getText().toString();

        if (!start_money_string.equals("") && !end_money_string.equals("")) {

            Float start_money = Float.parseFloat(start_money_string);
            Float end_money = Float.parseFloat(end_money_string);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String date = sdf.format(new Date());

            SQLiteDatabase db = mHelper.getWritableDatabase();

            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            String time = sdf2.format(new Date());

            ContentValues values = new ContentValues();

            values.put(ERSList.ClockInEntry.COL_name, "Admin");
            values.put(ERSList.ClockInEntry.COL_date, date);
            values.put(ERSList.ClockInEntry.COL_start_hour, time);
            values.put(ERSList.ClockInEntry.COL_end_hour, time);
            values.put(ERSList.ClockInEntry.COL_start_money, start_money);
            values.put(ERSList.ClockInEntry.COL_end_money, end_money);
            values.put(ERSList.ClockInEntry.COL_difference, end_money-start_money);

            db.insertWithOnConflict(ERSList.ClockInEntry.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);

            db.close();

            ChangeCashDialog.cancel();
            updateUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_extra, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_admin:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivityForResult(mainIntent,1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getQueryClockInList(){
        String tableColumns =
                ERSList.ClockInEntry._ID + "," +
                        ERSList.ClockInEntry.COL_name + "," +
                        ERSList.ClockInEntry.COL_date + "," +
                        ERSList.ClockInEntry.COL_start_hour + "," +
                        ERSList.ClockInEntry.COL_end_hour + "," +
                        ERSList.ClockInEntry.COL_start_money + "," +
                        ERSList.ClockInEntry.COL_end_money+","+
                        ERSList.ClockInEntry.COL_difference;
        String orderBy = ERSList.ClockInEntry._ID + " DESC ";
        String from = ERSList.ClockInEntry.TABLE;
        String query = "SELECT " + tableColumns + " FROM " + from;
                //+ " ORDER BY " + orderBy;
        return query;
    }

    public String getLastAdminRevenueQuery(String date) {
        String tableColumns ="SUM("+ERSList.RecordEntry.COL_price + " * " + ERSList.RecordEntry.COL_amount+") as revenue ";
        String whereClause =  "julianday("+ERSList.RecordEntry.COL_date + ")" +
                " > " + date;
        String from = ERSList.RecordEntry.TABLE;
        String query = " SELECT " + tableColumns + " FROM " + from
                + " WHERE " + whereClause;
        return query;
    }

    public void updateUI() {
        ArrayList<ClockIn> ClockInList = new ArrayList<>();
        int counter = 0;
        float end_money = 0f;

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(getQueryClockInList(),new String[]{});

        while (cursor.moveToNext() && counter < 50) {
            int id_name    = cursor.getColumnIndex(ERSList.ClockInEntry.COL_name);
            int id_date    = cursor.getColumnIndex(ERSList.ClockInEntry.COL_date);
            int id_s_hour  = cursor.getColumnIndex(ERSList.ClockInEntry.COL_start_hour);
            int id_e_hour  = cursor.getColumnIndex(ERSList.ClockInEntry.COL_end_hour);
            int id_s_money = cursor.getColumnIndex(ERSList.ClockInEntry.COL_start_money);
            int id_e_money = cursor.getColumnIndex(ERSList.ClockInEntry.COL_end_money);
            int id_diff = cursor.getColumnIndex(ERSList.ClockInEntry.COL_difference);
            boolean discrepancy = false;

            if(counter != 0 && end_money != cursor.getFloat(id_s_money)){
                discrepancy = true;
            }

            ClockInList.add(new ClockIn(cursor.getString(id_name), cursor.getString(id_date), cursor.getString(id_s_hour), cursor.getString(id_e_hour), cursor.getFloat(id_s_money), cursor.getFloat(id_e_money), cursor.getFloat(id_diff), discrepancy));
            end_money = cursor.getFloat(id_e_money);
            counter++;
        }

        ArrayList<ClockIn> ClockInListInverted = new ArrayList<>();

        for(int i = ClockInList.size()-1;i >= 0;i--){
            ClockInListInverted.add(ClockInList.get(i));
        }

        mClockInAdapter = new EmployeeClockinAdapter(this, ClockInListInverted);
        mClockInListView.setAdapter(mClockInAdapter);

        cursor.close();
        db.close();
    }
}
