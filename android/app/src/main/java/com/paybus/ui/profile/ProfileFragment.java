package com.paybus.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.adapter.CardAdapter;
import com.paybus.adapter.TransactionAdapter;
import com.paybus.api.ApiClient;
import com.paybus.api.PayBusApi;
import com.paybus.models.CardResponse;
import com.paybus.models.TransactionResponse;
import com.paybus.MainActivity;
import com.paybus.ui.payment.AddCardActivity;
import com.paybus.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvPhone;
    private RecyclerView rvCards, rvTransactions;
    private Button btnAddCard, btnLogout;
    private ImageButton btnEditProfile;
    private SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        session = new SessionManager(requireContext());

        tvName = view.findViewById(R.id.tvProfileName);
        tvPhone = view.findViewById(R.id.tvProfilePhone);
        rvCards = view.findViewById(R.id.rvCards);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        btnAddCard = view.findViewById(R.id.btnAddCard);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        rvCards.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        String phone = session.getPhoneNumber();
        tvPhone.setText(phone != null ? phone : "+998901234567");

        btnEditProfile.setOnClickListener(v -> {
            EditProfileDialog dialog = new EditProfileDialog(requireContext(), tvName.getText().toString(), newName -> {
                tvName.setText(newName);
                session.saveFullName(newName);
            });
            dialog.show();
        });

        btnAddCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddCardActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        loadCards();
        loadTransactions();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCards();

        String name = session.getFullName();
        if (name != null) {
            tvName.setText(name);
        }
    }

    private void loadCards() {
        String token = "Bearer " + session.getToken();
        PayBusApi api = ApiClient.getApi();
        api.getCards(token).enqueue(new Callback<List<CardResponse>>() {
            @Override
            public void onResponse(Call<List<CardResponse>> call, Response<List<CardResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CardAdapter adapter = new CardAdapter(response.body());
                    adapter.setContext(getContext());
                    rvCards.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<CardResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Tarmoq xatoligi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
