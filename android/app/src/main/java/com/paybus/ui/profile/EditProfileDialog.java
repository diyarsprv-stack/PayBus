package com.paybus.ui.profile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.paybus.R;

public class EditProfileDialog {

    private Context context;
    private String currentName;
    private OnNameSaveListener listener;

    public interface OnNameSaveListener {
        void onSave(String newName);
    }

    public EditProfileDialog(Context context, String currentName, OnNameSaveListener listener) {
        this.context = context;
        this.currentName = currentName;
        this.listener = listener;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_profile);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        EditText etName = dialog.findViewById(R.id.etEditName);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        etName.setText(currentName);
        etName.setSelection(etName.length());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (!newName.isEmpty()) {
                listener.onSave(newName);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
