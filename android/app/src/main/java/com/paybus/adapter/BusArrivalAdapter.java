package com.paybus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;

import java.util.List;

public class BusArrivalAdapter extends RecyclerView.Adapter<BusArrivalAdapter.ViewHolder> {

    private List<BusArrival> arrivals;
    private OnBusClickListener listener;

    public interface OnBusClickListener {
        void onBusClick(BusArrival bus);
    }

    public static class BusArrival {
        public String route;
        public String destination;
        public String arrivalTime;

        public BusArrival(String route, String destination, String arrivalTime) {
            this.route = route;
            this.destination = destination;
            this.arrivalTime = arrivalTime;
        }
    }

    public BusArrivalAdapter(List<BusArrival> arrivals, OnBusClickListener listener) {
        this.arrivals = arrivals;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_arrival, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BusArrival bus = arrivals.get(position);
        holder.tvRouteNum.setText(bus.route);
        holder.tvRoute.setText("№ " + bus.route + " " + bus.destination);
        holder.tvArrivalTime.setText(bus.arrivalTime);
        holder.itemView.setOnClickListener(v -> listener.onBusClick(bus));
    }

    @Override
    public int getItemCount() {
        return arrivals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteNum, tvRoute, tvArrivalTime;

        ViewHolder(View view) {
            super(view);
            tvRouteNum = view.findViewById(R.id.tvBusRouteNum);
            tvRoute = view.findViewById(R.id.tvBusRoute);
            tvArrivalTime = view.findViewById(R.id.tvArrivalTime);
        }
    }
}
