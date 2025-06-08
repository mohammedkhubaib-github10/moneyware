package com.example.moneyware;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ExpenseActivity extends AppCompatActivity implements CustomExpenseListAdapter.MenuListener {
    //Variable Initialization
    TextView name;
    ImageButton back;
    FloatingActionButton add;
    ListView expenses;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String Bkey,type;
    DatabaseReference ref;
    ArrayList<Expenses> e;
    ProgressBar progressBar;
    TextView noreult, exp, bal;
    double totalexp;
    InternetReciever internetReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        //Varible Declarations
        String Budgetname = getIntent().getStringExtra("Name");
        double Budgetamount = getIntent().getDoubleExtra("amt", 0.0);
        type=getIntent().getStringExtra("Type");
        Bkey = getIntent().getStringExtra("BKey");
        name = findViewById(R.id.name);
        name.setText(Budgetname);
        back = findViewById(R.id.back);
        add = findViewById(R.id.add);
        progressBar = findViewById(R.id.progressBar);
        noreult = findViewById(R.id.noresult);
        exp = findViewById(R.id.exp);
        bal = findViewById(R.id.bal);
        internetReciever = new InternetReciever(this);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //Adding New Expenses
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add.setEnabled(false);
                Intent intent = new Intent(ExpenseActivity.this, addExpense.class);
                intent.putExtra("Bkey", Bkey);
                intent.putExtra("Type",type);
                startActivity(intent);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add.setEnabled(true);
                    } //To Prevent the Multiple Popup Windows to Pop up
                }, 1000);
            }
        });
        noreult.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        //Displaying the Expenses as ListView
        e = new ArrayList<>();
        CustomExpenseListAdapter adapter = new CustomExpenseListAdapter(getApplicationContext(), e, this);
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        //Data Retrieval code (from Firebase)
        ref = database.getReference().child("Users").child(user.getUid()).child(type).child(Bkey).child("Expenses");
        DatabaseReference bref = database.getReference().child("Users").child(user.getUid()).child(type).child(Bkey);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                e.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String expensename = dataSnapshot.child("Expense Name").getValue(String.class);
                    String date = dataSnapshot.child("Date").getValue(String.class);
                    double amount = dataSnapshot.child("Amount").getValue(Double.class);
                    String Ekey = dataSnapshot.getKey();
                    e.add(new Expenses(expensename, date, amount, Ekey));
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if (snapshot.getChildrenCount() == 0) {
                    noreult.setVisibility(View.VISIBLE);
                } else {
                    noreult.setVisibility(View.GONE);
                }
                totalexp = 0.0;
                for (Expenses texp : e) {
                    totalexp = totalexp + texp.getAmount();
                }
                double balance = Budgetamount - totalexp;
                bref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() != 0) { //While deleting the items this part of the code shouldn't be executed otherwise it will cause the application to crash
                            database.getReference().child("Users").child(user.getUid()).child(type).child(Bkey).child("Total Expenses").setValue(totalexp);
                            database.getReference().child("Users").child(user.getUid()).child(type).child(Bkey).child("Balance").setValue(balance);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                exp.setText(String.valueOf(totalexp));
                bal.setText(String.valueOf(balance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        expenses = findViewById(R.id.expenses);
        expenses.setAdapter(adapter);
        expenses.setSelector(android.R.color.transparent);//To make it look visually non-clickable
    }

    //Deleting the items from the database and listview
    @Override
    public void del(int position) {
        //custom Dialog box code
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.deletedialog, null);
        builder.setView(v);
        Button no = v.findViewById(R.id.no);
        Button yes = v.findViewById(R.id.yes);
        AlertDialog dialog = builder.create();
        //custom Dialog box code ends here;
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child(type).child(Bkey).child("Expenses").child(e.get(position).getEkey()).removeValue();
                Toast.makeText(ExpenseActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //Editing the Expenses
    @Override
    public void edit(int position) {
        Intent intent = new Intent(this, addExpense.class);
        intent.putExtra("isedit", true);
        intent.putExtra("name", e.get(position).getExpense_name());
        intent.putExtra("amt", e.get(position).getAmount());
        intent.putExtra("date", e.get(position).getDate());
        intent.putExtra("Bkey", Bkey);
        intent.putExtra("Ekey", e.get(position).getEkey());
        intent.putExtra("Type",type);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetReciever, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(internetReciever);
        super.onStop();
    }
}