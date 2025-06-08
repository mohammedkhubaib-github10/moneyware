package com.example.moneyware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class addbudget extends AppCompatActivity {
    //Variable Initialization
    static boolean iscustomrb, iscurrentrb, ismonthlyrb, isyearlyrb;
    Button cancel, add;
    RadioGroup expensetype, budgettype;
    EditText editText1, editText2;
    String budgetname;
    double amount;
    FirebaseDatabase database;
    FirebaseAuth auth;
    boolean isedit;
    SpecialReceiver specialReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addbudget);
        specialReceiver = new SpecialReceiver(this);
        isedit = getIntent().getBooleanExtra("isedit", false); // To avoid creating new activity for edit
        //Pop up Window Code:
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
        //pop up Window code terminates
        cancel = findViewById(R.id.button2);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        add = findViewById(R.id.button);
        expensetype = (RadioGroup) findViewById(R.id.expensetype);
        budgettype = (RadioGroup) findViewById(R.id.budgettype);
        editText1 = findViewById(R.id.editTextText);
        editText2 = findViewById(R.id.editTextText2);
        iscustomrb = false;
        iscurrentrb = false;
        budgettype.getChildAt(0).setEnabled(false);
        budgettype.getChildAt(1).setEnabled(false);
        if (isedit) { //To avoid creating a new pop up window for edit
            String name = getIntent().getStringExtra("name");
            String amt = String.valueOf(getIntent().getDoubleExtra("amt", 0.0));
            String Bkey = getIntent().getStringExtra("Bkey");
            String totexp = String.valueOf(getIntent().getDoubleExtra("totexp", 0.0));
            String type=getIntent().getStringExtra("Type");
            expensetype.setVisibility(View.GONE);
            budgettype.setVisibility(View.GONE);
            editText1.setText(name);
            editText2.setText(amt);
            if(type.equals("Current")){
                editText1.setEnabled(false);
            }
            TextView textView7 = findViewById(R.id.textView7);
            TextView textView = findViewById(R.id.textView);
            textView.setVisibility(View.GONE);
            TextView textView2 = findViewById(R.id.textView2);
            textView2.setVisibility(View.GONE);
            textView7.setText("Edit");

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!emptyfield()) {
                        String newbname = editText1.getText().toString();
                        String newbamt = editText2.getText().toString();
                        uniqueness(newbname,type,new UniquenessChecker(){

                            @Override
                            public void uChecker(boolean isunique) {
                                if(isunique||newbname.equals(name)) {
                                    edit(newbname, Double.parseDouble(newbamt), Bkey, Double.parseDouble(totexp),type);
                                    finish();
                                }
                                else{
                                    Toast.makeText(addbudget.this, "Name Already Exists", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
            });
        } else {
            add.setEnabled(false);
            add.setAlpha(0.3f);
            expensetype.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.currentrb) {
                        add.setEnabled(false);
                        add.setAlpha(0.3f);
                        budgettype.getChildAt(0).setEnabled(true);
                        budgettype.getChildAt(1).setEnabled(true);
                        editText1.setEnabled(false);
                        editText1.getText().clear();
                        addbudget.iscurrentrb = true;
                        addbudget.iscustomrb = false;
                    } else if (checkedId == R.id.customrb) {
                        budgettype.getChildAt(0).setEnabled(false);
                        budgettype.getChildAt(1).setEnabled(false);
                        budgettype.clearCheck();
                        editText1.setEnabled(true);
                        addbudget.iscurrentrb = false;
                        addbudget.iscustomrb = true;
                        fieldcheck();
                    }
                }
            });
            budgettype.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.monthlyrb) {
                        addbudget.ismonthlyrb = true;
                        addbudget.isyearlyrb = false;
                        fieldcheck();
                    } else if (checkedId == R.id.yearlyrb) {
                        addbudget.isyearlyrb = true;
                        addbudget.ismonthlyrb = false;
                        fieldcheck();
                    }
                }
            });
        }
    }
    public interface UniquenessChecker{
        void uChecker(boolean isunique);
    }
  public boolean uniqueness(String name, String type, UniquenessChecker Listener){
        DatabaseReference ref=database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child(type);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean u=true;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Budgetname;
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Budgetname = dataSnapshot.child("Budget Name").getValue(String.class);
                    if(name.equals(Budgetname)){
                        u=false;
                        break;
                    }
                }
                Listener.uChecker(u);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true;
  }
    public void fieldcheck() {
        if ((iscurrentrb || iscustomrb)) {
            add.setEnabled(true);
            add.setAlpha(1.0f);
        }
    }

    public void add_budget(View v) {
        if (iscurrentrb) {
            if (TextUtils.isEmpty(editText2.getText())) {
                Toast.makeText(this, "Amount is Missing", Toast.LENGTH_SHORT).show();
            } else {
                Calendar calendar=Calendar.getInstance();
                double amt= Double.parseDouble(editText2.getText().toString());
                if (ismonthlyrb) {
                    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    String name=months[calendar.get(Calendar.MONTH)];
                    uniqueness(name, "Current", new UniquenessChecker() {
                        @Override
                        public void uChecker(boolean isunique) {
                            if(isunique) {
                                add(name, amt, "Current");
                                finish();
                            }
                            else{
                                Toast.makeText(addbudget.this, "Tracking of monthly expense for this month is already established", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else{
                   String name= String.valueOf(calendar.get(Calendar.YEAR));
                    uniqueness(name, "Current", new UniquenessChecker() {
                        @Override
                        public void uChecker(boolean isunique) {
                            if(isunique) {
                                add(name, amt, "Current");
                                finish();
                            }
                            else{
                                Toast.makeText(addbudget.this, "Tracking of yearly expense for this year is already established", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        } else{
            if (!emptyfield()) {
                budgetname = editText1.getText().toString();
                amount = Double.parseDouble(editText2.getText().toString());
                uniqueness(budgetname, "Budgets", new UniquenessChecker() {
                    @Override
                    public void uChecker(boolean isunique) {
                        if(isunique) {
                            add(budgetname, amount, "Budgets");
                            Toast.makeText(addbudget.this, "Custom Budget Added Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else{
                            Toast.makeText(addbudget.this, "Name Already Exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        }
    }

    private boolean emptyfield() {
        if (TextUtils.isEmpty(editText1.getText())) {
            Toast.makeText(this, "Name is Missing", Toast.LENGTH_SHORT).show();
            return true;
        } else if (TextUtils.isEmpty(editText2.getText())) {
            Toast.makeText(this, "Amount is Missing", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    //Program to add the data into database
    private void add(String budgetname, double amount,String type) {
        double totexp = 0;
        double bal = amount - totexp;
        FirebaseUser user = auth.getCurrentUser();
        HashMap<String, Object> map = new HashMap<>();
        map.put("Budget Name", budgetname);
        map.put("Amount", amount);
        map.put("Total Expenses", totexp);
        map.put("Balance", bal);
        map.put("Type",type);
        if(type.equals("Current")){
            database.getReference().child("Users").child(user.getUid()).child(type).child(budgetname).setValue(map);
        }
        else {
            database.getReference().child("Users").child(user.getUid()).child(type).push().setValue(map);
        }
    }
    //program to add the data into database ends here

    //program to edit the data in the database
    private void edit(String newbname, double newbamt, String Bkey, double totexp,String type) {
        HashMap<String, Object> map = new HashMap<>();
        double bal = newbamt - totexp;
        map.put("Budget Name", newbname);
        map.put("Amount", newbamt);
        map.put("Balance", bal);
        FirebaseUser user = auth.getCurrentUser();
        database.getReference().child("Users").child(user.getUid()).child(type).child(Bkey).updateChildren(map);
        Toast.makeText(this, "Edited Successfully", Toast.LENGTH_SHORT).show();
    }
    //program to edit the data in the database

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