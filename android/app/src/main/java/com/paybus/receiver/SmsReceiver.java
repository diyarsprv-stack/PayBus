package com.paybus.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener listener;

    public interface SmsListener {
        void onSmsReceived(String code);
    }

    public static void setListener(SmsListener listener) {
        SmsReceiver.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            String msgBody = message.getMessageBody();

            if (msgBody != null && msgBody.contains("PayBus")) {
                String code = msgBody.replaceAll("[^0-9]", "");
                if (code.length() >= 6) {
                    code = code.substring(0, 6);
                    if (listener != null) {
                        listener.onSmsReceived(code);
                    }
                }
            }
        }
    }
}
