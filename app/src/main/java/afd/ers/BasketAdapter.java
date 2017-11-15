package afd.ers;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class BasketAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<StockItem> objects;

    private class ViewHolder {
        TextView textViewID;
        TextView textViewCat;
        TextView textViewName;
        TextView textViewPrice;
        TextView textViewAmount;
        ImageView imageViewProduct;
    }

    public BasketAdapter(Context context, ArrayList<StockItem> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public StockItem getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.basket_item, null);
//            holder.textViewID = (TextView) convertView.findViewById(R.id.item_id);
//            holder.textViewCat = (TextView) convertView.findViewById(R.id.item_cat);
            holder.textViewName = (TextView) convertView.findViewById(R.id.item_title);
            holder.textViewPrice = (TextView) convertView.findViewById(R.id.item_price);
            holder.textViewAmount = (TextView) convertView.findViewById(R.id.item_amount);
            holder.imageViewProduct = (ImageView) convertView.findViewById(R.id.item_picture);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        holder.textViewID.setText(String.valueOf(objects.get(position).getID()));
//        holder.textViewCat.setText(objects.get(position).getCategory());
        holder.textViewName.setText(objects.get(position).getName());
        holder.textViewPrice.setText("K "+String.valueOf(objects.get(position).getPrice()));
        //holder.textViewAmount.setText("X "+String.valueOf(objects.get(position).getAmount()));
        holder.textViewAmount.setText(String.valueOf(objects.get(position).getAmount()));
        holder.imageViewProduct.setImageURI(Uri.fromFile(new File(objects.get(position).getPicture())));

        return convertView;
    }
}