package com.paybus.ui.reminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.paybus.R;
import com.paybus.utils.ReminderManager;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private List<ReminderManager.Reminder> reminders;
    private ReminderManager manager;

    public ReminderAdapter(List<ReminderManager.Reminder> reminders, ReminderManager manager) {
        this.reminders = reminders;
        this.manager = manager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReminderManager.Reminder r = reminders.get(position);
        holder.tvTitle.setText("№ " + r.routeId + " \u2192 " + r.stopName);
        holder.tvSubtitle.setText(r.routeName);
        holder.switchAutoPay.setChecked(r.autoPay);

        holder.btnDelete.setOnClickListener(v -> {
            manager.removeReminder(r.id);
            reminders.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, reminders.size());
        });

        holder.switchAutoPay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            manager.toggleAutoPay(r.id, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageButton btnDelete;
        SwitchMaterial switchAutoPay;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvReminderTitle);
            tvSubtitle = view.findViewById(R.id.tvReminderSubtitle);
            btnDelete = view.findViewById(R.id.btnDeleteReminder);
            switchAutoPay = view.findViewById(R.id.switchAutoPay);
        }
    }
}
