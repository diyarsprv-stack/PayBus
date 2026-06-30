package com.paybus.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReminderManager {

    private static final String PREFS_KEY = "PayBusReminders";
    private Context context;

    public static class Reminder {
        public String id;
        public String routeId;
        public String routeName;
        public String stopId;
        public String stopName;
        public String arrivalTime;
        public boolean autoPay;
        public double stopLat;
        public double stopLng;

        public Reminder(String id, String routeId, String routeName,
                        String stopId, String stopName, String arrivalTime, boolean autoPay,
                        double stopLat, double stopLng) {
            this.id = id;
            this.routeId = routeId;
            this.routeName = routeName;
            this.stopId = stopId;
            this.stopName = stopName;
            this.arrivalTime = arrivalTime;
            this.autoPay = autoPay;
            this.stopLat = stopLat;
            this.stopLng = stopLng;
        }
    }

    public ReminderManager(Context context) {
        this.context = context;
    }

    public void addReminder(String routeId, String routeName,
                            String stopId, String stopName, String arrivalTime) {
        addReminder(routeId, routeName, stopId, stopName, arrivalTime, 0, 0);
    }

    public void addReminder(String routeId, String routeName,
                            String stopId, String stopName, String arrivalTime,
                            double stopLat, double stopLng) {
        List<Reminder> reminders = getReminders();
        for (Reminder r : reminders) {
            if (r.routeId.equals(routeId) && r.stopId.equals(stopId)) {
                return;
            }
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        reminders.add(new Reminder(id, routeId, routeName, stopId, stopName, arrivalTime, false, stopLat, stopLng));
        saveReminders(reminders);
    }

    public void removeReminder(String id) {
        List<Reminder> reminders = getReminders();
        List<Reminder> updated = new ArrayList<>();
        for (Reminder r : reminders) {
            if (!r.id.equals(id)) {
                updated.add(r);
            }
        }
        saveReminders(updated);
    }

    public void toggleAutoPay(String id, boolean autoPay) {
        List<Reminder> reminders = getReminders();
        for (Reminder r : reminders) {
            if (r.id.equals(id)) {
                r.autoPay = autoPay;
                break;
            }
        }
        saveReminders(reminders);
    }

    public boolean hasReminder(String routeId, String stopId) {
        for (Reminder r : getReminders()) {
            if (r.routeId.equals(routeId) && r.stopId.equals(stopId)) {
                return true;
            }
        }
        return false;
    }

    public List<Reminder> getReminders() {
        String json = context.getSharedPreferences("PayBusRemindersPref", Context.MODE_PRIVATE)
                .getString(PREFS_KEY, "[]");
        List<Reminder> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new Reminder(
                        obj.getString("id"),
                        obj.getString("routeId"),
                        obj.getString("routeName"),
                        obj.getString("stopId"),
                        obj.getString("stopName"),
                        obj.getString("arrivalTime"),
                        obj.optBoolean("autoPay", false),
                        obj.optDouble("stopLat", 0),
                        obj.optDouble("stopLng", 0)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void saveReminders(List<Reminder> reminders) {
        JSONArray arr = new JSONArray();
        try {
            for (Reminder r : reminders) {
                JSONObject obj = new JSONObject();
                obj.put("id", r.id);
                obj.put("routeId", r.routeId);
                obj.put("routeName", r.routeName);
                obj.put("stopId", r.stopId);
                obj.put("stopName", r.stopName);
                obj.put("arrivalTime", r.arrivalTime);
                obj.put("autoPay", r.autoPay);
                obj.put("stopLat", r.stopLat);
                obj.put("stopLng", r.stopLng);
                arr.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.getSharedPreferences("PayBusRemindersPref", Context.MODE_PRIVATE)
                .edit().putString(PREFS_KEY, arr.toString()).apply();
    }
}
