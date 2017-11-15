package afd.ers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import afd.ers.db.ERSList;
import afd.ers.db.mDbHelper;

public class RecordAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<Transaction> objects;
    private mDbHelper mHelper;

    private class ViewHolder {
        TextView textViewRecordID;
        TextView textViewName;
        TextView textViewPrice;
        TextView textViewAmount;
        TextView textViewDate;
        ImageView imageViewPicture;
    }

    public RecordAdapter(Context context, ArrayList<Transaction> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
        mHelper = new mDbHelper(context);
    }

    public int getCount() {
        return objects.size();
    }

    public Transaction getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.record_item, null);
            holder.textViewRecordID = (TextView) convertView.findViewById(R.id.record_id);
            holder.textViewName = (TextView) convertView.findViewById(R.id.item_name);
            holder.textViewPrice = (TextView) convertView.findViewById(R.id.item_price);
            holder.textViewAmount = (TextView) convertView.findViewById(R.id.item_amount_record);
            holder.textViewDate = (TextView) convertView.findViewById(R.id.item_date);
            holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.item_picture);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();

        final String task = String.valueOf(objects.get(position).getItemID());

        Cursor cursor = db.query(ERSList.ItemEntry.TABLE,
                new String[]{ ERSList.ItemEntry.COL_name, ERSList.ItemEntry.COL_picture}, ERSList.ItemEntry._ID + " = ?",
                new String[] {task}, null, null, null, null);

        if (cursor.moveToNext()) {
            int id_name = cursor.getColumnIndex(ERSList.ItemEntry.COL_name);
            holder.textViewName.setText(cursor.getString(id_name));

            int id_picture = cursor.getColumnIndex(ERSList.ItemEntry.COL_picture);
            holder.imageViewPicture.setImageURI(Uri.fromFile(new File(cursor.getString(id_picture))));
        }

        db.close();


        holder.textViewRecordID.setText(String.valueOf(objects.get(position).getRecordID()));
        holder.textViewPrice.setText(String.valueOf(Math.round(objects.get(position).getPrice()*objects.get(position).getAmount()*10)/(float)10));
        holder.textViewAmount.setText(String.valueOf(objects.get(position).getAmount()));

        SimpleDateFormat firstFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = firstFormat.parse(objects.get(position).getDate());
        } catch (ParseException exc) {

        }
        SimpleDateFormat secondFormat = new SimpleDateFormat("dd/MM/yyyy");
        holder.textViewDate.setText(secondFormat.format(date));
        return convertView;
    }
}