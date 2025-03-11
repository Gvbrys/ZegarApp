package com.example.zegarapp;

public class AlarmModel {
    private String time;
    private boolean isEnabled;

    public AlarmModel(String time, boolean isEnabled) {
        this.time = time;
        this.isEnabled = isEnabled;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getHour() {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]);
    }

    public int getMinute() {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[1]);
    }
}
