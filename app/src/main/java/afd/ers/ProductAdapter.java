package afd.ers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ProductAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<StockItem> objects;
    private int menuCase;
    Drawable returnIcon;
    Drawable removeIcon;

    private class ViewHolder {
        TextView textViewID;
        TextView textViewName;
        TextView textViewPrice;
        ImageView imageViewPicture;
        Button buttonRemoveReturn;
        Button buttonUpdateHardRemove;
    }

    public ProductAdapter(Context context, ArrayList<StockItem> objects, int i) {
        inflater = LayoutInflater.from(context);
        returnIcon = context.getResources().getDrawable(R.drawable.refresh_icon);
        removeIcon = context.getResources().getDrawable(R.drawable.cancel_admin_popup_icon);
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
            convertView = inflater.inflate(R.layout.product_item, null);

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

            holder.textViewID = (TextView) convertView.findViewById(R.id.item_id);
            holder.textViewName = (TextView) convertView.findViewById(R.id.item_title);
            holder.textViewPrice = (TextView) convertView.findViewById(R.id.item_price);
            holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.picture_item);
            holder.buttonRemoveReturn = (Button) convertView.findViewById(R.id.item_delete);
            holder.buttonUpdateHardRemove = (Button) convertView.findViewById(R.id.item_update);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.buttonRemoveReturn.setText("hide");
        holder.buttonRemoveReturn.setCompoundDrawablesWithIntrinsicBounds(null,null,removeIcon,null);
        holder.buttonUpdateHardRemove.setText("update");
        holder.buttonUpdateHardRemove.setCompoundDrawablesWithIntrinsicBounds(null,null,returnIcon,null);
        if (objects.get(position).getName().endsWith("%") == true) {
            holder.buttonRemoveReturn.setText("return");
            holder.buttonRemoveReturn.setCompoundDrawablesWithIntrinsicBounds(null,null,returnIcon,null);
            holder.buttonUpdateHardRemove.setText("discard");
            holder.buttonUpdateHardRemove.setCompoundDrawablesWithIntrinsicBounds(null,null,removeIcon,null);
        }

        holder.textViewID.setText(String.valueOf(objects.get(position).getID()));
        holder.textViewName.setText(objects.get(position).getName());
        holder.textViewPrice.setText(String.valueOf(objects.get(position).getPrice()));
        holder.imageViewPicture.setImageURI(Uri.fromFile(new File(objects.get(position).getPicture())));
        return convertView;
    }
}