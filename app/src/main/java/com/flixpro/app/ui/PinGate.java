package com.flixpro.app.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.flixpro.app.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class PinGate {
    private static final String PREFS = "flix_pro_prefs";
    private static final String KEY = "content_unlocked";
    private static final String PIN = "9966";
    private static final String WHATSAPP = "8801795071557";

    private PinGate() {}

    public static boolean isUnlocked(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false);
    }

    public static void require(Context context, Runnable onUnlocked) {
        if (isUnlocked(context)) { onUnlocked.run(); return; }
        EditText input = new EditText(context);
        input.setHint(R.string.pin_hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        int p = (int) (20 * context.getResources().getDisplayMetrics().density);
        input.setPadding(p, p / 2, p, p / 2);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.unlock_title)
                .setMessage(R.string.unlock_message)
                .setView(input)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.whatsapp, null)
                .setPositiveButton(R.string.unlock, null)
                .create();
        dialog.setOnShowListener(x -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)
                    .setOnClickListener(v -> openWhatsApp(context));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (PIN.equals(input.getText().toString().trim())) {
                    SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                    sp.edit().putBoolean(KEY, true).apply();
                    dialog.dismiss();
                    onUnlocked.run();
                } else input.setError(context.getString(R.string.wrong_pin));
            });
        });
        dialog.show();
    }

    private static void openWhatsApp(Context context) {
        String msg = "Assalamu Alaikum, Flix Pro PIN পেতে চাই।";
        Uri uri = Uri.parse("https://wa.me/" + WHATSAPP + "?text=" + Uri.encode(msg));
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Toast.makeText(context, R.string.whatsapp_unavailable, Toast.LENGTH_SHORT).show();
        }
    }
}
