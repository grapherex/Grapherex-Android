package org.thoughtcrime.securesms.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.keyvalue.GrapherexStore;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

public class LogSectionPin implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "PIN STATE";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    return new StringBuilder().append("State: ").append(GrapherexStore.pinValues().getPinState()).append("\n")
                              .append("Last Successful Reminder Entry: ").append(GrapherexStore.pinValues().getLastSuccessfulEntryTime()).append("\n")
                              .append("Next Reminder Interval: ").append(GrapherexStore.pinValues().getCurrentInterval()).append("\n")
                              .append("ReglockV1: ").append(TextSecurePreferences.isV1RegistrationLockEnabled(context)).append("\n")
                              .append("ReglockV2: ").append(GrapherexStore.kbsValues().isV2RegistrationLockEnabled()).append("\n")
                              .append("Signal PIN: ").append(GrapherexStore.kbsValues().hasPin()).append("\n")
                              .append("Opted Out: ").append(GrapherexStore.kbsValues().hasOptedOut()).append("\n")
                              .append("Last Creation Failed: ").append(GrapherexStore.kbsValues().lastPinCreateFailed()).append("\n")
                              .append("Needs Account Restore: ").append(GrapherexStore.storageServiceValues().needsAccountRestore()).append("\n")
                              .append("PIN Required at Registration: ").append(GrapherexStore.registrationValues().pinWasRequiredAtRegistration()).append("\n")
                              .append("Registration Complete: ").append(GrapherexStore.registrationValues().isRegistrationComplete());

  }
}
