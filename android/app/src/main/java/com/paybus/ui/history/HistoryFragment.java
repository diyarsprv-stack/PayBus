package com.paybus.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.adapter.ReceiptAdapter;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.TransactionResponse;
import com.paybus.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TextView tvEmpty;
    private SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        session = new SessionManager(requireContext());
        rvHistory = view.findViewById(R.id.rvHistory);
        tvEmpty = view.findViewById(R.id.tvEmptyHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        loadHistory();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getTransactions(token).enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call,
                                   Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    rvHistory.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    rvHistory.setAdapter(new ReceiptAdapter(response.body()));
                } else {
                    rvHistory.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
