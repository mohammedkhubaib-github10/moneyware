package com.example.moneyware;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class addExpense extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseAuth auth;
    Button expadd, cancel;
    EditText date, expamt, expname;
    final Calendar calendar = Calendar.getInstance();
    boolean isedit;
    String Bkey, Ekey;
    SpecialReceiver specialReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        specialReceiver = new SpecialReceiver(this);
        //pop up window:
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .5));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
        //pop window code ends;
        expadd = findViewById(R.id.expadd);
        cancel = findViewById(R.id.cancel);
        date = findViewById(R.id.date);
        expamt = findViewById(R.id.expamt);
        expname = findViewById(R.id.expname);
        date.setEnabled(false);
        Bkey = getIntent().getStringExtra("Bkey");
        Ekey = getIntent().getStringExtra("Ekey");
        isedit = getIntent().getBooleanExtra("isedit", false);
        String type=getIntent().getStringExtra("Type");
        if (isedit) {
            TextView textView7 = findViewById(R.id.textView7);
            textView7.setText("Edit");
            String name = getIntent().getStringExtra("name");
            String amt = String.valueOf(getIntent().getDoubleExtra("amt", 0.0));
            String expdate = getIntent().getStringExtra("date");
            expname.setText(name);
            expamt.setText(amt);
            date.setText(expdate);
            expadd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!emptyfield()) {
                        String newexpname = expname.getText().toString();
                        String newexpamt = expamt.getText().toString();
                        String newdate = date.getText().toString();
                        edit(newexpname, newdate, Double.parseDouble(newexpamt),type);
                        finish();
                    }
                }
            });
        } else {
            expadd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!emptyfield()) {
                        String expstr = expname.getText().toString();
                        String datestr = date.getText().toString();
                        double amount = Double.parseDouble(expamt.getText().toString());
                        addExp(expstr, datestr, amount,type);
                        Toast.makeText(addExpense.this, "Expense Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean emptyfield() {
        if (TextUtils.isEmpty(date.getText())) {
            Toast.makeText(addExpense.this, "Select a Date", Toast.LENGTH_SHORT).show();
            return true;
        } else if (TextUtils.isEmpty(expname.getText())) {
            Toast.makeText(addExpense.this, "Missing Expense Name", Toast.LENGTH_SHORT).show();
            return true;
        } else if (TextUtils.isEmpty(expamt.getText())) {
            Toast.makeText(addExpense.this, "Missing Amount", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    //Program to add the data into the firebase
    private void addExp(String expstr, String datestr, double amount,String type) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Expense Name", expstr);
        map.put("Date", datestr);
        map.put("Amount", amount);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        database.getReference().child("Users").child(user.getUid()).child(type).child(Bkey).child("Expenses").push().setValue(map);
    }
    //Program to add the data into the firebase terminates here

    //Program for Editing the data in the firebase
    private void edit(String expstr, String datestr, double amount,String type) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Expense Name", expstr);
        map.put("Date", datestr);
        map.put("Amount", amount);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        database.getReference().child("Users").child(user.getUid()).child(type).child(Bkey).child("Expenses").child(Ekey).updateChildren(map);
        Toast.makeText(this, "Edited Successfully ", Toast.LENGTH_SHORT).show();
    }
//Program for Editing the data in the firebase terminates here

    //Date Picker code:
    public void openCalender(View v) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                int realmonth = month + 1;
                date.setText(dayOfMonth + "-" + realmonth + "-" + year);

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    //date picker code ends;

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(specialReceiver, intentFilter);
        super.onStart();
    }


    @Override
    protected void onStop() {
        unregisterReceiver(specialReceiver);
        super.onStop();
    }
}