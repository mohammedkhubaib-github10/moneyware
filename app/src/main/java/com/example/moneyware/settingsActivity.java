package com.example.moneyware;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class settingsActivity extends AppCompatActivity {
    ImageButton settingsback;
    Button bin;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsback = findViewById(R.id.settingsback);
        bin = findViewById(R.id.bin);
        database = FirebaseDatabase.getInstance();
        settingsback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(settingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void deleteAll(View v1) {
        if (LoginActivity.isNetworkAvailable(settingsActivity.this)) {
            if (MainActivity.ispossible) {
                AlertDialog.Builder builder = new AlertDialog.Builder(settingsActivity.this);
                View v = getLayoutInflater().inflate(R.layout.deletedialog, null);
                builder.setView(v);
                Button no = v.findViewById(R.id.no);
                Button yes = v.findViewById(R.id.yes);
                TextView msg = v.findViewById(R.id.textView8);
                msg.setText("Are You Sure You Want To Delete All Permanently");
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
                        database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Budgets").removeValue();
                        database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Current").removeValue();
                        Toast.makeText(settingsActivity.this, "Deleted All", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
                Toast.makeText(settingsActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
