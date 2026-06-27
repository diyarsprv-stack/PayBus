package com.paybus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.models.TransactionResponse;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionResponse> transactions;

    public TransactionAdapter(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_custom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TransactionResponse t = transactions.get(position);
        holder.tvProvider.setText(t.provider != null ? t.provider.toUpperCase() : "Noma'lum");
        holder.tvRoute.setText("Avtobus № " + (t.bus_route != null ? t.bus_route : "Noma'lum"));
        holder.tvAmount.setText((int) t.amount + " so'm");

        if ("success".equals(t.status)) {
            holder.tvStatus.setText("Muvaffaqiyatli");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.success));
        } else {
            holder.tvStatus.setText("Xatolik");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.error));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvProvider, tvRoute, tvAmount, tvStatus;

        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.ivTransactionIcon);
            tvProvider = view.findViewById(R.id.tvTransactionProvider);
            tvRoute = view.findViewById(R.id.tvTransactionRoute);
            tvAmount = view.findViewById(R.id.tvTransactionAmount);
            tvStatus = view.findViewById(R.id.tvTransactionStatus);
        }
    }
}
