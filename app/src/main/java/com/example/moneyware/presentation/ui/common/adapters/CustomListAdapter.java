package com.example.moneyware.presentation.ui.common.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.moneyware.data.models.Budgets;
import com.example.moneyware.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Budgets> budgets;
    LayoutInflater inflater;
    SparseBooleanArray selectedItems;
    onItemClickListener Listener;

    public interface onItemClickListener {
        void setItemClickListener(int position);

        void setMenuClickListener(int position, MenuItem item);
    }

    public CustomListAdapter(Context context, ArrayList<Budgets> budgets, onItemClickListener Listener) {
        this.context = context;
        this.budgets = budgets;
        this.Listener = Listener;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        return budgets.size();
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
        View view = inflater.inflate(R.layout.customlist, null);
        TextView budgetname = view.findViewById(R.id.budgetname);
        TextView amt = view.findViewById(R.id.amt);
        TextView bal = view.findViewById(R.id.bal);
        Budgets budget = budgets.get(position);
        budgetname.setText(budget.getBudgetname());
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        String at = decimalFormat.format(budget.getAmount());
        String balance = decimalFormat.format(budget.getBalance());
        bal.setText(balance);
        amt.setText(at);
        ImageView go = view.findViewById(R.id.go);
        ImageButton options = view.findViewById(R.id.options);
        options.setImageResource(R.drawable.three_dots);
        go.setImageResource(R.drawable.baseline_arrow_forward_ios_24);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Listener.setItemClickListener(position);
            }
        });
        //code for pop up menu
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, options);
                popupMenu.getMenuInflater().inflate(R.menu.deleteandedit, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Listener.setMenuClickListener(position, item);
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        // code for popup menu ends here
        return view;
    }
}
