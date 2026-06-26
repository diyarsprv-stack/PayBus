package com.paybus.ui.reminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paybus.R;
import com.paybus.utils.ReminderManager;

import java.util.List;

public class ReminderFragment extends Fragment {

    private RecyclerView rvReminders;
    private TextView tvEmpty;
    private ReminderManager reminderManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        reminderManager = new ReminderManager(requireContext());
        rvReminders = view.findViewById(R.id.rvReminders);
        tvEmpty = view.findViewById(R.id.tvEmptyReminders);
        rvReminders.setLayoutManager(new LinearLayoutManager(getContext()));

        loadReminders();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        List<ReminderManager.Reminder> reminders = reminderManager.getReminders();

        if (reminders.isEmpty()) {
            rvReminders.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvReminders.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            rvReminders.setAdapter(new ReminderAdapter(reminders, reminderManager));
        }
    }
}
