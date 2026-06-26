package com.paybus.ui.payment;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.paybus.R;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.PaymentRequest;
import com.paybus.models.TransactionResponse;
import com.paybus.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etProvider, etCardId;
    private MaterialButton btnPay;
    private TextView tvRouteInfo;
    private SessionManager session;
    private String routeId, routeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        session = new SessionManager(this);

        routeId = getIntent().getStringExtra("route_id");
        routeName = getIntent().getStringExtra("route_name");
        int routePrice = getIntent().getIntExtra("route_price", 2000);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvRouteInfo = findViewById(R.id.tvRouteInfo);
        tvRouteInfo.setText(routeId + " - " + routeName);

        etAmount = findViewById(R.id.etAmount);
        etAmount.setText(String.valueOf(routePrice));

        etProvider = findViewById(R.id.etProvider);
        etCardId = findViewById(R.id.etCardId);
        btnPay = findViewById(R.id.btnPay);

        btnPay.setOnClickListener(v -> makePayment());
    }

    private void makePayment() {
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String provider = etProvider.getText().toString().trim();
        String cardId = etCardId.getText().toString().trim();

        btnPay.setEnabled(false);
        String token = "Bearer " + session.getToken();

        PayBusApi api = ApiClient.getApi();
        api.pay(token, new PaymentRequest(amount, provider,
                cardId.isEmpty() ? null : cardId, routeId))
                .enqueue(new Callback<TransactionResponse>() {
                    @Override
                    public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                        btnPay.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            TransactionResponse t = response.body();
                            String msg = "To'lov amalga oshirildi!\n" +
                                    "Summa: " + t.amount + " so'm\n" +
                                    "Status: " + (t.status.equals("success") ? "Muvaffaqiyatli" : "Xatolik");
                            Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(PaymentActivity.this, "To'lovda xatolik", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TransactionResponse> call, Throwable t) {
                        btnPay.setEnabled(true);
                        Toast.makeText(PaymentActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
