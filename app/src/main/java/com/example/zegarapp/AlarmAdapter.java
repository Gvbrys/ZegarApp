package com.example.zegarapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private final List<AlarmModel> alarmList;
    private final OnAlarmDeleteListener deleteListener;
    private final OnAlarmToggleListener toggleListener;

    public interface OnAlarmDeleteListener {
        void onAlarmDeleted(int position);
    }

    public interface OnAlarmToggleListener {
        void onAlarmToggled(int position, boolean isEnabled);
    }

    public AlarmAdapter(List<AlarmModel> alarmList, OnAlarmDeleteListener deleteListener, OnAlarmToggleListener toggleListener) {
        this.alarmList = alarmList;
        this.deleteListener = deleteListener;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmModel alarm = alarmList.get(position);

        holder.alarmSwitch.setChecked(alarm.isEnabled());

        holder.alarmTimeText.setText(alarm.getTime());

        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleListener.onAlarmToggled(position, isChecked);
        });

        holder.deleteButton.setOnClickListener(v -> deleteListener.onAlarmDeleted(position));
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmTimeText;
        Switch alarmSwitch;
        ImageButton deleteButton;

        AlarmViewHolder(View itemView) {
            super(itemView);
            alarmTimeText = itemView.findViewById(R.id.alarmTimeText);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
