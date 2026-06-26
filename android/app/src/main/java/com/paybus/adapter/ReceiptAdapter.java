package com.paybus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.models.TransactionResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ViewHolder> {

    private List<TransactionResponse> transactions;
    private int counter = 0;

    public ReceiptAdapter(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TransactionResponse t = transactions.get(position);
        counter++;

        holder.tvRoute.setText("№ " + (t.bus_route != null ? t.bus_route : "Noma'lum"));
        holder.tvProvider.setText(t.provider.toUpperCase());
        holder.tvAmount.setText(String.format("%,d", (int) t.amount) + " so'm");
        holder.tvReceiptId.setText("#TRX" + String.format("%03d", counter));

        if (t.created_at != null) {
            try {
                String dateStr = t.created_at;
                holder.tvDate.setText(dateStr);
            } catch (Exception e) {
                holder.tvDate.setText(t.created_at);
            }
        }

        if ("success".equals(t.status)) {
            holder.tvStatus.setText("\u2705 Muvaffaqiyatli");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(com.paybus.R.color.success));
        } else {
            holder.tvStatus.setText("\u274C " + t.status);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(com.paybus.R.color.error));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoute, tvProvider, tvAmount, tvDate, tvStatus, tvReceiptId;

        ViewHolder(View view) {
            super(view);
            tvRoute = view.findViewById(R.id.tvReceiptRoute);
            tvProvider = view.findViewById(R.id.tvReceiptProvider);
            tvAmount = view.findViewById(R.id.tvReceiptAmount);
            tvDate = view.findViewById(R.id.tvReceiptDate);
            tvStatus = view.findViewById(R.id.tvReceiptStatus);
            tvReceiptId = view.findViewById(R.id.tvReceiptId);
        }
    }
}
