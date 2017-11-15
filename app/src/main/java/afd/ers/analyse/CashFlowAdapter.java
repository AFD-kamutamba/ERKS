package afd.ers.analyse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import afd.ers.R;

/**
 * Created by Andreas on 18/07/2017.
 */

public class CashFlowAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<CashFlowItem> objects;

    private class ViewHolder {
        TextView textViewName;
        TextView textViewAmount;
        TextView textViewRevenue;
    }

    public CashFlowAdapter(Context context, ArrayList<CashFlowItem> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public CashFlowItem getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.cash_flow_item, null);
            holder.textViewName = (TextView) convertView.findViewById(R.id.item_name);
            holder.textViewAmount = (TextView) convertView.findViewById(R.id.item_amount);
            holder.textViewRevenue = (TextView) convertView.findViewById(R.id.item_revenue);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textViewName.setText(objects.get(position).getName());
        holder.textViewAmount.setText(objects.get(position).getAmount());
        holder.textViewRevenue.setText(objects.get(position).getRevenue());
        return convertView;
    }
}