package com.paybus.ui.transactions;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.paybus.R;
import com.paybus.adapter.TransactionAdapter;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.TransactionResponse;
import com.paybus.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvTransactions = findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        loadTransactions();
    }

    private void loadTransactions() {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getTransactions(token).enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rvTransactions.setAdapter(new TransactionAdapter(response.body()));
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                Toast.makeText(TransactionsActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
