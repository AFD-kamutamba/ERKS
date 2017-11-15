package afd.ers;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class EmployeeClockinAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<ClockIn> objects;

    private class ViewHolder {
        TextView textViewName;
        TextView textViewDate;
        TextView textViewStartHour;
        TextView textViewEndHour;
        TextView textViewStartMoney;
        TextView textViewEndMoney;
        TextView textViewDifference;
    }

    public EmployeeClockinAdapter(Context context, ArrayList<ClockIn> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public ClockIn getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.employee_clockin_item, null);

            holder.textViewName = (TextView) convertView.findViewById(R.id.employee_name_clockin);
            holder.textViewDate = (TextView) convertView.findViewById(R.id.employee_date_clockin);
            holder.textViewStartHour = (TextView) convertView.findViewById(R.id.start_hour_clockin);
            holder.textViewEndHour = (TextView) convertView.findViewById(R.id.end_hour_clockin);
            holder.textViewStartMoney = (TextView) convertView.findViewById(R.id.start_money_clockin);
            holder.textViewEndMoney = (TextView) convertView.findViewById(R.id.end_money_clockin);
            holder.textViewDifference = (TextView) convertView.findViewById(R.id.difference_money);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textViewName.setText(objects.get(position).getClockInName());
        holder.textViewDate.setText(objects.get(position).getClockInDate());
        holder.textViewStartHour.setText(String.valueOf(objects.get(position).getClockInStartTime()));
        holder.textViewEndHour.setText(String.valueOf(objects.get(position).getClockInEndTime()));
        holder.textViewStartMoney.setText(String.valueOf(objects.get(position).getClockInStartMoney()));
        holder.textViewEndMoney.setText(String.valueOf(objects.get(position).getClockInEndMoney()));
        holder.textViewDifference.setText(String.valueOf(objects.get(position).getDifference()));
        if(objects.get(position).getClockInDiscrepancy()){
            holder.textViewStartMoney.setTextColor(Color.RED);
        }else {
            holder.textViewStartMoney.setTextColor(Color.BLACK);
        }

        return convertView;
    }
}