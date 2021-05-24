package org.thoughtcrime.securesms.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.keyvalue.GrapherexStore;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Util;

final class LogSectionKeyPreferences implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "KEY PREFERENCES";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    return new StringBuilder().append("Screen Lock          : ").append(TextSecurePreferences.isScreenLockEnabled(context)).append("\n")
                              .append("Screen Lock Timeout  : ").append(TextSecurePreferences.getScreenLockTimeout(context)).append("\n")
                              .append("Password Disabled    : ").append(TextSecurePreferences.isPasswordDisabled(context)).append("\n")
                              .append("WiFi SMS             : ").append(TextSecurePreferences.isWifiSmsEnabled(context)).append("\n")
                              .append("Default SMS          : ").append(Util.isDefaultSmsProvider(context)).append("\n")
                              .append("Prefer Contact Photos: ").append(GrapherexStore.settings().isPreferSystemContactPhotos()).append("\n")
                              .append("Call Bandwidth Mode  : ").append(GrapherexStore.settings().getCallBandwidthMode()).append("\n")
                              .append("Client Deprecated    : ").append(GrapherexStore.misc().isClientDeprecated()).append("\n");
  }
}
