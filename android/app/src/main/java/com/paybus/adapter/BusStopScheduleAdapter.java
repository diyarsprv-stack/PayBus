package com.paybus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.utils.ReminderManager;

import java.util.List;

public class BusStopScheduleAdapter extends RecyclerView.Adapter<BusStopScheduleAdapter.ViewHolder> {

    private List<StopSchedule> stops;
    private ReminderManager reminderManager;
    private String routeId;
    private String routeName;

    public static class StopSchedule {
        public String stopId;
        public String name;
        public String arrivalTime;

        public StopSchedule(String stopId, String name, String arrivalTime) {
            this.stopId = stopId;
            this.name = name;
            this.arrivalTime = arrivalTime;
        }
    }

    public BusStopScheduleAdapter(List<StopSchedule> stops, ReminderManager reminderManager,
                                  String routeId, String routeName) {
        this.stops = stops;
        this.reminderManager = reminderManager;
        this.routeId = routeId;
        this.routeName = routeName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_stop_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StopSchedule stop = stops.get(position);
        holder.tvStopName.setText(stop.name);
        holder.tvArrivalTime.setText(stop.arrivalTime);

        if (position == 0) {
            holder.vDot.setBackgroundResource(R.drawable.dot_start);
        } else if (position == stops.size() - 1) {
            holder.vDot.setBackgroundResource(R.drawable.dot_end);
        } else {
            holder.vDot.setBackgroundResource(R.drawable.dot_mid);
        }

        boolean hasReminder = reminderManager.hasReminder(routeId, stop.stopId);
        if (hasReminder) {
            holder.btnRemind.setImageResource(R.drawable.ic_bell);
            holder.btnRemind.setAlpha(1f);
        } else {
            holder.btnRemind.setImageResource(R.drawable.ic_bell);
            holder.btnRemind.setAlpha(0.4f);
        }

        holder.btnRemind.setOnClickListener(v -> {
            reminderManager.addReminder(routeId, routeName, stop.stopId, stop.name, stop.arrivalTime);
            holder.btnRemind.setAlpha(1f);
        });

        holder.btnAutoPay.setOnClickListener(v -> {
            if (reminderManager.hasReminder(routeId, stop.stopId)) {
                holder.btnAutoPay.setAlpha(1f);
            } else {
                reminderManager.addReminder(routeId, routeName, stop.stopId, stop.name, stop.arrivalTime);
                holder.btnRemind.setAlpha(1f);
                holder.btnAutoPay.setAlpha(1f);
            }
        });

        holder.itemView.setOnClickListener(v -> {
        });
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vDot;
        TextView tvStopName, tvArrivalTime;
        ImageButton btnRemind, btnAutoPay;

        ViewHolder(View view) {
            super(view);
            vDot = view.findViewById(R.id.vDot);
            tvStopName = view.findViewById(R.id.tvStopName);
            tvArrivalTime = view.findViewById(R.id.tvArrivalTime);
            btnRemind = view.findViewById(R.id.btnRemind);
            btnAutoPay = view.findViewById(R.id.btnAutoPay);
        }
    }
}
