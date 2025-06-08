package com.example.moneyware;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SpecialReceiver extends BroadcastReceiver {
    Context fcontext;

    public SpecialReceiver(Context fcontext) {
        this.fcontext = fcontext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!LoginActivity.isNetworkAvailable(context)) {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
            ((Activity) fcontext).finish();
        }

    }
}
