package com.paybus.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.paybus.R;
import com.paybus.adapter.BusRouteAdapter;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.BusRoute;
import com.paybus.ui.payment.CardsActivity;
import com.paybus.ui.payment.PaymentActivity;
import com.paybus.ui.transactions.TransactionsActivity;
import com.paybus.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvRoutes;
    private MaterialButton btnMyCards, btnTransactions;
    private TextView tvWelcome;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                session.logout();
                finish();
                return true;
            }
            return false;
        });

        tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Xush kelibsiz, " + session.getPhoneNumber());

        rvRoutes = findViewById(R.id.rvRoutes);
        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        rvRoutes.setHasFixedSize(true);

        btnMyCards = findViewById(R.id.btnMyCards);
        btnTransactions = findViewById(R.id.btnTransactions);

        btnMyCards.setOnClickListener(v ->
                startActivity(new Intent(this, CardsActivity.class)));

        btnTransactions.setOnClickListener(v ->
                startActivity(new Intent(this, TransactionsActivity.class)));

        loadRoutes();
    }

    private void loadRoutes() {
        PayBusApi api = ApiClient.getApi();
        api.getBusRoutes().enqueue(new Callback<List<BusRoute>>() {
            @Override
            public void onResponse(Call<List<BusRoute>> call, Response<List<BusRoute>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BusRouteAdapter adapter = new BusRouteAdapter(response.body(), route -> {
                        Intent intent = new Intent(HomeActivity.this, PaymentActivity.class);
                        intent.putExtra("route_id", route.id);
                        intent.putExtra("route_name", route.name);
                        intent.putExtra("route_price", route.price);
                        startActivity(intent);
                    });
                    rvRoutes.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<BusRoute>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
