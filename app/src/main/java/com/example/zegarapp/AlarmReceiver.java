package com.example.zegarapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isAlarmEnabled = intent.getBooleanExtra("isEnabled", true);

        if (!isAlarmEnabled) {
            Log.d("AlarmReceiver", "Alarm został wyłączony, nie dzwoni!");
            return;
        }

        Toast.makeText(context, "Alarm!", Toast.LENGTH_SHORT).show();

        Intent stopIntent = new Intent(context, StopAlarmActivity.class);
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(stopIntent);
    }

}