package com.example.moneyware;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseUser user= auth.getCurrentUser();
    DatabaseReference ref=database.getReference();
    Calendar calendar=Calendar.getInstance();
    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    String msg="";


    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        //acct!=null; The app will not perform further execution if the app is not logged in
            if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")&&acct!=null) {
                Bundle bundle = intent.getExtras();
                Object[] smsObj = (Object[]) bundle.get("pdus");
                for (Object obj : smsObj) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
                    msg = msg+smsMessage.getDisplayMessageBody();
                }
                extract(msg,context);
            }
    }

    private void extract(String msg,Context context) {
        //Message Filtering using Regex
        Pattern amtpattern= Pattern.compile("A/c .*\\d{4} debited.* (\\d+\\.\\d+)",Pattern.CASE_INSENSITIVE);
        Pattern datepattern = Pattern.compile("on.*((\\d{2}-(\\d{2}|[a-zA-Z]+)-\\d{2})|(\\d{2}[a-zA-Z]+\\d{2}))");
        Pattern merchantpattern=Pattern.compile("to (.*)(UPI|Refno)",Pattern.CASE_INSENSITIVE);
        Matcher datematcher=datepattern.matcher(msg);
        Matcher merchantmatcher=merchantpattern.matcher(msg);
        Matcher amtmatcher=amtpattern.matcher(msg);
        double amount;
        String date="";
        String merchant="";
        if(amtmatcher.find()&&datematcher.find()&&merchantmatcher.find()){
            amount= Double.parseDouble(amtmatcher.group(1));
            date=datematcher.group(1);
            merchant=merchantmatcher.group(1);
            HashMap<String,Object>map=new HashMap<>();
            map.put("Expense Name",merchant);
            map.put("Date",date);
            map.put("Amount",amount);
            String monthname=months[calendar.get(Calendar.MONTH)];
            String year= String.valueOf(calendar.get(Calendar.YEAR));
            DatabaseReference dref=ref.child("Users").child(user.getUid()).child("Current");
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(monthname)){
                        ref.child("Users").child(user.getUid()).child("Current").child(monthname).child("Expenses").push().setValue(map);
                    }
                    if(snapshot.hasChild(year)){
                        ref.child("Users").child(user.getUid()).child("Current").child(year).child("Expenses").push().setValue(map);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }
}
