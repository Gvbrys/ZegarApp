package com.example.zegarapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MaterialButton addAlarmButton;
    private RecyclerView alarmsRecyclerView;
    private AlarmAdapter alarmAdapter;
    private TextView timeTextView;
    private TextView alarmStatusTextView;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<AlarmModel> alarmList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addAlarmButton = findViewById(R.id.addAlarmButton);
        alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView);
        timeTextView = findViewById(R.id.timeTextView);
        alarmStatusTextView = findViewById(R.id.alarmStatus);

        startClockUpdate();

        loadAlarms();

        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        alarmAdapter = new AlarmAdapter(alarmList,
                position -> {
                    alarmList.remove(position);
                    saveAlarms();
                    alarmAdapter.notifyItemRemoved(position);
                    toggleNoAlarmsText();
                },
                (position, isEnabled) -> {

                    onSwitchChanged(position, isEnabled);
                });

        alarmsRecyclerView.setAdapter(alarmAdapter);

        addAlarmButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SetAlarmActivity.class);
            startActivityForResult(intent, 1);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        toggleNoAlarmsText();
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String time = data.getStringExtra("alarm_time");

            if (time != null) {
                Log.d("MainActivity", "Otrzymano nowy alarm: " + time);
                alarmList.add(new AlarmModel(time, true));
                saveAlarms();

                if (alarmAdapter != null) {
                    alarmAdapter.notifyDataSetChanged();
                } else {
                    Log.e("MainActivity", "alarmAdapter jest null!");
                }

                toggleNoAlarmsText();
                Toast.makeText(this, "Alarm ustawiony na " + time, Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Nie otrzymano poprawnego czasu alarmu.");
            }
        } else {
            Log.d("MainActivity", "onActivityResult - brak poprawnych danych.");
        }
    }

    private void toggleNoAlarmsText() {
        if (!alarmList.isEmpty()) {
            alarmStatusTextView.setVisibility(View.GONE);
        } else {
            alarmStatusTextView.setVisibility(View.VISIBLE);
        }
    }

    private void saveAlarms() {
        SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        StringBuilder alarmsString = new StringBuilder();

        for (AlarmModel alarm : alarmList) {
            alarmsString.append(alarm.getTime()).append(",").append(alarm.isEnabled()).append(";");
        }

        editor.putString("alarms", alarmsString.toString());
        editor.apply();

        Log.d("MainActivity", "Zapisane alarmy: " + alarmsString);
    }

    private void loadAlarms() {
        SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        String alarmsString = prefs.getString("alarms", "");
        alarmList.clear();

        if (!alarmsString.isEmpty()) {
            String[] alarmArray = alarmsString.split(";");
            for (String alarmData : alarmArray) {
                String[] data = alarmData.split(",");
                if (data.length >= 2) {
                    alarmList.add(new AlarmModel(data[0], Boolean.parseBoolean(data[1])));
                }
            }
        }

        Log.d("MainActivity", "Wczytane alarmy: " + alarmsString);
    }


    private void startClockUpdate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateClock();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        timeTextView.setText(currentTime);
    }

    public void cancelAlarm(int requestCode) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d("MainActivity", "Alarm anulowany: " + requestCode);
        } else {
            Log.e("MainActivity", "AlarmManager jest null. Nie można anulować alarmu.");
        }
    }


    public void setAlarm(AlarmModel alarm, int requestCode) {
        if (!alarm.isEnabled()) {

            Log.d("MainActivity", "Alarm wyłączony. Nie ustawiamy alarmu.");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Brak uprawnień do dokładnych alarmów!", Toast.LENGTH_LONG).show();
                Intent intentSettings = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intentSettings);
                return;
            }
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.d("MainActivity", "Alarm ustawiony na: " + alarm.getTime());
    }

    private void onSwitchChanged(int position, boolean isChecked) {
        AlarmModel alarm = alarmList.get(position);
        alarm.setEnabled(isChecked);
        saveAlarms();

        if (isChecked) {
            Log.d("MainActivity", "Włączono alarm na pozycji: " + position);
            setAlarm(alarm, position);
        } else {
            Log.d("MainActivity", "Wyłączono alarm na pozycji: " + position);
            cancelAlarm(position);
        }
    }

}
