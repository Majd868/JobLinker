package com.example.joblinker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A non-deprecated replacement for android.app.ProgressDialog.
 * Uses AlertDialog + ProgressBar instead.
 */
public class ProgressDialogHelper {

    private final AlertDialog dialog;

    public ProgressDialogHelper(Context context, String message) {
        // Build layout programmatically — no extra XML needed
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int pad = dp(context, 24);
        layout.setPadding(pad, pad, pad, pad);

        ProgressBar progressBar = new ProgressBar(context);
        int size = dp(context, 40);
        android.widget.LinearLayout.LayoutParams pbParams =
                new android.widget.LinearLayout.LayoutParams(size, size);
        progressBar.setLayoutParams(pbParams);
        layout.addView(progressBar);

        TextView tvMessage = new TextView(context);
        android.widget.LinearLayout.LayoutParams tvParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParams.setMarginStart(dp(context, 20));
        tvMessage.setLayoutParams(tvParams);
        tvMessage.setText(message);
        tvMessage.setTextSize(16f);
        layout.addView(tvMessage);

        dialog = new AlertDialog.Builder(context)
                .setView(layout)
                .setCancelable(false)
                .create();
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    private static int dp(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
