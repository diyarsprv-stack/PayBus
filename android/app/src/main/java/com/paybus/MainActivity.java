package com.paybus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.SendSMSRequest;
import com.paybus.models.TokenResponse;
import com.paybus.models.VerifySMSRequest;
import com.paybus.receiver.SmsReceiver;
import com.paybus.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private TextInputEditText etPhone, etCode;
    private MaterialButton btnSendCode, btnVerify, btnTelegramLogin;
    private LinearLayout phoneLayout, codeLayout;
    private TextView tvCodeSentTo, tvResendCode;
    private ImageButton btnBack;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            navigateToHome();
            return;
        }

        phoneLayout = findViewById(R.id.phoneLayout);
        codeLayout = findViewById(R.id.codeLayout);
        etPhone = findViewById(R.id.etPhone);
        etCode = findViewById(R.id.etCode);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnTelegramLogin = findViewById(R.id.btnTelegramLogin);
        tvCodeSentTo = findViewById(R.id.tvCodeSentTo);
        btnBack = findViewById(R.id.btnBack);
        tvResendCode = findViewById(R.id.tvResendCode);

        btnSendCode.setOnClickListener(v -> sendCode());
        btnVerify.setOnClickListener(v -> verifyCode());
        btnTelegramLogin.setOnClickListener(v -> openTelegramBot());
        btnBack.setOnClickListener(v -> showPhoneStep());
        tvResendCode.setOnClickListener(v -> resendCode());

        SmsReceiver.setListener(code -> {
            if (etCode != null) {
                etCode.setText(code);
                verifyCode();
            }
        });
    }

    private void showPhoneStep() {
        phoneLayout.setVisibility(View.VISIBLE);
        codeLayout.setVisibility(View.GONE);
        btnSendCode.setEnabled(true);
    }

    private void showCodeStep() {
        phoneLayout.setVisibility(View.GONE);
        codeLayout.setVisibility(View.VISIBLE);
        etCode.requestFocus();
    }

    private void openTelegramBot() {
        String phone = session.getPhoneNumber();
        if (phone == null) {
            phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Telefon raqamni kiriting", Toast.LENGTH_SHORT).show();
                return;
            }
            session.savePhoneNumber(phone);
        }
        btnTelegramLogin.setEnabled(false);

        PayBusApi api = ApiClient.getApi();
        api.sendTelegramCode(new SendSMSRequest(phone)).enqueue(new Callback<PayBusApi.TelegramCodeResponse>() {
            @Override
            public void onResponse(Call<PayBusApi.TelegramCodeResponse> call, Response<PayBusApi.TelegramCodeResponse> response) {
                btnTelegramLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().sent) {
                    Toast.makeText(MainActivity.this, "Kod Telegram orqali yuborildi", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/PayBus_bot"));
                    startActivity(intent);
                    Toast.makeText(MainActivity.this,
                            "Telegram botga yozing va Telefon raqamni ulashish tugmasini bosing",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PayBusApi.TelegramCodeResponse> call, Throwable t) {
                btnTelegramLogin.setEnabled(true);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/PayBus_bot"));
                startActivity(intent);
                Toast.makeText(MainActivity.this,
                        "Telegram botga yozing va Telefon raqamni ulashish tugmasini bosing",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendCode() {
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "Telefon raqamni kiriting", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendCode.setEnabled(false);
        PayBusApi api = ApiClient.getApi();
        api.sendCode(new SendSMSRequest(phone)).enqueue(new Callback<PayBusApi.SendCodeResponse>() {
            @Override
            public void onResponse(Call<PayBusApi.SendCodeResponse> call, Response<PayBusApi.SendCodeResponse> response) {
                btnSendCode.setEnabled(true);
                if (response.isSuccessful()) {
                    session.savePhoneNumber(phone);
                    tvCodeSentTo.setText("SMS kod yuborildi: " + phone);
                    showCodeStep();
                } else {
                    Toast.makeText(MainActivity.this, "Xatolik: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PayBusApi.SendCodeResponse> call, Throwable t) {
                btnSendCode.setEnabled(true);
                Toast.makeText(MainActivity.this, "Tarmoq xatoligi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyCode() {
        String phone = session.getPhoneNumber();
        String code = etCode.getText().toString().trim();

        if (phone == null || code.isEmpty()) {
            Toast.makeText(this, "Kodni kiriting", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);
        PayBusApi api = ApiClient.getApi();
        api.verifyCode(new VerifySMSRequest(phone, code)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                btnVerify.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    session.saveToken(response.body().access_token);
                    navigateToHome();
                } else {
                    Toast.makeText(MainActivity.this, "Notog'ri kod", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                btnVerify.setEnabled(true);
                Toast.makeText(MainActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendCode() {
        String phone = session.getPhoneNumber();
        if (phone == null) return;

        tvResendCode.setEnabled(false);
        PayBusApi api = ApiClient.getApi();
        api.sendCode(new SendSMSRequest(phone)).enqueue(new Callback<PayBusApi.SendCodeResponse>() {
            @Override
            public void onResponse(Call<PayBusApi.SendCodeResponse> call, Response<PayBusApi.SendCodeResponse> response) {
                tvResendCode.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Kod qayta yuborildi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PayBusApi.SendCodeResponse> call, Throwable t) {
                tvResendCode.setEnabled(true);
                Toast.makeText(MainActivity.this, "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SmsReceiver.setListener(null);
    }
}
