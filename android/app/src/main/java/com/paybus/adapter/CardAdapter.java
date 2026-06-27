package com.paybus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.models.CardResponse;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private List<CardResponse> cards;
    private Context context;

    public CardAdapter(List<CardResponse> cards) {
        this.cards = cards;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_custom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CardResponse card = cards.get(position);
        holder.tvProvider.setText(card.provider != null ? card.provider.toUpperCase() : "Noma'lum");
        holder.tvCardNumber.setText("**** " + (card.card_number != null ? card.card_number : "****"));

        if (card.is_default) {
            holder.tvDefault.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefault.setVisibility(View.GONE);
        }

        holder.btnDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvProvider, tvCardNumber, tvDefault;
        ImageButton btnDelete;

        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.ivCardIcon);
            tvProvider = view.findViewById(R.id.tvCardProvider);
            tvCardNumber = view.findViewById(R.id.tvCardNumber);
            tvDefault = view.findViewById(R.id.tvCardDefault);
            btnDelete = view.findViewById(R.id.btnDeleteCard);
        }
    }
}
