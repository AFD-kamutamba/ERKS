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

public class StockAnalyseAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<AnalyseStockItem> objects;
    private int menuCase;

    private class ViewHolder {
        TextView textViewName;
        TextView textViewStock;
        TextView textViewProposedAmount;
        TextView textViewBuy;
        ImageView imageViewPicture;
    }

    public StockAnalyseAdapter(Context context, ArrayList<AnalyseStockItem> objects, int i) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
        this.menuCase = i;
    }

    public int getCount() {
        return objects.size();
    }

    public AnalyseStockItem getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.stock_analyse_item, null);

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


            holder.textViewName = (TextView) convertView.findViewById(R.id.item_title);
            holder.textViewStock = (TextView) convertView.findViewById(R.id.current_stock);
            holder.textViewProposedAmount = (TextView) convertView.findViewById(R.id.proposed_amount_stock);
            holder.textViewBuy = (TextView) convertView.findViewById(R.id.buy_stock);
            holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.item_picture);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textViewName.setText(objects.get(position).getName());
        holder.textViewStock.setText(objects.get(position).getStock());
        holder.textViewProposedAmount.setText(objects.get(position).getProposedAmountADay());
        holder.textViewBuy.setText(objects.get(position).getAmountBuy());
        holder.imageViewPicture.setImageURI(Uri.fromFile(new File(objects.get(position).getPicture())));
        return convertView;
    }
}