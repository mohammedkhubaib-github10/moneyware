package com.example.moneyware;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import static androidx.core.content.SharedPreferencesKt.edit;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.RadioAccessSpecifier;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity implements CustomListAdapter.onItemClickListener {
    //Variable Initialization
    DrawerLayout drawerLayout;
    ImageView menu, search, notifcation, upropic;
    FloatingActionButton add;
    NavigationView nav;
    TextView uname;
    ListView budgets;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Budgets> b;
    DatabaseReference ref,ref2;
    ProgressBar progressBar;
    TextView noresult;
    public static boolean ispossible;
    InternetReciever internetReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        drawerLayout = findViewById(R.id.drawerlayout);
        menu = findViewById(R.id.menu);
        search = findViewById(R.id.search);
        notifcation = findViewById(R.id.notification);
        add = findViewById(R.id.add);
        nav = findViewById(R.id.nav);
        View headerview = nav.getHeaderView(0); //Setup for drawer layout
        uname = headerview.findViewById(R.id.uname);
        upropic = headerview.findViewById(R.id.upropic);
        progressBar = findViewById(R.id.progressBar);
        noresult = findViewById(R.id.noresult);
        internetReciever = new InternetReciever(this);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            Uri personPhoto = acct.getPhotoUrl();
            uname.setText(personName);
            Glide.with(this).load(personPhoto).circleCrop().into(upropic);
        }
        final String msg = "Coming Soon";
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        notifcation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,NotificationActivity.class);
                startActivity(intent);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add.setEnabled(false);
                Intent intent = new Intent(MainActivity.this, addbudget.class);
                startActivity(intent);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add.setEnabled(true);
                    }
                }, 1000);
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int option_id = item.getItemId();
                if (option_id == R.id.sap)
                    Toast.makeText(MainActivity.this, "Downloading as pdf", Toast.LENGTH_SHORT).show();
                if (option_id == R.id.sae)
                    Toast.makeText(MainActivity.this, "Downloading as excel", Toast.LENGTH_SHORT).show();
                if(option_id==R.id.settings){
                    Intent intent=new Intent(MainActivity.this,settingsActivity.class);
                    startActivity(intent);
                }
                if (option_id == R.id.lout) {
                    signOut();
                }
                return false;
            }
        });
        //Program for Retrieving the data from the database
        noresult.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        b = new ArrayList<>();
        CustomListAdapter adapter = new CustomListAdapter(getApplicationContext(), b, this);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Users").child(auth.getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                b.clear();
                for (DataSnapshot dataSnapshot : snapshot.child("Budgets").getChildren()) {
                    String Budgetname = dataSnapshot.child("Budget Name").getValue(String.class);
                    String key = dataSnapshot.getKey();
                    double amount = dataSnapshot.child("Amount").getValue(Double.class);
                    double balance = dataSnapshot.child("Balance").getValue(Double.class);
                    double totexp = dataSnapshot.child("Total Expenses").getValue(Double.class);
                    String type=dataSnapshot.child("Type").getValue(String.class);
                    b.add(new Budgets(Budgetname, amount, key, balance, totexp,type));
                }
                for(DataSnapshot dataSnapshot: snapshot.child("Current").getChildren()){
                    String Budgetname = dataSnapshot.child("Budget Name").getValue(String.class);
                    String key = dataSnapshot.getKey();
                    double amount = dataSnapshot.child("Amount").getValue(Double.class);
                    double balance = dataSnapshot.child("Balance").getValue(Double.class);
                    double totexp = dataSnapshot.child("Total Expenses").getValue(Double.class);
                    String type=dataSnapshot.child("Type").getValue(String.class);
                    b.add(new Budgets(Budgetname, amount, key, balance, totexp,type));
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if (snapshot.getChildrenCount() == 0) {
                    noresult.setVisibility(View.VISIBLE);
                    ispossible = false;
                } else {
                    noresult.setVisibility(View.GONE);
                    ispossible = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //Program Ends
        budgets = findViewById(R.id.budgets);
        budgets.setAdapter(adapter);
    }
    private void signOut() {
        auth.signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "signing out...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                        // ...
                    }
                });
    }

    //Event Handling for the items
    @Override
    public void setItemClickListener(int position) {
        String name = b.get(position).getBudgetname();
        double amt = b.get(position).getAmount();
        String Bkey = b.get(position).getBudgetKey();
        String type=b.get(position).getType();
        Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
        intent.putExtra("Name", name);
        intent.putExtra("BKey", Bkey);
        intent.putExtra("amt", amt);
        intent.putExtra("Type",type);
        startActivity(intent);
    }

    //Event Handling for the menu
    @Override
    public void setMenuClickListener(int position, MenuItem item) {
        if (item.getItemId() == R.id.del) {
            del(position);
        } else if (item.getItemId() == R.id.edit) {
            edit(position);
        }
    }

    //Editing the Budgets
    private void edit(int position) {
        Intent intent = new Intent(this, addbudget.class);
        intent.putExtra("isedit", true);
        intent.putExtra("name", b.get(position).getBudgetname());
        intent.putExtra("amt", b.get(position).getAmount());
        intent.putExtra("Bkey", b.get(position).getBudgetKey());
        intent.putExtra("totexp", b.get(position).getTotexp());
        intent.putExtra("Type",b.get(position).getType());
        startActivity(intent);
    }

    //Deleting Budgets from the firebase and the listview
    private void del(int position) {
        //custom Dialog box code;
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
                auth = FirebaseAuth.getInstance();
                database = FirebaseDatabase.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                database.getReference().child("Users").child(user.getUid()).child(b.get(position).getType()).child(b.get(position).getBudgetKey()).removeValue();
                Toast.makeText(MainActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //Exit Dialog
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.deletedialog, null);
        builder.setView(v);
        TextView title = v.findViewById(R.id.textView7);
        TextView msg = v.findViewById(R.id.textView8);
        title.setText("Exit");
        msg.setText("Are You Sure You Want To Exit?");
        msg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Button no = v.findViewById(R.id.no);
        Button yes = v.findViewById(R.id.yes);
        AlertDialog dialog = builder.create();
        //custom Dialog box code ends here;
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            } //finishAffinity is used to destroy all the activities in the stack
        });
        dialog.show();


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

