package com.paybus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.models.BusRoute;

import java.util.List;

public class BusRouteAdapter extends RecyclerView.Adapter<BusRouteAdapter.ViewHolder> {

    private List<BusRoute> routes;
    private OnRouteClickListener listener;

    public interface OnRouteClickListener {
        void onRouteClick(BusRoute route);
    }

    public BusRouteAdapter(List<BusRoute> routes, OnRouteClickListener listener) {
        this.routes = routes;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BusRoute route = routes.get(position);
        holder.text1.setText(route.id + " - " + route.name);
        holder.text2.setText(route.price + " so'm");
        holder.itemView.setOnClickListener(v -> listener.onRouteClick(route));
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        ViewHolder(View view) {
            super(view);
            text1 = view.findViewById(android.R.id.text1);
            text2 = view.findViewById(android.R.id.text2);
        }
    }
}
