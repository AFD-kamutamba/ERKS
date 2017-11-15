package afd.ers.analyse;

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

import afd.ers.R;

public class TopSellAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private ArrayList<ProfitItem> objects;

    private class ViewHolder {
        TextView textViewName;
        TextView textViewProfit;
        ImageView imageViewPicture;
    }
    public TopSellAdapter(Context context, ArrayList<ProfitItem> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int i) {
            return objects.get(i);
    }

    @Override
    public long getItemId(int i) {
            return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.profit_item, null);
            holder.textViewName = (TextView) convertView.findViewById(R.id.item_name);
            holder.textViewProfit = (TextView) convertView.findViewById(R.id.item_profit);
            holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.item_picture);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textViewName.setText(objects.get(i).getName());
        holder.textViewProfit.setText(String.valueOf(objects.get(i).getProfit()));
        holder.imageViewPicture.setImageURI(Uri.fromFile(new File(objects.get(i).getPicture())));
        return convertView;
    }

}
