package com.example.moneyware;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class NotificationActivity extends AppCompatActivity {
    ImageButton notificationback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        notificationback=findViewById(R.id.notificationback);
        notificationback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(NotificationActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}