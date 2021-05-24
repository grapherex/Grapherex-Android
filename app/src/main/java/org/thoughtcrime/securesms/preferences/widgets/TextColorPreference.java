package org.thoughtcrime.securesms.preferences.widgets;


import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.thoughtcrime.securesms.R;

public class TextColorPreference extends Preference {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextColorPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TextColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextColorPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        ((TextView) viewHolder.findViewById(android.R.id.title)).setTextColor(getContext().getResources().getColor(R.color.red));
    }
}
