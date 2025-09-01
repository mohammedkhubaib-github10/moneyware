package com.example.moneyware.presentation.ui.common.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.moneyware.data.models.Expenses;
import com.example.moneyware.R;

import java.util.ArrayList;

public class CustomExpenseListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Expenses> e;
    LayoutInflater inflater;

    public interface MenuListener {
        void del(int position);

        void edit(int position);
    }

    MenuListener menuListener;

    public CustomExpenseListAdapter(Context context, ArrayList<Expenses> e, MenuListener menuListener) {
        this.context = context;
        this.e = e;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.menuListener = menuListener;
    }

    @Override
    public int getCount() {
        return e.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.customexpenselist, null);
        TextView exptext = view.findViewById(R.id.exptext);
        TextView expdate = view.findViewById(R.id.expdate);
        TextView expamt = view.findViewById(R.id.expamt);
        ImageButton options = view.findViewById(R.id.options);
        String expname = e.get(position).getExpense_name();
        String date = e.get(position).getDate();
        String amt = String.valueOf(e.get(position).getAmount());
        options.setImageResource(R.drawable.three_dots_vertical);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, options);
                popupMenu.getMenuInflater().inflate(R.menu.deleteandedit, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.del) {
                            menuListener.del(position);
                        } else if (item.getItemId() == R.id.edit) {
                            menuListener.edit(position);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        exptext.setText(expname);
        expdate.setText(date);
        expamt.setText(amt);
        return view;
    }
}
