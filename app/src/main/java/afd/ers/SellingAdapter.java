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

public class SellingAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<StockItem> objects;
    private int menuCase;

    private class ViewHolder {
        TextView textViewCat;
        TextView textViewName;
        TextView textViewAmount;
        ImageView imageViewPicture;
        TextView textViewID;
    }

    public SellingAdapter(Context context, ArrayList<StockItem> objects, int i) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
        this.menuCase = i;
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
            convertView = inflater.inflate(R.layout.selling_item, null);

            switch (menuCase) {
                case 1:
                    convertView.setBackgroundResource(R.drawable.item_border);
                    break;
                case 2:
                    convertView.setBackgroundResource(R.drawable.item_border2);
                    break;
                case 3:
                    convertView.setBackgroundResource(R.drawable.item_border3);
                    break;
                case 4:
                    convertView.setBackgroundResource(R.drawable.item_border4);
                    break;
                case 5:
                    convertView.setBackgroundResource(R.drawable.item_border5);
                    break;
                case 6:
                    convertView.setBackgroundResource(R.drawable.item_border6);
                    break;
                case 7:
                    convertView.setBackgroundResource(R.drawable.item_border7);
                    break;
            }


            holder.textViewCat = (TextView) convertView.findViewById(R.id.item_cat);
            holder.textViewName = (TextView) convertView.findViewById(R.id.item_title);
            holder.textViewAmount = (TextView) convertView.findViewById(R.id.item_price);
            holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.item_picture);
            holder.textViewID = (TextView) convertView.findViewById(R.id.item_ID);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textViewCat.setText(objects.get(position).getCategory());
        holder.textViewName.setText(objects.get(position).getName());
        holder.textViewAmount.setText(String.valueOf(objects.get(position).getPrice()));
        holder.imageViewPicture.setImageURI(Uri.fromFile(new File(objects.get(position).getPicture())));
        holder.textViewID.setText(String.valueOf(objects.get(position).getID()));

        return convertView;
    }
}