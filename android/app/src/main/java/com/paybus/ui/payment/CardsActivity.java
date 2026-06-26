package com.paybus.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.paybus.R;
import com.paybus.adapter.CardAdapter;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.CardResponse;
import com.paybus.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardsActivity extends AppCompatActivity {

    private RecyclerView rvCards;
    private MaterialButton btnAddCard;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCards = findViewById(R.id.rvCards);
        rvCards.setLayoutManager(new LinearLayoutManager(this));

        btnAddCard = findViewById(R.id.btnAddCard);
        btnAddCard.setOnClickListener(v ->
                startActivity(new Intent(this, AddCardActivity.class)));

        loadCards();
    }

    private void loadCards() {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getCards(token).enqueue(new Callback<List<CardResponse>>() {
            @Override
            public void onResponse(Call<List<CardResponse>> call, Response<List<CardResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rvCards.setAdapter(new CardAdapter(response.body()));
                }
            }

            @Override
            public void onFailure(Call<List<CardResponse>> call, Throwable t) {
                Toast.makeText(CardsActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
