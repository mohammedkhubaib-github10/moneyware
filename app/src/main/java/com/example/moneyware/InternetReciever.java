package com.example.moneyware;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InternetReciever extends BroadcastReceiver {
    Context fcontext;

    public InternetReciever(Context fcontext) {
        this.fcontext = fcontext;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.networkdialog, null);
        builder.setView(v);
        AppCompatButton retry = v.findViewById(R.id.retry);
        AppCompatButton close = v.findViewById(R.id.close);
        AlertDialog dialog = builder.create();
        if (!LoginActivity.isNetworkAvailable(context)) {  //Internet Not Connected
            dialog.setCancelable(false);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) fcontext).finishAffinity();
                } //finishAffinity is used to destroy all the activities in the stack
            });
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LoginActivity.isNetworkAvailable(context)) {
                        dialog.dismiss();
                    }

                }
            });
            dialog.show();
        }
    }


}
