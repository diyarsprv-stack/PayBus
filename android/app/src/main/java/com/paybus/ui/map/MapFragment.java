package com.paybus.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.paybus.R;
import com.paybus.adapter.BusArrivalAdapter;
import com.paybus.adapter.BusStopScheduleAdapter;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.CardResponse;
import com.paybus.utils.ReminderManager;
import com.paybus.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private SessionManager session;
    private ReminderManager reminderManager;
    private boolean locationPermissionGranted = false;

    private FloatingActionButton fabLocation;
    private ImageButton btnClosePanel, btnBackToStop, btnBackToRoute;
    private Button btnPayRoute, btnConfirmPay, btnDecrement, btnIncrement;
    private Spinner spinnerCards;
    private EditText etAmount;
    private TextView tvStopName, tvStopAddress;
    private TextView tvRouteTitle, tvRoutePrice;
    private TextView tvPassengerCount, tvTotalAmount;
    private RecyclerView rvBusArrivals, rvRouteStops;

    private LinearLayout busStopPanel, busRoutePanel, paymentPanel;

    private List<CardResponse> cardsList = new ArrayList<>();
    private int passengerCount = 1;
    private static final int BASE_FARE = 1700;
    private String selectedRouteId;
    private String selectedStopId;
    private String selectedRouteName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        session = new SessionManager(requireContext());
        reminderManager = new ReminderManager(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        fabLocation = view.findViewById(R.id.fabLocation);
        busStopPanel = view.findViewById(R.id.busStopPanel);
        busRoutePanel = view.findViewById(R.id.busRoutePanel);
        paymentPanel = view.findViewById(R.id.paymentPanel);
        tvStopName = view.findViewById(R.id.tvStopName);
        tvStopAddress = view.findViewById(R.id.tvStopAddress);
        tvRouteTitle = view.findViewById(R.id.tvRouteTitle);
        tvRoutePrice = view.findViewById(R.id.tvRoutePrice);
        rvBusArrivals = view.findViewById(R.id.rvBusArrivals);
        rvRouteStops = view.findViewById(R.id.rvRouteStops);
        btnClosePanel = view.findViewById(R.id.btnClosePanel);
        btnBackToStop = view.findViewById(R.id.btnBackToStop);
        btnBackToRoute = view.findViewById(R.id.btnBackToRoute);
        btnPayRoute = view.findViewById(R.id.btnPayRoute);
        btnConfirmPay = view.findViewById(R.id.btnConfirmPay);
        spinnerCards = view.findViewById(R.id.spinnerCards);
        etAmount = view.findViewById(R.id.etAmount);
        btnDecrement = view.findViewById(R.id.btnDecrement);
        btnIncrement = view.findViewById(R.id.btnIncrement);
        tvPassengerCount = view.findViewById(R.id.tvPassengerCount);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);

        rvBusArrivals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRouteStops.setLayoutManager(new LinearLayoutManager(getContext()));

        fabLocation.setOnClickListener(v -> moveToCurrentLocation());

        btnClosePanel.setOnClickListener(v -> hideAllPanels());
        btnBackToStop.setOnClickListener(v -> showStopPanel());
        btnBackToRoute.setOnClickListener(v -> showRoutePanel());

        btnPayRoute.setOnClickListener(v -> showPaymentPanel());

        btnDecrement.setOnClickListener(v -> {
            if (passengerCount > 1) {
                passengerCount--;
                updatePassengerUI();
            }
        });

        btnIncrement.setOnClickListener(v -> {
            passengerCount++;
            updatePassengerUI();
        });

        btnConfirmPay.setOnClickListener(v -> makePayment());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadCards();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMarkerClickListener(marker -> {
            String stopId = (String) marker.getTag();
            if (stopId != null) {
                selectedStopId = stopId;
                String title = marker.getTitle();
                String snippet = marker.getSnippet();
                fetchArrivals(stopId, title, snippet);
            }
            return true;
        });

        requestLocationPermission();
    }

    private void fetchNearbyStops(double lat, double lng) {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getNearbyStops(token, lat, lng).enqueue(new Callback<PayBusApi.NearbyStopsResponse>() {
            @Override
            public void onResponse(Call<PayBusApi.NearbyStopsResponse> call,
                                   Response<PayBusApi.NearbyStopsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().stops != null) {
                    for (PayBusApi.BusStop stop : response.body().stops) {
                        addBusStopMarker(stop.id, stop.lat, stop.lng, stop.name, stop.address);
                    }
                }
            }

            @Override
            public void onFailure(Call<PayBusApi.NearbyStopsResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Bekatlarni yuklashda xatolik", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchArrivals(String stopId, String name, String address) {
        hideAllPanels();
        tvStopName.setText(name);
        tvStopAddress.setText(address);
        busStopPanel.setVisibility(View.VISIBLE);
        rvBusArrivals.setAdapter(null);

        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getStopArrivals(token, stopId).enqueue(new Callback<PayBusApi.BusArrivalsResponse>() {
            @Override
            public void onResponse(Call<PayBusApi.BusArrivalsResponse> call,
                                   Response<PayBusApi.BusArrivalsResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().arrivals != null) {
                    List<BusArrivalAdapter.BusArrival> arrivals = new ArrayList<>();
                    for (PayBusApi.BusArrival a : response.body().arrivals) {
                        arrivals.add(new BusArrivalAdapter.BusArrival(
                                a.route, a.destination, a.arrival_minutes + " min"));
                    }
                    rvBusArrivals.setAdapter(new BusArrivalAdapter(arrivals, bus -> {
                        selectedRouteId = bus.route;
                        selectedRouteName = bus.destination;
                        loadRouteSchedule(bus.route);
                    }));
                }
            }

            @Override
            public void onFailure(Call<PayBusApi.BusArrivalsResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addBusStopMarker(String id, double lat, double lng, String name, String address) {
        LatLng position = new LatLng(lat, lng);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(name)
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        if (marker != null) {
            marker.setTag(id);
        }
    }

    private void showBusStopInfo(String stopId, String name, String address) {
        fetchArrivals(stopId, name, address);
    }

    private void showStopPanel() {
        hideAllPanels();
        busStopPanel.setVisibility(View.VISIBLE);
    }

    private void showRoutePanel() {
        hideAllPanels();
        busRoutePanel.setVisibility(View.VISIBLE);
    }

    private void hideAllPanels() {
        busStopPanel.setVisibility(View.GONE);
        busRoutePanel.setVisibility(View.GONE);
        paymentPanel.setVisibility(View.GONE);
    }

    private void loadRouteSchedule(String routeId) {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getRouteSchedule(token, routeId).enqueue(new Callback<PayBusApi.RouteScheduleResponse>() {
            @Override
            public void onResponse(Call<PayBusApi.RouteScheduleResponse> call,
                                   Response<PayBusApi.RouteScheduleResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().schedule != null) {
                    PayBusApi.RouteSchedule schedule = response.body().schedule;
                    tvRouteTitle.setText("№ " + routeId + " " + schedule.name);
                    tvRoutePrice.setText(schedule.price + " so'm");

                    List<BusStopScheduleAdapter.StopSchedule> stops = new ArrayList<>();
                    for (PayBusApi.ScheduleStop s : schedule.stops) {
                        stops.add(new BusStopScheduleAdapter.StopSchedule(
                                s.stop_id, s.name, s.arrival_time));
                    }
                    rvRouteStops.setAdapter(new BusStopScheduleAdapter(stops, reminderManager, routeId, schedule.name));

                    hideAllPanels();
                    busRoutePanel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<PayBusApi.RouteScheduleResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCards() {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getCards(token).enqueue(new Callback<List<CardResponse>>() {
            @Override
            public void onResponse(Call<List<CardResponse>> call, Response<List<CardResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cardsList = response.body();
                    setupCardSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<CardResponse>> call, Throwable t) {
            }
        });
    }

    private void setupCardSpinner() {
        List<String> cardLabels = new ArrayList<>();
        for (CardResponse card : cardsList) {
            String label = card.provider.toUpperCase() + " ****" + card.card_number;
            cardLabels.add(label);
        }
        if (cardLabels.isEmpty()) {
            cardLabels.add("Karta mavjud emas");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, cardLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCards.setAdapter(adapter);
    }

    private void showPaymentPanel() {
        hideAllPanels();
        passengerCount = 1;
        updatePassengerUI();
        etAmount.setText(BASE_FARE + " so'm");
        paymentPanel.setVisibility(View.VISIBLE);
    }

    private void updatePassengerUI() {
        tvPassengerCount.setText(String.valueOf(passengerCount));
        int total = BASE_FARE * passengerCount;
        tvTotalAmount.setText("Jami: " + String.format("%,d", total) + " so'm");
        etAmount.setText(total + " so'm");
    }

    private void makePayment() {
        if (cardsList.isEmpty()) {
            Toast.makeText(getContext(), "Avval karta qo'shing", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPos = spinnerCards.getSelectedItemPosition();
        if (selectedPos < 0 || selectedPos >= cardsList.size()) {
            Toast.makeText(getContext(), "Karta tanlang", Toast.LENGTH_SHORT).show();
            return;
        }

        CardResponse selectedCard = cardsList.get(selectedPos);
        int totalAmount = BASE_FARE * passengerCount;

        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        com.paybus.models.PaymentRequest request = new com.paybus.models.PaymentRequest(
                totalAmount, selectedCard.provider, selectedCard.id, selectedRouteId);

        btnConfirmPay.setEnabled(false);
        api.pay(token, request).enqueue(new Callback<com.paybus.models.TransactionResponse>() {
            @Override
            public void onResponse(Call<com.paybus.models.TransactionResponse> call,
                                   Response<com.paybus.models.TransactionResponse> response) {
                btnConfirmPay.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(),
                            "To'lov amalga oshirildi!\n" + totalAmount + " so'm",
                            Toast.LENGTH_LONG).show();
                    hideAllPanels();
                } else {
                    Toast.makeText(getContext(), "To'lovda xatolik", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.paybus.models.TransactionResponse> call, Throwable t) {
                btnConfirmPay.setEnabled(true);
                Toast.makeText(getContext(), "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveToCurrentLocation() {
        if (!locationPermissionGranted) {
            requestLocationPermission();
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                LatLng current = new LatLng(lat, lng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                mMap.addMarker(new MarkerOptions()
                        .position(current)
                        .title("Sizning joylashuvingiz")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                fetchNearbyStops(lat, lng);
            } else {
                LatLng tashkent = new LatLng(41.2995, 69.2401);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tashkent, 13f));
                fetchNearbyStops(41.2995, 69.2401);
                Toast.makeText(getContext(), "Joylashuv topilmadi, Toshkent markazi ko'rsatilmoqda", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(getContext(), "Joylashuvni aniqlash uchun ruxsat kerak", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1001) {
            locationPermissionGranted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (locationPermissionGranted) {
                moveToCurrentLocation();
            }
        }
    }
}
