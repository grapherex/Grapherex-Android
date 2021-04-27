package org.thoughtcrime.securesms.permissions;


import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ThemeUtil;

public class RationaleDialog {

    public static AlertDialog.Builder createFor(@NonNull Context context, @NonNull String message, @DrawableRes int... drawables) {
        View view = LayoutInflater.from(context).inflate(R.layout.permissions_rationale_dialog, null);
        AppCompatImageView headerIcon = view.findViewById(R.id.ivHeaderIcon);
        AppCompatTextView textMessage = view.findViewById(R.id.tvMessage);
        if (drawables.length > 0) {
            headerIcon.setImageResource(drawables[0]);
        }

        textMessage.setText(message);

        return new AlertDialog.Builder(context, ThemeUtil.isDarkTheme(context) ? R.style.Theme_Signal_AlertDialog_Dark_Cornered : R.style.Theme_Signal_AlertDialog_Light_Cornered)
                .setView(view);
    }

}
