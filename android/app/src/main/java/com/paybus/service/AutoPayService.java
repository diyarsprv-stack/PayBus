package com.paybus.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.PaymentRequest;
import com.paybus.models.TransactionResponse;
import com.paybus.utils.ReminderManager;
import com.paybus.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutoPayService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private ReminderManager reminderManager;
    private SessionManager session;
    private android.os.Handler handler = new android.os.Handler();
    private static final long CHECK_INTERVAL = 20000;
    private static final double TRIGGER_DISTANCE = 100;
    private Runnable checkRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        reminderManager = new ReminderManager(this);
        session = new SessionManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startChecking();
        return START_STICKY;
    }

    private void startChecking() {
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkAutoPay();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.postDelayed(checkRunnable, 5000);
    }

    private void checkAutoPay() {
        List<ReminderManager.Reminder> reminders = reminderManager.getReminders();
        boolean hasAutoPay = false;
        for (ReminderManager.Reminder r : reminders) {
            if (r.autoPay && r.stopLat != 0 && r.stopLng != 0) {
                hasAutoPay = true;
                break;
            }
        }
        if (!hasAutoPay) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;
            for (ReminderManager.Reminder r : reminders) {
                if (!r.autoPay || r.stopLat == 0 || r.stopLng == 0) continue;
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        r.stopLat, r.stopLng, results);
                if (results[0] <= TRIGGER_DISTANCE) {
                    executeAutoPay(r);
                }
            }
        });
    }

    private void executeAutoPay(ReminderManager.Reminder reminder) {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();

        api.getCards(token).enqueue(new Callback<List<com.paybus.models.CardResponse>>() {
            @Override
            public void onResponse(Call<List<com.paybus.models.CardResponse>> call,
                                   Response<List<com.paybus.models.CardResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    com.paybus.models.CardResponse card = response.body().get(0);
                    PaymentRequest request = new PaymentRequest(1700, card.provider, card.id, reminder.routeId);
                    api.pay(token, request).enqueue(new Callback<TransactionResponse>() {
                        @Override
                        public void onResponse(Call<TransactionResponse> call,
                                               Response<TransactionResponse> resp) {
                            if (resp.isSuccessful()) {
                                reminderManager.toggleAutoPay(reminder.id, false);
                                Toast.makeText(AutoPayService.this,
                                        "Avtomatik to'lov: " + reminder.stopName + " - 1700 so'm",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<TransactionResponse> call, Throwable t) {
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<com.paybus.models.CardResponse>> call, Throwable t) {
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(checkRunnable);
    }
}
