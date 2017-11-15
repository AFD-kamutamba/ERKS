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

public class EmployeeAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<Employee> objects;

    private class ViewHolder {
        TextView textViewName;
        TextView textViewPassword;
    }

    public EmployeeAdapter(Context context, ArrayList<Employee> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public Employee getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.employee_item, null);
            holder.textViewName = (TextView) convertView.findViewById(R.id.employee_name);
            holder.textViewPassword = (TextView) convertView.findViewById(R.id.employee_password);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textViewName.setText(objects.get(position).getEmployeeName());
        holder.textViewPassword.setText(objects.get(position).getEmployeePassword());

        return convertView;
    }
}