package afd.ers;


import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

import static afd.ers.R.color.black;
import static afd.ers.R.color.green;

public class ProductActivity extends AppCompatActivity {
    private static final String TAG = "ProductActivity";
    private mDbHelper mHelper;
    private ListView mListView;
    private ProductAdapter mListAdapter;
    private String selected_category;
    private int menuCase = 1;

    private Bitmap picturePreview = null;
    private String productNameBuffer;
    private String productPriceBuffer;
    private Bitmap productPictureBuffer;
    private String productIdBuffer;

    private String productNameBufferOriginal;
    private String productPriceBufferOriginal;
    private Bitmap productPictureBufferOriginal;
    private String productPictureUriBufferOriginal;
    private String deleteImageUri;

    private String newCategory;



    public Dialog addDialog;


    static final int RESULT_LOAD_IMAGE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int RESULT_LOAD_IMAGE_UPDATE = 3;
    static final int REQUEST_IMAGE_CAPTURE_UPDATE = 4;

    static final String[] categoriesX = {"Snacks", "Drinks", "Hygienic", "Fridge", "Talk Time", "Other"};
    static final String[] categories = {"Snacks", "Drinks", "Hygienic", "Vegetables", "Talk Time", "Other","Restaurant"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_page);

        setTitle("Products");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));

        TextView textView = new TextView(this);

        ViewGroup layout = (ViewGroup) findViewById(R.id.products_id);
        layout.addView(textView);
        mHelper = new mDbHelper(this);
        mListView = (ListView) findViewById(R.id.product_list);

        Button button = (Button)findViewById(R.id.select_snacks);

        button.setSelected(true);


        selected_category = "Snacks";


        updateUI();
    }

    public boolean AddProduct(View view) {

        createAddDialog("","",null,0);
        return true;

    }

    public void createUpdateDialog (String name, String price, Bitmap picture, final String item_id){
        final String productName = name;


        String productPrice = price;
        final Bitmap productPicture = picture;
        final String itemId = item_id;


        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.product_item_update, null);
        final EditText taskEditText = (EditText) textEntryView.findViewById(R.id.set_name);
        final EditText taskEditPrice = (EditText) textEntryView.findViewById(R.id.set_price);
        final ImageView picturePreviewView = (ImageView) textEntryView.findViewById(R.id.picture_preview);
        Button photoButton = (Button) textEntryView.findViewById(R.id.set_background);
        Button browseButton = (Button) textEntryView.findViewById(R.id.browse_pictures);
        Button resetButton = (Button) textEntryView.findViewById(R.id.reset_button);

        ImageButton confirm = (ImageButton) textEntryView.findViewById(R.id.add_popup_confirm);
        ImageButton cancel = (ImageButton) textEntryView.findViewById(R.id.add_popup_cancel);
        int position = 0;
        Spinner menuSpinner = (Spinner) textEntryView.findViewById(R.id.categoryChoseSpinner);
        for(int i = 0; i < categories.length;i++){
            if(selected_category.equals(categories[i])){
                position = i;
            }
        }
        final String[] changedCategories = new String[categories.length];
        for(int i = 0; i < categories.length;i++){
            changedCategories[i] = categories[(i+(position))%categories.length];
        }



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, changedCategories);
        menuSpinner.setAdapter(adapter);

        menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        newCategory = changedCategories[0];
                        break;
                    case 1:
                        newCategory = changedCategories[1];
                        break;
                    case 2:
                        newCategory = changedCategories[2];
                        break;
                    case 3:
                        newCategory = changedCategories[3];
                        break;
                    case 4:
                        newCategory = changedCategories[4];
                        break;
                    case 5:
                        newCategory = changedCategories[5];
                        break;
                    case 6:
                        newCategory = changedCategories[6];
                        break;
                    case 7:
                        newCategory = changedCategories[7];
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        taskEditText.setText(productName);
        taskEditPrice.setText(String.valueOf(productPrice));

        if (productPicture != null) {
            picturePreviewView.setImageBitmap(productPicture);

        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(textEntryView)
                .setCancelable(false)
                .create();
        dialog.show();

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_UPDATE);

                productNameBuffer = taskEditText.getText().toString().trim();
                productPriceBuffer = taskEditPrice.getText().toString().trim();
                productPictureBuffer = productPicture;
                productIdBuffer = itemId;

                dialog.cancel();

            }
        });
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE_UPDATE);

                productNameBuffer = taskEditText.getText().toString().trim();
                productPriceBuffer = taskEditPrice.getText().toString().trim();
                productPictureBuffer = productPicture;
                productIdBuffer = itemId;

                dialog.cancel();


            }

        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUpdateDialog(productNameBufferOriginal, productPriceBufferOriginal, productPictureBufferOriginal, productIdBuffer);

                dialog.cancel();

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String price = String.valueOf(taskEditPrice.getText());
                String name = String.valueOf(taskEditText.getText());
                Bitmap photo = picturePreview;

                // Add the new product
                if ((!name.equalsIgnoreCase(productName) ? !checkIfNameExists(name) : true) && name.length() >= 3 && price != "" && picturePreview != null) {

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());

                    File myPath = new File(getFilesDir(), "IMG_" + timeStamp + ".jpg");

                    FileOutputStream fos = null;
                    try {

                        fos = new FileOutputStream(myPath);

                        photo.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                        fos.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    SQLiteDatabase db = mHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(ERSList.ItemEntry.COL_price, price);
                    values.put(ERSList.ItemEntry.COL_name, name);
                    values.put(ERSList.ItemEntry.COL_picture, myPath.getAbsolutePath());
                    values.put(ERSList.ItemEntry.COL_cat,newCategory);

                    db.updateWithOnConflict(ERSList.ItemEntry.TABLE, values, ERSList.ItemEntry._ID + "='" + itemId + "'", null, SQLiteDatabase.CONFLICT_REPLACE);

                    File fdelete = new File(productPictureUriBufferOriginal);

                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            System.out.println("file Deleted :" + productPictureUriBufferOriginal);
                        } else {
                            System.out.println("file not Deleted :" + productPictureUriBufferOriginal);
                        }
                    }

                    db.close();
                    updateUI();

                } else {
                    AlertDialog.Builder payDialogBuilder = new AlertDialog.Builder(ProductActivity.this);
                    payDialogBuilder.setMessage("Please fill in a unique name (min 3 characters) and price and choose a picture")
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int id) {

                                            dialogInterface.cancel();
                                        }
                                    }
                            );
                    AlertDialog payDialog = payDialogBuilder.create();
                    payDialog.show();
                }

                dialog.cancel();

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.cancel();

            }
        });

        updateUI();


    }

    private boolean checkIfNameExists(String name) {

        String tableColumns = ERSList.ItemEntry.COL_name;
        String table = ERSList.ItemEntry.TABLE;
        String whereClause = ERSList.ItemEntry.COL_name + " = " + "'" + name + "'";
        String groupBy = "";
        String query = "SELECT " + tableColumns + " FROM " + table
                + " WHERE " + whereClause ; // + " GROUP BY " + groupBy;



        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor c = db.rawQuery(query,new String[]{});

        if(c.moveToNext()) {
            db.close();
            return true;
        }

        db.close();
        return false;
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

    public void createAddDialog (String name, String price, Bitmap picture, int i){
        String productName = name;
        String productPrice = price;
        Bitmap productPicture = picture;
        int buttonState = i;

        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.product_add, null);
        final EditText taskEditText = (EditText)textEntryView.findViewById(R.id.set_name);
        final EditText taskEditPrice = (EditText)textEntryView.findViewById(R.id.set_price);
        final EditText taskEditAmount = (EditText)textEntryView.findViewById(R.id.set_amount);
        final EditText taskEditPurchase = (EditText)textEntryView.findViewById(R.id.set_purchase_price);
        final ImageView picturePreviewView = (ImageView) textEntryView.findViewById(R.id.picture_preview);
        final Spinner spinnerMargin = (Spinner) textEntryView.findViewById(R.id.margin_spinner);
        final TextView textViewSuggested = (TextView) textEntryView.findViewById(R.id.sell_price);

        Button photoButton = (Button) textEntryView.findViewById(R.id.set_background);
        Button browseButton = (Button) textEntryView.findViewById(R.id.browse_pictures);

        ImageButton confirm = (ImageButton)textEntryView.findViewById(R.id.add_popup_confirm);
        ImageButton cancel = (ImageButton) textEntryView.findViewById(R.id.add_popup_cancel);

        //Button deleteDatabase = (Button) textEntryView.findViewById(R.id.delete_database);

        switch (buttonState) {
            case 0:
                //photoButton.setBackgroundResource(R.drawable.pay_button_style);
                //browseButton.setBackgroundResource(R.drawable.pay_button_style);
                photoButton.setTextColor(getResources().getColor(black));
                browseButton.setTextColor(getResources().getColor(black));

                break;
            case 2:
                //photoButton.setBackgroundResource(R.drawable.pay_button_style_green);
                //browseButton.setBackgroundResource(R.drawable.pay_button_style);

                photoButton.setTextColor(getResources().getColor(green));
                browseButton.setTextColor(getResources().getColor(black));
                break;
            case 1:
                //photoButton.setBackgroundResource(R.drawable.pay_button_style);
                //browseButton.setBackgroundResource(R.drawable.pay_button_style_green);
                photoButton.setTextColor(getResources().getColor(black));
                browseButton.setTextColor(getResources().getColor(green));
                break;
        }


        taskEditText.setText(productName);
        taskEditPrice.setText(String.valueOf(productPrice));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, new String[]{"margin", "1", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7","1.8", "1.9", "2.0"} );
        spinnerMargin.setAdapter(adapter);
        spinnerMargin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    try {
                        String margin_string = spinnerMargin.getSelectedItem().toString();
                        float margin = Float.valueOf(margin_string);
                        float suggested_price = Math.round(margin * Float.valueOf(taskEditPurchase.getText().toString()) / Float.valueOf(taskEditAmount.getText().toString())*10)/(float)10;
                        textViewSuggested.setText(String.valueOf(suggested_price));
                    } catch (NumberFormatException e) {
                        textViewSuggested.setText("0");
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        if(productPicture != null){
            picturePreviewView.setImageBitmap(productPicture);

        }

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
                String category = selected_category;
                String item = taskEditText.getText().toString().trim();
                String price = taskEditPrice.getText().toString().trim();
                String amount = String.valueOf(taskEditAmount.getText());
                String purchase = String.valueOf(taskEditPurchase.getText());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                String date = sdf.format(new Date());
                Bitmap photo = picturePreview;

                if (!checkIfNameExists(item) && item.length() >= 3 && price != "" && picturePreview != null){

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());

                    File myPath = new File(getFilesDir(),"IMG_" + timeStamp + ".jpg");

                    FileOutputStream fos = null;
                    try {

                        fos = new FileOutputStream(myPath);

                        photo.compress(Bitmap.CompressFormat.JPEG,90,fos);

                        fos.flush();

                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        try{
                            fos.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }

                    SQLiteDatabase db = mHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(ERSList.ItemEntry.COL_cat, category);
                    values.put(ERSList.ItemEntry.COL_name, item);
                    values.put(ERSList.ItemEntry.COL_price, price);
                    values.put(ERSList.ItemEntry.COL_picture, myPath.getAbsolutePath());
                    values.put(ERSList.ItemEntry.COL_stock, 0);
                    long item_id = db.insert(ERSList.ItemEntry.TABLE, null, values);

                    // Add Stock for the new product
                    if(!purchase.equals("") && !amount.equals("")) {
                        double cost = Float.parseFloat(purchase) / Integer.parseInt(amount);
                        Log.d("ALERT", purchase+" "+amount+" "+String.valueOf(cost));
                        cost = -Math.floor(cost * 10.0) / (float) 10.0;
                        purchase = String.valueOf(cost);

                        Cursor cursor = db.rawQuery(getQueryStockFromID(item_id),new String[]{});
                        while (cursor.moveToNext()) {
                            int stock = cursor.getColumnIndex(ERSList.ItemEntry.COL_stock);
                            values = new ContentValues();
                            values.put(ERSList.RecordEntry.COL_price, purchase);
                            values.put(ERSList.RecordEntry.COL_amount, amount);
                            values.put(ERSList.RecordEntry.COL_date, date);
                            values.put(ERSList.RecordEntry.COL_item_ID, String.valueOf(item_id));
                            values.put(ERSList.RecordEntry.COL_stock_after_record,stock + amount);
                            db.insertWithOnConflict(ERSList.RecordEntry.TABLE,
                                    null,
                                    values,
                                    SQLiteDatabase.CONFLICT_REPLACE);
                        }
                        cursor.close();

                        String sql = "UPDATE " + ERSList.ItemEntry.TABLE +
                                " SET " + ERSList.ItemEntry.COL_stock + " = " + ERSList.ItemEntry.COL_stock + " + " + amount +
                                " WHERE lower(" + ERSList.ItemEntry.COL_name + ") = '" + item.toLowerCase() + "'";
                        db.execSQL(sql);
                    }

                    db.close();
                    updateUI();
                    dialog.cancel();
                }else{
                    Toast.makeText(getApplicationContext(), "Please fill in a unique name (min 3 characters) and price and choose a picture", Toast.LENGTH_SHORT).show();

                }

            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);

                productNameBuffer = taskEditText.getText().toString().trim();
                productPriceBuffer = taskEditPrice.getText().toString().trim();

                dialog.cancel();

            }
        });
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,RESULT_LOAD_IMAGE);

                productNameBuffer = taskEditText.getText().toString().trim();
                productPriceBuffer = taskEditPrice.getText().toString().trim();

                dialog.cancel();


            }

        });

    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    public static Bitmap decodeSampledBitmapFromUri(ContentResolver CS, Uri imageUri, int reqWidth, int reqHeight) throws FileNotFoundException {

        // Get input stream of the image
        final BitmapFactory.Options options = new BitmapFactory.Options();
        InputStream iStream = CS.openInputStream(imageUri);

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(iStream, null, options);

        Boolean scaleByHeight = Math.abs(options.outHeight - reqHeight) >= Math.abs(options.outWidth - reqWidth);

        if(options.outHeight * options.outWidth * 2 >= 200*200*2){
            // Load, scaling to smallest power of 2 that'll get it <= desired dimensions
            double sampleSize = scaleByHeight
                    ? options.outHeight / 200
                    : options.outWidth / 200;
            options.inSampleSize =
                    (int)Math.pow(2d, Math.floor(
                            Math.log(sampleSize)/Math.log(2d)));
        }

        // Do the actual decoding
        options.inJustDecodeBounds = false;

        try {
            iStream.close();
        } catch (IOException e) { }
        iStream = CS.openInputStream(imageUri);
        Bitmap img = BitmapFactory.decodeStream(iStream, null, options);
        try {
            iStream.close();
        } catch (IOException e) { }

        return img;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");



            Uri imageUri = data.getData();




            //crop photo to square
            Bitmap photoCropped = null;
            if(photo.getWidth() < photo.getHeight()){
                photoCropped = Bitmap.createBitmap(photo, 0, ((photo.getHeight() - photo.getWidth())/2),
                        photo.getWidth() - 1, photo.getHeight() - (photo.getHeight() - photo.getWidth()) - 1);
            }else{
                photoCropped = Bitmap.createBitmap(photo,((photo.getWidth() - photo.getHeight())/2),
                        0, photo.getWidth() - (photo.getWidth() - photo.getHeight()) - 1 ,photo.getHeight() - 1);
            }

            //scale image to 450x450
            Bitmap photoScaled = null;
            if(photo.getHeight() > 450){
                photoScaled = Bitmap.createScaledBitmap(photoCropped,450,450,false);
            }
            if(photoScaled != null){
                picturePreview = photoScaled;
            }else{
                picturePreview = photoCropped;
            }

            createAddDialog(productNameBuffer,productPriceBuffer,picturePreview,2);



        }
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED){
            createAddDialog(productNameBuffer,productPriceBuffer,null,0);
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data){

            Bitmap photo = null;
            Uri imageUri = data.getData();
            try {
                photo = decodeSampledBitmapFromUri(this.getContentResolver(), imageUri, 1000,1000);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap photoCropped = null;
            if(photo.getWidth() < photo.getHeight()){
                photoCropped = Bitmap.createBitmap(photo, 0, ((photo.getHeight() - photo.getWidth())/2),
                        photo.getWidth() - 1, photo.getHeight() - (photo.getHeight() - photo.getWidth()) - 1);
            }else{
                photoCropped = Bitmap.createBitmap(photo,((photo.getWidth() - photo.getHeight())/2),
                        0, photo.getWidth() - (photo.getWidth() - photo.getHeight()) - 1 ,photo.getHeight() - 1);
            }

            //scale image to 450x450
            Bitmap photoScaled = null;
            if(photo.getHeight() > 450){
                photoScaled = Bitmap.createScaledBitmap(photoCropped,450,450,false);
            }
            if(photoScaled != null){
                picturePreview = photoScaled;
            }else{
                picturePreview = photoCropped;
            }

            createAddDialog(productNameBuffer,productPriceBuffer,picturePreview,1);



        }
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_CANCELED){
            createAddDialog(productNameBuffer,productPriceBuffer,null,0);
        }

        // ///////  UPDATE SECTION  //////

        if(requestCode == REQUEST_IMAGE_CAPTURE_UPDATE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            Uri imageUri = data.getData();

            //crop photo to square
            Bitmap photoCropped = null;
            if(photo.getWidth() < photo.getHeight()){
                photoCropped = Bitmap.createBitmap(photo,0,((photo.getHeight() - photo.getWidth())/2),photo.getWidth() -1 ,photo.getHeight() - (photo.getHeight() - photo.getWidth()) - 1);
            }else{
                photoCropped = Bitmap.createBitmap(photo,((photo.getWidth() - photo.getHeight())/2),0,photo.getWidth()- (photo.getWidth() - photo.getHeight()) -1 ,photo.getHeight() - 1);
            }

            //scale image to 450x450
            Bitmap photoScaled = null;
            if(photo.getHeight() > 450){
                photoScaled = Bitmap.createScaledBitmap(photoCropped,450,450,false);
            }
            if(photoScaled != null){
                picturePreview = photoScaled;
            }else{
                picturePreview = photoCropped;
            }

            createUpdateDialog(productNameBuffer,productPriceBuffer,picturePreview,productIdBuffer);

        }
        if(requestCode == REQUEST_IMAGE_CAPTURE_UPDATE && resultCode == RESULT_CANCELED){
            createUpdateDialog(productNameBuffer,productPriceBuffer,productPictureBuffer,productIdBuffer);
        }

        if (requestCode == RESULT_LOAD_IMAGE_UPDATE && resultCode == RESULT_OK && null != data){

            Bitmap photo = null;
            Uri imageUri = data.getData();
            try {
                photo = decodeSampledBitmapFromUri(this.getContentResolver(), imageUri, 1000, 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap photoCropped = null;
            if(photo.getWidth() < photo.getHeight()){
                photoCropped = Bitmap.createBitmap(photo, 0, ((photo.getHeight() - photo.getWidth())/2),
                        photo.getWidth() - 1, photo.getHeight() - (photo.getHeight() - photo.getWidth()) - 1);
            }else{
                photoCropped = Bitmap.createBitmap(photo,((photo.getWidth() - photo.getHeight())/2),
                        0, photo.getWidth() - (photo.getWidth() - photo.getHeight()) - 1 ,photo.getHeight() - 1);
            }

            //scale image to 450x450
            Bitmap photoScaled = null;
            if(photo.getHeight() > 450){
                photoScaled = Bitmap.createScaledBitmap(photoCropped,450,450,false);
            }
            if(photoScaled != null){
                picturePreview = photoScaled;
            }else{
                picturePreview = photoCropped;
            }

            createUpdateDialog(productNameBuffer,productPriceBuffer,picturePreview,productIdBuffer);



        }
        if(requestCode == RESULT_LOAD_IMAGE_UPDATE && resultCode == RESULT_CANCELED){
            createUpdateDialog(productNameBuffer,productPriceBuffer,productPictureBuffer,productIdBuffer);
        }

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

    private String allImageUri(){
        String tableColumns = ERSList.ItemEntry.COL_picture;
        String table = ERSList.ItemEntry.TABLE;
        String whereClause = "*";
        String groupBy = "";
        String query = "SELECT " + tableColumns + " FROM " + table;

        /*
                + " WHERE " + whereClause ; // + " GROUP BY " + groupBy;
                */
        return query;

    }

    public void deleteItem(View view) {
        final View parent = (View) view.getParent();
        final Button b = (Button)view;
        final String buttonText = b.getText().toString();
        LayoutInflater factory = LayoutInflater.from(this);

        final View WarningView = factory.inflate(R.layout.product_item_remove, null);

        TextView taskTextView = (TextView) parent.findViewById(R.id.item_id);
        final String task = String.valueOf(taskTextView.getText());


        ImageView removeProductImage = (ImageView)WarningView.findViewById(R.id.remove_picture);

        SQLiteDatabase db = mHelper.getWritableDatabase();

        String imageUri = "";
        Cursor c = db.rawQuery(imageUri(taskTextView.getText().toString()),new String[]{});
        while (c.moveToNext()){
            imageUri = c.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture));

        }
        deleteImageUri = imageUri;
        c.close();

        final ArrayList<String> allImageUri = new ArrayList<>();
        Cursor c2 = db.rawQuery(allImageUri(),new String[]{});
        while(c2.moveToNext()){
            allImageUri.add(c2.getString(c.getColumnIndex(ERSList.ItemEntry.COL_picture)));
        }
        c2.close();

        removeProductImage.setImageURI(Uri.fromFile(new File(imageUri)));

        ImageButton cancel = (ImageButton)WarningView.findViewById(R.id.cancel_remove);
        ImageButton confirm = (ImageButton) WarningView.findViewById(R.id.confirm_remove);

        //Find name of the product


        Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
                new String[]{ ERSList.ItemEntry.COL_name, ERSList.ItemEntry.COL_picture}, ERSList.ItemEntry._ID + " = ?",
                new String[] {task}, null, null, null, null);

        int id_name = cursor.getColumnIndex(ERSList.ItemEntry.COL_name);
        if (cursor.moveToNext()) {
            final String productName = cursor.getString(id_name);

            final TextView itemName = (TextView) WarningView.findViewById(R.id.removed_item_name);

            if (productName.endsWith("%") == true) {
                final TextView message = (TextView) WarningView.findViewById(R.id.text_product_remove_return);
                if (buttonText.equalsIgnoreCase("discard")){
                    message.setText("Are you sure you want to discard  ");
                }else {
                    message.setText("Are you sure you want to return ");
                }
                itemName.setText(productName.substring(1, productName.length() - 1));
            }else{
                itemName.setText(productName);
            }

            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(WarningView)
                    .create();
            dialog.show();
            cursor.close();
            db.close();

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    dialog.cancel();

                }
            });
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText enteredName = (EditText) WarningView.findViewById(R.id.item_name_check);
                    String checkedName = "";
                    checkedName = enteredName.getText().toString();
                    if (productName.equalsIgnoreCase(checkedName) ||
                            productName.substring(1,productName.length()-1).equalsIgnoreCase(checkedName)) {
                        if (productName.endsWith("%") == true) {
                            if (buttonText.equalsIgnoreCase("Discard")){
                                hardDeleteDB(task, allImageUri);
                            }else{
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                db.execSQL(getQueryReturnItemUpdate(productName));
                                db.close();
                            }

                        }else {
                            SQLiteDatabase db = mHelper.getWritableDatabase();
                            db.execSQL(getQueryRemoveItemUpdate(productName));
                            db.close();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Wrong item name entered", Toast.LENGTH_SHORT).show();
                    }

                    updateUI();
                    dialog.cancel();

                }

            });
        }
    }
    public void hardDeleteDB(String task, ArrayList<String> allImageUri){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cs = db.query(ERSList.ItemEntry.TABLE,
                new String[]{ERSList.ItemEntry._ID, ERSList.ItemEntry.COL_picture}, ERSList.ItemEntry._ID + " = ?",
                new String[]{task}, null, null, null, null);

        Context context = getApplicationContext();
        String deleteUriString = null;
        if (cs.moveToFirst()) {
            deleteUriString = cs.getString(cs.getColumnIndex(ERSList.ItemEntry.COL_picture));

        } else {
            Toast toast = Toast.makeText(context, "Could not find URI !!", Toast.LENGTH_SHORT);
            toast.show();
        }

        int doubleImage = 0;
        for(int i = 0; i < allImageUri.size(); i++){
            if(allImageUri.get(i).equals(deleteImageUri)){
                doubleImage++;
            }
        }

        if(doubleImage <= 1){
            File fdelete = new File(deleteUriString);

            if (fdelete.exists()) {
                fdelete.delete();
            }
        }

        db.delete(ERSList.ItemEntry.TABLE,
                ERSList.ItemEntry._ID + " = ?",
                new String[]{task});
        db.delete(ERSList.RecordEntry.TABLE,
                ERSList.RecordEntry.COL_item_ID + " = ?",
                new String[]{task});
        db.close();
        cs.close();
    }

    public void updateItem(View view) {
        View parent = (View) view.getParent();
        final TextView taskTextView = (TextView) parent.findViewById(R.id.item_id);
        final String task = String.valueOf(taskTextView.getText());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cs = db.query(ERSList.ItemEntry.TABLE,
                new String[] {ERSList.ItemEntry._ID,ERSList.ItemEntry.COL_name, ERSList.ItemEntry.COL_price, ERSList.ItemEntry.COL_picture},ERSList.ItemEntry._ID + " = ?",
                new String[] {task},null,null,null,null);

        Context context = getApplicationContext();
        String name = null;
        String price = null;
        String picture = null;



        if(cs.moveToFirst()) {
            name = cs.getString(cs.getColumnIndex(ERSList.ItemEntry.COL_name));
            price = cs.getString(cs.getColumnIndex(ERSList.ItemEntry.COL_price));
            picture = cs.getString(cs.getColumnIndex(ERSList.ItemEntry.COL_picture));
            Uri pictureUri = Uri.parse("file://"+picture);

            try {
                Bitmap photo = decodeSampledBitmapFromUri(this.getContentResolver(), pictureUri, 1000, 1000);
                productNameBufferOriginal = name;
                productPriceBufferOriginal = price;
                productPictureBufferOriginal = photo;
                productIdBuffer = task;
                productPictureUriBufferOriginal = picture;
                picturePreview = photo;
                if (name.endsWith("%") == true) {
                    deleteItem(view);
                }else {
                    createUpdateDialog(name, price, photo, task);
                }




            } catch (Exception IOException) {
                IOException.printStackTrace();
            } finally {
                db.close();
                cs.close();
            }


        }

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


        updateUI();
    }

    public void selectDrinks(View view) {
        resetButton();

        selected_category = "Drinks";

        Button button = (Button)findViewById(R.id.select_drinks);

        button.setSelected(true);

        menuCase = 3;

        updateUI();
    }

    public void selectHygienic(View view) {
        resetButton();

        selected_category = "Hygienic";

        Button button = (Button)findViewById(R.id.select_hygienic);

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

        Button button = (Button)findViewById(R.id.select_talk_time);

        button.setSelected(true);

        menuCase = 5;

        updateUI();
    }

    public void selectOther(View view) {
        resetButton();

        selected_category = "Other";

        Button button = (Button)findViewById(R.id.select_other);

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

    private void updateUI() {
        ArrayList<StockItem> itemList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
                new String[]{ERSList.ItemEntry._ID, ERSList.ItemEntry.COL_cat, ERSList.ItemEntry.COL_name, ERSList.ItemEntry.COL_price, ERSList.ItemEntry.COL_picture},
                null, null, null, null, ERSList.ItemEntry.COL_name +" COLLATE NOCASE ");
        while (cursor.moveToNext()) {
            int id = cursor.getColumnIndex(ERSList.ItemEntry._ID);
            int idcat = cursor.getColumnIndex(ERSList.ItemEntry.COL_cat);
            int idx = cursor.getColumnIndex(ERSList.ItemEntry.COL_name);
            int idy = cursor.getColumnIndex(ERSList.ItemEntry.COL_price);
            int id_picture = cursor.getColumnIndex(ERSList.ItemEntry.COL_picture);
            if (cursor.getString(idcat).equals(selected_category)) {
                itemList.add(new StockItem(cursor.getInt(id), cursor.getString(idcat), cursor.getString(idx), cursor.getFloat(idy), 1, cursor.getString(id_picture)));
            }
        }

        mListAdapter = new ProductAdapter(this, itemList,menuCase);
        mListView.setAdapter(mListAdapter);



        cursor.close();
        db.close();
    }

    // Override the back button to do nothing
    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_extra, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.record_list_go_back:
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return true;
               */

            case R.id.action_admin:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivityForResult(mainIntent,1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getQueryRemoveItemUpdate(String name){
        String newName = "%" + name + "%";
        String update = ERSList.ItemEntry.TABLE;
        String set = ERSList.ItemEntry.COL_name +" = "+
                "'"+newName+"'";
        String where = ERSList.ItemEntry.COL_name +" = "+ "'"+name+"'";
        String query = " UPDATE " + update + " SET " + set + " WHERE " + where;
        return query;
    }
    public String getQueryReturnItemUpdate(String name){
        String newName = name.substring(1,name.length()-1);
        String update = ERSList.ItemEntry.TABLE;
        String set = ERSList.ItemEntry.COL_name +" = "+
                "'"+newName+"'";
        String where = ERSList.ItemEntry.COL_name +" = "+ "'"+name+"'";
        String query = " UPDATE " + update + " SET " + set + " WHERE " + where;
        return query;
    }


}