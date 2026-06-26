package com.paybus.ui.payment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.paybus.R;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.AddCardRequest;
import com.paybus.models.CardResponse;
import com.paybus.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCardActivity extends AppCompatActivity {

    private TextInputEditText etCardNumber, etCardHolder, etExpireDate, etProvider;
    private MaterialButton btnSaveCard;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etCardNumber = findViewById(R.id.etCardNumber);
        etCardHolder = findViewById(R.id.etCardHolder);
        etExpireDate = findViewById(R.id.etExpireDate);
        etProvider = findViewById(R.id.etProvider);
        btnSaveCard = findViewById(R.id.btnSaveCard);

        btnSaveCard.setOnClickListener(v -> sendCardVerifyCode());
    }

    private void sendCardVerifyCode() {
        String cardNumber = etCardNumber.getText().toString().trim();
        String cardHolder = etCardHolder.getText().toString().trim();
        String expireDate = etExpireDate.getText().toString().trim();
        String provider = etProvider.getText().toString().trim();

        if (cardNumber.isEmpty() || cardHolder.isEmpty() || expireDate.isEmpty()) {
            Toast.makeText(this, "Barcha maydonlarni to'ldiring", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveCard.setEnabled(false);
        String token = "Bearer " + session.getToken();

        PayBusApi api = ApiClient.getApi();
        api.sendCardVerifyCode(token, new AddCardRequest(cardNumber, cardHolder, expireDate, provider))
                .enqueue(new Callback<PayBusApi.SendCodeResponse>() {
                    @Override
                    public void onResponse(Call<PayBusApi.SendCodeResponse> call,
                                           Response<PayBusApi.SendCodeResponse> response) {
                        btnSaveCard.setEnabled(true);
                        if (response.isSuccessful()) {
                            showVerifyDialog(cardNumber, cardHolder, expireDate, provider);
                        } else {
                            Toast.makeText(AddCardActivity.this, "Kod yuborishda xatolik", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PayBusApi.SendCodeResponse> call, Throwable t) {
                        btnSaveCard.setEnabled(true);
                        Toast.makeText(AddCardActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showVerifyDialog(String cardNumber, String cardHolder, String expireDate, String provider) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_verify_card);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        EditText etCode = dialog.findViewById(R.id.etVerifyCode);
        Button btnCancel = dialog.findViewById(R.id.btnCancelVerify);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmVerify);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Kodni kiriting", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            verifyCardCode(code, cardNumber, cardHolder, expireDate, provider);
        });

        dialog.show();
    }

    private void verifyCardCode(String code, String cardNumber, String cardHolder,
                                String expireDate, String provider) {
        btnSaveCard.setEnabled(false);
        String token = "Bearer " + session.getToken();

        PayBusApi api = ApiClient.getApi();
        PayBusApi.CardVerifyRequest req = new PayBusApi.CardVerifyRequest();
        req.code = code;

        api.verifyCardCode(token, req).enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> response) {
                btnSaveCard.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddCardActivity.this, "Karta muvaffaqiyatli qo'shildi", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddCardActivity.this, "Notog'ri kod", Toast.LENGTH_SHORT).show();
                    showVerifyDialog(cardNumber, cardHolder, expireDate, provider);
                }
            }

            @Override
            public void onFailure(Call<CardResponse> call, Throwable t) {
                btnSaveCard.setEnabled(true);
                Toast.makeText(AddCardActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
