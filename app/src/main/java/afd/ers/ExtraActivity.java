package afd.ers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URISyntaxException;

import afd.ers.analyse.CashFlowItem;
import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

import static afd.ers.R.id.expenses;
import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

public class ExtraActivity extends AppCompatActivity {
    private static final String TAG = "ExtraActivity";
    private mDbHelper mHelper;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_page);

        getSupportActionBar().setTitle("Extra Features");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));


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

    public void backup(View view) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.remove_warning, null);
        ImageButton cancel = (ImageButton)textEntryView.findViewById(R.id.cancel_remove);
        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.confirm_remove);
        TextView popupTitle = (TextView)textEntryView.findViewById(R.id.removeWarningTitle);
        popupTitle.setText("Are you sure that you want to backup the database?");


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

                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "Kiosk");

                if (!folder.exists()) {
                    if (!folder.mkdirs()) {
                        Toast.makeText(getApplicationContext(), "Cannot make a folder in external storage!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (backupDb()) {
                    if (backupPictures()) {
                        Toast.makeText(getApplicationContext(), "A copy of the database and pictures are saved in the Kiosk folder!", Toast.LENGTH_SHORT).show();
                    }
                }


                dialog.cancel();

            }
        });



        /*
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Kiosk");

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Toast.makeText(getApplicationContext(), "Cannot make a folder in external storage!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (backupDb()) {
            if (backupPictures()) {
                Toast.makeText(getApplicationContext(), "A copy of the database and pictures are saved in the Kiosk folder!", Toast.LENGTH_SHORT).show();
            }
        }
        */


    }

    public boolean backupDb() {
        final String inFileName = "/data/data/afd.ers/databases/ERS_table.db";
        File dbFile = new File(inFileName);
        FileInputStream fis;
        try {
            fis = new FileInputStream(dbFile);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Did not find the database! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        File outDbFile = new File(Environment.getExternalStorageDirectory() + "/Kiosk/kiosk_database_copy.db");
        if (outDbFile.exists()) {
            outDbFile.delete();
        }

        try {
            outDbFile.createNewFile();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can't create new file! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Open the empty db as the output stream
        FileOutputStream output;
        try {
            output = new FileOutputStream(outDbFile);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "The new file was not found! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong with reading and writing data! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public boolean backupPictures() {

        File folder = new File(Environment.getExternalStorageDirectory() + "/Kiosk/kiosk_product_pictures");

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Toast.makeText(getApplicationContext(), "Cannot make a folder in external storage!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        final Pattern p = Pattern.compile("(.*)IMG_(.*)");

        File[] images = getFilesDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return p.matcher(file.getName()).matches();
            }
        });

        for (int i = 0; i < images.length; i++) {
            File picturesFile = images[i];
            FileInputStream fis;
            try {
                fis = new FileInputStream(picturesFile);
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Did not find the pictures! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            String imageName = images[i].toString();

            File outPicturesFile = new File(Environment.getExternalStorageDirectory() + "/Kiosk/kiosk_product_pictures/" + imageName.substring(imageName.indexOf("IMG_"), imageName.length()));
            if (outPicturesFile.exists()) {
                outPicturesFile.delete();
            }

            try {
                outPicturesFile.createNewFile();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Can't create new file! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            // Open the empty db as the output stream
            FileOutputStream output;
            try {
                output = new FileOutputStream(outPicturesFile);
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "The new file was not found! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = fis.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                // Close the streams
                output.flush();
                output.close();
                fis.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong with reading and writing data! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public void recover(View view) {


        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.remove_warning, null);
        ImageButton cancel = (ImageButton)textEntryView.findViewById(R.id.cancel_remove);
        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.confirm_remove);
        TextView popupTitle = (TextView)textEntryView.findViewById(R.id.removeWarningTitle);
        popupTitle.setText("Are you sure that you want to recover the database?");


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

                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "Kiosk");
                if (!folder.exists()) {
                    Toast.makeText(getApplicationContext(), "No backup found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (recoverDb()) {
                    if (recoverPictures()) {
                        Toast.makeText(getApplicationContext(), "Recovery successful!", Toast.LENGTH_SHORT).show();
                    }
                }

                dialog.cancel();

            }
        });

        /*
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Kiosk");
        if (!folder.exists()) {
            Toast.makeText(getApplicationContext(), "No backup found!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (recoverDb()) {
            if (recoverPictures()) {
                Toast.makeText(getApplicationContext(), "Recovery successful!", Toast.LENGTH_SHORT).show();
            }
        }
        */
    }

    public boolean recoverDb() {
        final String inFileName = Environment.getExternalStorageDirectory() + "/Kiosk/kiosk_database_copy.db";

        File dbFile = new File(inFileName);
        FileInputStream fis;
        try {
            fis = new FileInputStream(dbFile);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Did not find the copy of the database! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        File outDbFile = new File("/data/data/afd.ers/databases/ERS_table.db");
        if (outDbFile.exists()) {
            outDbFile.delete();
        }

        try {
            outDbFile.createNewFile();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can't create the new internal database! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Open the empty db as the output stream
        FileOutputStream output;
        try {
            output = new FileOutputStream(outDbFile);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "The internal database was not found! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong with reading and writing data! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public boolean recoverPictures() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Kiosk/kiosk_product_pictures");
        if (!folder.exists()) {
            Toast.makeText(getApplicationContext(), "No backup product pictures found!", Toast.LENGTH_SHORT).show();
            return false;
        }

        final Pattern p = Pattern.compile("(.*)IMG_(.*)");

        File image_folder = new File(Environment.getExternalStorageDirectory() + "/Kiosk/kiosk_product_pictures");

        File[] images = image_folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return p.matcher(file.getName()).matches();
            }
        });

        for (int i = 0; i < images.length; i++) {
            File picturesFile = images[i];
            FileInputStream fis;
            try {
                fis = new FileInputStream(picturesFile);
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Did not find the backed-up pictures! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            String imageName = images[i].toString();

            File outPicturesFile = new File(getFilesDir() + "/" + imageName.substring(imageName.indexOf("IMG_"), imageName.length()));
            if (outPicturesFile.exists()) {
                outPicturesFile.delete();
            }

            try {
                outPicturesFile.createNewFile();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Can't copy the pictures! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            // Open the empty db as the output stream
            FileOutputStream output;
            try {
                output = new FileOutputStream(outPicturesFile);
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "The new pictures were not found! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = fis.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                // Close the streams
                output.flush();
                output.close();
                fis.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong with reading and writing data! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }


    public void makeCSVFile(View view) {


        File outDbFile = new File(Environment.getExternalStorageDirectory() + "/kiosk_full_excel.csv");
        if (outDbFile.exists()) {
            outDbFile.delete();
            Toast.makeText(this, "Excel File Updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Excel File Created", Toast.LENGTH_SHORT).show();
        }

        File dbFile = getDatabasePath("ERS_table.db");
        mDbHelper dbhelper = new mDbHelper(getApplicationContext());
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        /*
        SQLiteDatabase db2 = dbhelper.getReadableDatabase();
        Cursor c3 = db2.rawQuery(combineTables(), null);
        c3.close();
        */


        File file = new File(exportDir, "kiosk_full_excel.csv");
        /*
        if (file.exists ()) {
            file.delete ();

        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"CSV file can't make NOOOOOO "+e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }
        */
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM " + ERSList.ItemEntry.TABLE, null);
            csvWrite.writeNext(c.getColumnNames());

            while (c.moveToNext()) {

                String id = c.getString(c.getColumnIndex(ERSList.ItemEntry._ID));
                String name = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_name));
                String category = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_cat));
                String price = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_price));
                String stock = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_stock));
                String imageUri = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture));

                String arrStr[] = {id, category, name, price, stock, imageUri};


                csvWrite.writeNext(arrStr, false);


            }
            c.close();

            String arrayString[] = {""};
            csvWrite.writeNext(arrayString);


            Cursor c2 = db.rawQuery("SELECT * FROM " + ERSList.RecordEntry.TABLE, null);
            csvWrite.writeNext(c2.getColumnNames());

            while (c2.moveToNext()) {
                //Which column you want to exprort
                String id = c2.getString(c2.getColumnIndex(ERSList.RecordEntry._ID));
                String itemid = c2.getString(c2.getColumnIndex(ERSList.RecordEntry.COL_item_ID));
                String date = c2.getString(c2.getColumnIndex(ERSList.RecordEntry.COL_date));
                String amount = c2.getString(c2.getColumnIndex(ERSList.RecordEntry.COL_amount));
                String price = c2.getString(c2.getColumnIndex(ERSList.RecordEntry.COL_price));
                String stockafterrecord = c2.getString(c2.getColumnIndex(ERSList.RecordEntry.COL_stock_after_record));


                String arrStr[] = {id, itemid, price, amount, stockafterrecord, date};


                csvWrite.writeNext(arrStr, false);


            }
            c2.close();


            Cursor c3 = db.rawQuery(combineTables(), null);
            String arrayString2[] = {""};
            csvWrite.writeNext(arrayString2);


            while (c3.moveToNext()) {

                String name = c3.getString(c3.getColumnIndex(ERSList.ItemEntry.COL_name));
                String date = c3.getString(c3.getColumnIndex(ERSList.RecordEntry.COL_date));
                String amount = c3.getString(c3.getColumnIndex(ERSList.RecordEntry.COL_amount));
                String price = c3.getString(c3.getColumnIndex(ERSList.RecordEntry.COL_price));


                String arrStr[] = {name, date, amount, price};

                csvWrite.writeNext(arrStr, false);


            }
            c3.close();


            csvWrite.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }





    }


    private String combineTables() {

        String tableColumns = "entry." + ERSList.ItemEntry.COL_name + ", record." + ERSList.RecordEntry.COL_amount
                + ",record." + ERSList.RecordEntry.COL_price + ", record." + ERSList.RecordEntry.COL_date;
        String table = "(" + ERSList.ItemEntry.TABLE + ") AS entry, (" + ERSList.RecordEntry.TABLE + ") AS record ";
        String whereClause = "entry." + ERSList.ItemEntry._ID + " = record." + ERSList.RecordEntry.COL_item_ID;
        String groupBy = "";
        String query = "SELECT " + tableColumns + " FROM " + table
                + " WHERE " + whereClause; // + " GROUP BY " + groupBy;
        return query;

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

    @Override
    public void onBackPressed() {
    }


    public void changePassword(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.change_password_popup, null);

        final Context context = this;

        final EditText oldPassword = (EditText)textEntryView.findViewById(R.id.oldPassword);
        final EditText newPassword = (EditText)textEntryView.findViewById(R.id.newPassword);
        final EditText confirmPassword = (EditText)textEntryView.findViewById(R.id.confirmNewPassword);

        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.changePasswordConfirm);
        ImageButton cancel = (ImageButton) textEntryView.findViewById(R.id.changePasswordCancel);

        final AlertDialog dialog = new AlertDialog.Builder(this)

                .setView(textEntryView)
                .setCancelable(false)
                .create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 1200;
        lp.height = 500;

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("MasterPassword","KatleenWasEenBeetjeDom");
        editor.apply();


        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                dialog.cancel();

            }
        });
        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String oldPasswordString = oldPassword.getText().toString();
                String newPasswordString = newPassword.getText().toString();
                String confirmPasswordString = confirmPassword.getText().toString();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);


                String sharedPreferencesPassword = preferences.getString("Password", "");


                if(sharedPreferencesPassword.equalsIgnoreCase("") || oldPasswordString.equalsIgnoreCase(preferences.getString("MasterPassword", "")))
                {
                    if(newPasswordString.equals(confirmPasswordString)){
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("Password",newPasswordString);

                        editor.apply();
                        dialog.cancel();
                    }else {
                        Toast.makeText(getApplicationContext(), "Confirm password doesn't match", Toast.LENGTH_LONG).show();
                    }

                }
                if(sharedPreferencesPassword.equals(oldPasswordString) || oldPasswordString.equals(preferences.getString("MasterPassword", ""))){

                    if(newPasswordString.equals(confirmPasswordString)){
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("Password",newPasswordString);
                        editor.apply();
                        dialog.cancel();
                    }else {
                        Toast.makeText(getApplicationContext(), "Confirm password doesn't match", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Old password is not correct", Toast.LENGTH_LONG).show();
                }




            }
        });

    }
}
